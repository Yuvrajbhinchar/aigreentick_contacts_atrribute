package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.dto.request.*;
import com.aigreentick.services.contacts.dto.response.*;
import com.aigreentick.services.contacts.entity.*;
import com.aigreentick.services.contacts.exception.*;
import com.aigreentick.services.contacts.mapper.ContactMapper;
import com.aigreentick.services.contacts.repository.*;
import com.aigreentick.services.contacts.specification.ContactSpecifications;
import com.aigreentick.services.contacts.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Contact business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final ContactAttributeValueRepository attributeValueRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final ContactTagAssignmentRepository tagAssignmentRepository;
    private final ContactTagRepository tagRepository;
    private final ContactNoteRepository noteRepository;
    private final ContactMapper contactMapper;

    /**
     * Create new contact with attributes and tags
     */
    @Transactional
    public ContactResponse createContact(ContactCreateRequest request, Long organizationId) {

        log.info("Creating contact for organization: {}, phone: {}", organizationId, request.getPhoneNumber());

        // 1. Normalize phone number to E.164
        String e164Phone = PhoneNumberUtil.normalizeToE164(request.getPhoneNumber());

        // 2. Check for duplicate
        if (contactRepository.existsByOrganizationIdAndWaPhoneE164(organizationId, e164Phone)) {
            Contact existing = contactRepository.findByOrganizationIdAndWaPhoneE164(organizationId, e164Phone)
                    .orElseThrow();
            throw new DuplicateContactException(e164Phone, existing.getId());
        }

        // 3. Create contact entity
        Contact contact = new Contact();
        contact.setOrganizationId(organizationId);
        contact.setWaPhoneE164(e164Phone);
        contact.setWaId(PhoneNumberUtil.generateWhatsAppId(e164Phone));
        contact.setDisplayName(request.getName());
        contact.setSource(Contact.Source.manual);
        contact.setFirstSeenAt(LocalDateTime.now());

        contact = contactRepository.save(contact);
        log.info("Contact created with ID: {}", contact.getId());

        // 4. Process attributes (if any)
        List<ContactAttributeValue> attributes = new ArrayList<>();
        Map<Long, AttributeDefinition> attributeDefinitions = new HashMap<>();

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributes = createContactAttributes(
                    contact.getId(),
                    request.getAttributes(),
                    organizationId
            );

            // Load attribute definitions for response
            List<Long> attrDefIds = attributes.stream()
                    .map(ContactAttributeValue::getAttributeDefinitionId)
                    .collect(Collectors.toList());

            attributeDefinitionRepository.findAllById(attrDefIds).forEach(def ->
                    attributeDefinitions.put(def.getId(), def)
            );
        }

        // 5. Assign tags (if any)
        List<ContactTagAssignment> tagAssignments = new ArrayList<>();
        List<ContactTag> tags = new ArrayList<>();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tagAssignments = assignTagsToContact(
                    contact.getId(),
                    request.getTagIds(),
                    organizationId,
                    null // No user ID for now, can be added later
            );

            tags = tagRepository.findAllById(request.getTagIds());
        }

        // 6. Add initial note (if provided)
        List<ContactNote> notes = new ArrayList<>();
        if (request.getInitialNote() != null && !request.getInitialNote().trim().isEmpty()) {
            ContactNote note = new ContactNote();
            note.setOrganizationId(organizationId);
            note.setContactId(contact.getId());
            note.setNoteText(request.getInitialNote());
            note.setVisibility(ContactNote.Visibility.team);
            note.setCreatedBy(null); // TODO: Get from security context

            note = noteRepository.save(note);
            notes.add(note);
        }

        // 7. Build and return response
        return contactMapper.toResponse(
                contact,
                attributes,
                tagAssignments,
                tags,
                notes,
                attributeDefinitions
        );
    }

    /**
     * Get contact by ID with all relations
     */
    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long contactId, Long organizationId) {

        log.info("Fetching contact: {} for organization: {}", contactId, organizationId);

        // 1. Find contact
        Contact contact = contactRepository.findByIdAndOrganizationId(contactId, organizationId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));

        // 2. Load attributes
        List<ContactAttributeValue> attributes = attributeValueRepository.findByContactId(contactId);

        // 3. Load attribute definitions
        Map<Long, AttributeDefinition> attributeDefinitions = new HashMap<>();
        if (!attributes.isEmpty()) {
            List<Long> attrDefIds = attributes.stream()
                    .map(ContactAttributeValue::getAttributeDefinitionId)
                    .collect(Collectors.toList());

            attributeDefinitionRepository.findAllById(attrDefIds).forEach(def ->
                    attributeDefinitions.put(def.getId(), def)
            );
        }

        // 4. Load tag assignments
        List<ContactTagAssignment> tagAssignments = tagAssignmentRepository.findByContactId(contactId);

        // 5. Load tags
        List<ContactTag> tags = new ArrayList<>();
        if (!tagAssignments.isEmpty()) {
            List<Long> tagIds = tagAssignments.stream()
                    .map(ContactTagAssignment::getTagId)
                    .collect(Collectors.toList());

            tags = tagRepository.findAllById(tagIds);
        }

        // 6. Load notes (recent 5)
        List<ContactNote> notes = noteRepository.findByContactIdOrderByCreatedAtDesc(contactId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // 7. Build response
        return contactMapper.toResponse(
                contact,
                attributes,
                tagAssignments,
                tags,
                notes,
                attributeDefinitions
        );
    }

    /**
     * List contacts with pagination and filters
     */
    @Transactional(readOnly = true)
    public PageResponse<ContactListItemResponse> listContacts(
            ContactSearchRequest searchRequest,
            Long organizationId
    ) {

        log.info("Listing contacts for organization: {} with search: {}",
                organizationId, searchRequest.getSearch());

        // 1. Build specification (dynamic query)
        Specification<Contact> spec = buildSpecification(searchRequest, organizationId);

        // 2. Build pageable
        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                sort
        );

        // 3. Execute query
        Page<Contact> contactPage = contactRepository.findAll(spec, pageable);

        if (contactPage.isEmpty()) {
            return PageResponse.<ContactListItemResponse>builder()
                    .content(new ArrayList<>())
                    .page(searchRequest.getPage())
                    .size(searchRequest.getSize())
                    .totalElements(0L)
                    .totalPages(0)
                    .hasNext(false)
                    .hasPrevious(false)
                    .isFirst(true)
                    .isLast(true)
                    .build();
        }

        // 4. Batch load related data for performance
        List<Long> contactIds = contactPage.getContent().stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

        // Load all attributes for these contacts
        List<ContactAttributeValue> allAttributes = attributeValueRepository.findByContactIdIn(contactIds);
        Map<Long, List<ContactAttributeValue>> attributesByContact = allAttributes.stream()
                .collect(Collectors.groupingBy(ContactAttributeValue::getContactId));

        // Load attribute definitions
        Set<Long> attrDefIds = allAttributes.stream()
                .map(ContactAttributeValue::getAttributeDefinitionId)
                .collect(Collectors.toSet());

        Map<Long, AttributeDefinition> attributeDefinitions =
                attributeDefinitionRepository.findAllById(attrDefIds).stream()
                        .collect(Collectors.toMap(AttributeDefinition::getId, def -> def));

        // Load all tag assignments for these contacts
        List<ContactTagAssignment> allTagAssignments =
                tagAssignmentRepository.findByContactIdIn(contactIds);
        Map<Long, List<ContactTagAssignment>> tagAssignmentsByContact =
                allTagAssignments.stream()
                        .collect(Collectors.groupingBy(ContactTagAssignment::getContactId));

        // Load tags
        Set<Long> tagIds = allTagAssignments.stream()
                .map(ContactTagAssignment::getTagId)
                .collect(Collectors.toSet());

        Map<Long, ContactTag> tagsMap =
                tagRepository.findAllById(tagIds).stream()
                        .collect(Collectors.toMap(ContactTag::getId, tag -> tag));

        // 5. Map to response DTOs
        List<ContactListItemResponse> responseList = contactPage.getContent().stream()
                .map(contact -> {
                    List<ContactAttributeValue> contactAttrs =
                            attributesByContact.getOrDefault(contact.getId(), new ArrayList<>());

                    List<ContactTagAssignment> contactTagAssignments =
                            tagAssignmentsByContact.getOrDefault(contact.getId(), new ArrayList<>());

                    List<ContactTag> contactTags = contactTagAssignments.stream()
                            .map(assignment -> tagsMap.get(assignment.getTagId()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    return contactMapper.toListItemResponse(
                            contact,
                            contactAttrs,
                            contactTagAssignments,
                            contactTags,
                            attributeDefinitions,
                            0 // note count
                    );
                })
                .collect(Collectors.toList());

        // 6. Build page response
        return PageResponse.<ContactListItemResponse>builder()
                .content(responseList)
                .page(contactPage.getNumber())
                .size(contactPage.getSize())
                .totalElements(contactPage.getTotalElements())
                .totalPages(contactPage.getTotalPages())
                .hasNext(contactPage.hasNext())
                .hasPrevious(contactPage.hasPrevious())
                .isFirst(contactPage.isFirst())
                .isLast(contactPage.isLast())
                .build();
    }

    /**
     * Update contact
     */
    @Transactional
    public ContactResponse updateContact(
            Long contactId,
            ContactUpdateRequest request,
            Long organizationId
    ) {

        log.info("Updating contact: {} for organization: {}", contactId, organizationId);

        // 1. Find existing contact
        Contact contact = contactRepository.findByIdAndOrganizationId(contactId, organizationId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));

        // 2. Check phone number change for duplicates
        String newE164Phone = PhoneNumberUtil.normalizeToE164(request.getPhoneNumber());

        if (!contact.getWaPhoneE164().equals(newE164Phone)) {
            // Phone number is changing, check for duplicates
            Optional<Contact> duplicate = contactRepository
                    .findByOrganizationIdAndWaPhoneE164(organizationId, newE164Phone);

            if (duplicate.isPresent() && !duplicate.get().getId().equals(contactId)) {
                throw new DuplicateContactException(newE164Phone, duplicate.get().getId());
            }

            // Update phone
            contact.setWaPhoneE164(newE164Phone);
            contact.setWaId(PhoneNumberUtil.generateWhatsAppId(newE164Phone));
        }

        // 3. Update basic fields
        contact.setDisplayName(request.getName());
        contact = contactRepository.save(contact);

        // 4. Replace attributes (delete old, insert new)
        attributeValueRepository.deleteByContactId(contactId);

        List<ContactAttributeValue> attributes = new ArrayList<>();
        Map<Long, AttributeDefinition> attributeDefinitions = new HashMap<>();

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributes = createContactAttributes(
                    contactId,
                    request.getAttributes(),
                    organizationId
            );

            List<Long> attrDefIds = attributes.stream()
                    .map(ContactAttributeValue::getAttributeDefinitionId)
                    .collect(Collectors.toList());

            attributeDefinitionRepository.findAllById(attrDefIds).forEach(def ->
                    attributeDefinitions.put(def.getId(), def)
            );
        }

        // 5. Replace tags (delete old, insert new)
        tagAssignmentRepository.deleteByContactId(contactId);

        List<ContactTagAssignment> tagAssignments = new ArrayList<>();
        List<ContactTag> tags = new ArrayList<>();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tagAssignments = assignTagsToContact(
                    contactId,
                    request.getTagIds(),
                    organizationId,
                    null
            );

            tags = tagRepository.findAllById(request.getTagIds());
        }

        // 6. Load notes
        List<ContactNote> notes = noteRepository.findByContactIdOrderByCreatedAtDesc(contactId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // 7. Build response
        return contactMapper.toResponse(
                contact,
                attributes,
                tagAssignments,
                tags,
                notes,
                attributeDefinitions
        );
    }

    /**
     * Delete contact and all related data
     */
    @Transactional
    public void deleteContact(Long contactId, Long organizationId) {

        log.info("Deleting contact: {} for organization: {}", contactId, organizationId);

        // 1. Verify contact exists and belongs to organization
        Contact contact = contactRepository.findByIdAndOrganizationId(contactId, organizationId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));

        // 2. Delete related data (cascade)
        attributeValueRepository.deleteByContactId(contactId);
        tagAssignmentRepository.deleteByContactId(contactId);

        // Delete notes
        List<ContactNote> notes = noteRepository.findByContactId(contactId);
        noteRepository.deleteAll(notes);

        // 3. Delete contact
        contactRepository.delete(contact);

        log.info("Contact deleted: {}", contactId);
    }

    /**
     * Get total contact count for organization
     */
    @Transactional(readOnly = true)
    public long getContactCount(Long organizationId) {
        log.info("Getting contact count for organization: {}", organizationId);
        return contactRepository.countByOrganizationId(organizationId);
    }

    /**
     * Helper: Create contact attributes
     */
    private List<ContactAttributeValue> createContactAttributes(
            Long contactId,
            Map<String, String> attributes,
            Long organizationId
    ) {

        List<ContactAttributeValue> savedAttributes = new ArrayList<>();

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey().trim();
            String value = entry.getValue();

            if (key.isEmpty() || value == null || value.trim().isEmpty()) {
                continue; // Skip empty attributes
            }

            // Find or create attribute definition
            AttributeDefinition definition = attributeDefinitionRepository
                    .findByOrganizationIdAndAttrKey(organizationId, key)
                    .orElseGet(() -> createAttributeDefinition(key, organizationId));

            // Create attribute value
            ContactAttributeValue attributeValue = new ContactAttributeValue();
            attributeValue.setContactId(contactId);
            attributeValue.setAttributeDefinitionId(definition.getId());
            attributeValue.setValueText(value); // For simplicity, store everything as text
            attributeValue.setUpdatedSource(ContactAttributeValue.UpdatedSource.user);

            savedAttributes.add(attributeValueRepository.save(attributeValue));
        }

        return savedAttributes;
    }

    /**
     * Helper: Create attribute definition
     */
    private AttributeDefinition createAttributeDefinition(String key, Long organizationId) {

        log.info("Creating new attribute definition: {} for organization: {}", key, organizationId);

        AttributeDefinition definition = new AttributeDefinition();
        definition.setOrganizationId(organizationId);
        definition.setAttrKey(key);
        definition.setLabel(capitalize(key)); // Capitalize first letter
        definition.setCategory(AttributeDefinition.Category.user_defined);
        definition.setDataType(AttributeDefinition.DataType.text); // Default to text
        definition.setIsEditable(true);
        definition.setIsRequired(false);
        definition.setIsSearchable(true);

        return attributeDefinitionRepository.save(definition);
    }

    /**
     * Helper: Assign tags to contact
     */
    private List<ContactTagAssignment> assignTagsToContact(
            Long contactId,
            List<Long> tagIds,
            Long organizationId,
            Long userId
    ) {

        List<ContactTagAssignment> assignments = new ArrayList<>();

        for (Long tagId : tagIds) {
            // Verify tag exists and belongs to organization
            ContactTag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new InvalidAttributeException("Tag not found: " + tagId));

            if (!tag.getOrganizationId().equals(organizationId)) {
                throw new ContactAccessDeniedException("Tag does not belong to organization");
            }

            ContactTagAssignment assignment = new ContactTagAssignment();
            assignment.setOrganizationId(organizationId);
            assignment.setContactId(contactId);
            assignment.setTagId(tagId);
            assignment.setAssignedBy(userId);

            assignments.add(tagAssignmentRepository.save(assignment));
        }

        return assignments;
    }

    /**
     * Helper: Build specification from search request
     */
    private Specification<Contact> buildSpecification(
            ContactSearchRequest request,
            Long organizationId
    ) {

        Specification<Contact> spec = Specification.where(
                ContactSpecifications.belongsToOrganization(organizationId)
        );

        // Search term (name or phone)
        if (request.getSearch() != null && !request.getSearch().trim().isEmpty()) {
            spec = spec.and(ContactSpecifications.searchByNameOrPhone(request.getSearch()));
        }

        // Specific phone filter
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            spec = spec.and(ContactSpecifications.hasPhoneLike(request.getPhone()));
        }

        // Source filter
        if (request.getSource() != null && !request.getSource().trim().isEmpty()) {
            spec = spec.and(ContactSpecifications.hasSource(request.getSource()));
        }

        // Tag filter
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            spec = spec.and(ContactSpecifications.hasTags(request.getTagIds()));
        }

        // Date filters
        if (request.getCreatedAfter() != null || request.getCreatedBefore() != null) {
            spec = spec.and(ContactSpecifications.createdBetween(
                    request.getCreatedAfter(),
                    request.getCreatedBefore()
            ));
        }

        if (request.getLastSeenAfter() != null || request.getLastSeenBefore() != null) {
            spec = spec.and(ContactSpecifications.lastSeenBetween(
                    request.getLastSeenAfter(),
                    request.getLastSeenBefore()
            ));
        }

        // Attribute filter
        if (request.getAttributeKey() != null && !request.getAttributeKey().trim().isEmpty()) {
            spec = spec.and(ContactSpecifications.hasAttribute(
                    request.getAttributeKey(),
                    request.getAttributeValue()
            ));
        }

        return spec;
    }

    /**
     * Helper: Capitalize first letter
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}