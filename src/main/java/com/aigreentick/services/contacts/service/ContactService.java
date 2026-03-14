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
    private final ProjectContactRepository projectContactRepository;
    private final ContactMapper contactMapper;

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public ContactResponse createContact(ContactCreateRequest request, Long organizationId) {
        log.info("Creating contact for org: {}, phone: {}", organizationId, request.getPhoneNumber());

        String e164Phone = PhoneNumberUtil.normalizeToE164(request.getPhoneNumber());

        if (contactRepository.existsByOrganizationIdAndWaPhoneE164(organizationId, e164Phone)) {
            Contact existing = contactRepository
                    .findByOrganizationIdAndWaPhoneE164(organizationId, e164Phone)
                    .orElseThrow();
            throw new DuplicateContactException(e164Phone, existing.getId());
        }

        Contact contact = new Contact();
        contact.setOrganizationId(organizationId);
        contact.setWaPhoneE164(e164Phone);
        contact.setWaId(PhoneNumberUtil.generateWhatsAppId(e164Phone));
        contact.setDisplayName(request.getName());
        contact.setSource(Contact.Source.MANUAL);
        contact.setFirstSeenAt(LocalDateTime.now());
        contact = contactRepository.save(contact);
        log.info("Contact created with ID: {}", contact.getId());

        // FIX: Batch-load all attribute definitions up front instead of one query per key (N+1)
        List<ContactAttributeValue> attributes = new ArrayList<>();
        Map<Long, AttributeDefinition> attributeDefinitions = new HashMap<>();

        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            attributes = createContactAttributes(contact.getId(), request.getAttributes(), organizationId);

            List<Long> attrDefIds = attributes.stream()
                    .map(ContactAttributeValue::getAttributeDefinitionId)
                    .collect(Collectors.toList());
            attributeDefinitionRepository.findAllById(attrDefIds)
                    .forEach(def -> attributeDefinitions.put(def.getId(), def));
        }

        List<ContactTagAssignment> tagAssignments = new ArrayList<>();
        List<ContactTag> tags = new ArrayList<>();

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tagAssignments = assignTagsToContact(contact.getId(), request.getTagIds(), organizationId, null);
            tags = tagRepository.findAllById(request.getTagIds());
        }

        List<ContactNote> notes = new ArrayList<>();
        if (request.getInitialNote() != null && !request.getInitialNote().trim().isEmpty()) {
            ContactNote note = new ContactNote();
            note.setOrganizationId(organizationId);
            note.setContactId(contact.getId());
            note.setNoteText(request.getInitialNote());
            note.setVisibility(ContactNote.Visibility.team);
            note.setCreatedBy(null);
            notes.add(noteRepository.save(note));
        }

        return contactMapper.toResponse(contact, attributes, tagAssignments, tags, notes, attributeDefinitions);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long contactId, Long organizationId) {
        log.info("Fetching contact: {} for org: {}", contactId, organizationId);

        Contact contact = contactRepository.findByIdAndOrganizationId(contactId, organizationId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));

        List<ContactAttributeValue> attributes = attributeValueRepository.findByContactId(contactId);

        Map<Long, AttributeDefinition> attributeDefinitions = new HashMap<>();
        if (!attributes.isEmpty()) {
            List<Long> attrDefIds = attributes.stream()
                    .map(ContactAttributeValue::getAttributeDefinitionId)
                    .collect(Collectors.toList());
            attributeDefinitionRepository.findAllById(attrDefIds)
                    .forEach(def -> attributeDefinitions.put(def.getId(), def));
        }

        List<ContactTagAssignment> tagAssignments = tagAssignmentRepository.findByContactId(contactId);

        List<ContactTag> tags = new ArrayList<>();
        if (!tagAssignments.isEmpty()) {
            List<Long> tagIds = tagAssignments.stream()
                    .map(ContactTagAssignment::getTagId)
                    .collect(Collectors.toList());
            tags = tagRepository.findAllById(tagIds);
        }

        // FIX: .limit(5) applied once here; mapper no longer applies its own limit
        List<ContactNote> notes = noteRepository.findByContactIdOrderByCreatedAtDesc(contactId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        return contactMapper.toResponse(contact, attributes, tagAssignments, tags, notes, attributeDefinitions);
    }

    // ── List ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ContactListItemResponse> listContacts(
            ContactSearchRequest searchRequest,
            Long organizationId
    ) {
        log.info("Listing contacts for org: {}, page: {}, size: {}",
                organizationId, searchRequest.getPage(), searchRequest.getSize());

        Specification<Contact> spec = buildSpecification(searchRequest, organizationId);

        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("ASC")
                        ? Sort.Direction.ASC : Sort.Direction.DESC,
                searchRequest.getSortBy()
        );
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

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

        List<Long> contactIds = contactPage.getContent().stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

        // Batch load attributes
        List<ContactAttributeValue> allAttributes = attributeValueRepository.findByContactIdIn(contactIds);
        Map<Long, List<ContactAttributeValue>> attributesByContact = allAttributes.stream()
                .collect(Collectors.groupingBy(ContactAttributeValue::getContactId));

        Set<Long> attrDefIds = allAttributes.stream()
                .map(ContactAttributeValue::getAttributeDefinitionId)
                .collect(Collectors.toSet());
        Map<Long, AttributeDefinition> attributeDefinitions =
                attributeDefinitionRepository.findAllById(attrDefIds).stream()
                        .collect(Collectors.toMap(AttributeDefinition::getId, def -> def));

        // Batch load tag assignments
        List<ContactTagAssignment> allTagAssignments = tagAssignmentRepository.findByContactIdIn(contactIds);
        Map<Long, List<ContactTagAssignment>> tagAssignmentsByContact = allTagAssignments.stream()
                .collect(Collectors.groupingBy(ContactTagAssignment::getContactId));

        Set<Long> tagIds = allTagAssignments.stream()
                .map(ContactTagAssignment::getTagId)
                .collect(Collectors.toSet());
        Map<Long, ContactTag> tagsMap = tagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(ContactTag::getId, tag -> tag));

        // FIX: Fetch actual note counts per contact instead of hardcoding 0
        Map<Long, Long> noteCountByContact = contactIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        noteRepository::countByContactId
                ));

        List<ContactListItemResponse> responseList = contactPage.getContent().stream()
                .map(contact -> {
                    List<ContactAttributeValue> contactAttrs =
                            attributesByContact.getOrDefault(contact.getId(), new ArrayList<>());
                    List<ContactTagAssignment> contactTagAssignments =
                            tagAssignmentsByContact.getOrDefault(contact.getId(), new ArrayList<>());
                    List<ContactTag> contactTags = contactTagAssignments.stream()
                            .map(a -> tagsMap.get(a.getTagId()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    long noteCount = noteCountByContact.getOrDefault(contact.getId(), 0L);

                    return contactMapper.toListItemResponse(
                            contact, contactAttrs, contactTagAssignments,
                            contactTags, attributeDefinitions, noteCount
                    );
                })
                .collect(Collectors.toList());

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

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public ContactResponse updateContact(Long contactId, ContactUpdateRequest request, Long organizationId) {
        log.info("Updating contact: {} for org: {}", contactId, organizationId);

        Contact contact = contactRepository.findByIdAndOrganizationId(contactId, organizationId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));

        String newE164Phone = PhoneNumberUtil.normalizeToE164(request.getPhoneNumber());
        if (!contact.getWaPhoneE164().equals(newE164Phone)) {
            Optional<Contact> duplicate = contactRepository
                    .findByOrganizationIdAndWaPhoneE164(organizationId, newE164Phone);
            if (duplicate.isPresent() && !duplicate.get().getId().equals(contactId)) {
                throw new DuplicateContactException(newE164Phone, duplicate.get().getId());
            }
            contact.setWaPhoneE164(newE164Phone);
            contact.setWaId(PhoneNumberUtil.generateWhatsAppId(newE164Phone));
        }

        contact.setDisplayName(request.getName());
        contact = contactRepository.save(contact);

        // FIX: Only delete + replace attributes when the caller actually sent an attributes map.
        // Previously, omitting the field silently wiped all existing attributes.
        List<ContactAttributeValue> attributes = new ArrayList<>();
        Map<Long, AttributeDefinition> attributeDefinitions = new HashMap<>();

        if (request.getAttributes() != null) {
            attributeValueRepository.deleteByContactId(contactId);

            if (!request.getAttributes().isEmpty()) {
                attributes = createContactAttributes(contactId, request.getAttributes(), organizationId);
                List<Long> attrDefIds = attributes.stream()
                        .map(ContactAttributeValue::getAttributeDefinitionId)
                        .collect(Collectors.toList());
                attributeDefinitionRepository.findAllById(attrDefIds)
                        .forEach(def -> attributeDefinitions.put(def.getId(), def));
            }
        } else {
            // Attributes not supplied — keep existing ones and load them for the response
            attributes = attributeValueRepository.findByContactId(contactId);
            if (!attributes.isEmpty()) {
                List<Long> attrDefIds = attributes.stream()
                        .map(ContactAttributeValue::getAttributeDefinitionId)
                        .collect(Collectors.toList());
                attributeDefinitionRepository.findAllById(attrDefIds)
                        .forEach(def -> attributeDefinitions.put(def.getId(), def));
            }
        }

        // FIX: Same guard for tags
        List<ContactTagAssignment> tagAssignments = new ArrayList<>();
        List<ContactTag> tags = new ArrayList<>();

        if (request.getTagIds() != null) {
            tagAssignmentRepository.deleteByContactId(contactId);
            if (!request.getTagIds().isEmpty()) {
                tagAssignments = assignTagsToContact(contactId, request.getTagIds(), organizationId, null);
                tags = tagRepository.findAllById(request.getTagIds());
            }
        } else {
            tagAssignments = tagAssignmentRepository.findByContactId(contactId);
            if (!tagAssignments.isEmpty()) {
                List<Long> existingTagIds = tagAssignments.stream()
                        .map(ContactTagAssignment::getTagId)
                        .collect(Collectors.toList());
                tags = tagRepository.findAllById(existingTagIds);
            }
        }

        // FIX: .limit(5) applied once here only
        List<ContactNote> notes = noteRepository.findByContactIdOrderByCreatedAtDesc(contactId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        return contactMapper.toResponse(contact, attributes, tagAssignments, tags, notes, attributeDefinitions);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteContact(Long contactId, Long organizationId) {
        log.info("Deleting contact: {} for org: {}", contactId, organizationId);

        Contact contact = contactRepository.findByIdAndOrganizationId(contactId, organizationId)
                .orElseThrow(() -> new ContactNotFoundException(contactId));

        // FIX: Use bulk JPQL deletes (no entity loading into memory) and include project_contacts
        attributeValueRepository.deleteByContactId(contactId);
        tagAssignmentRepository.deleteByContactId(contactId);
        noteRepository.deleteByContactId(contactId);             // FIX: was loadAll() + deleteAll()
        projectContactRepository.deleteByContactId(contactId);   // FIX: was missing entirely

        contactRepository.delete(contact);
        log.info("Contact deleted: {}", contactId);
    }

    // ── Count ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public long getContactCount(Long organizationId) {
        return contactRepository.countByOrganizationId(organizationId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * FIX: Batch-loads all attribute definitions for the given keys in a single query,
     * then creates only the ones that are missing. Previously called findByOrganizationIdAndAttrKey
     * once per key inside the loop (N+1 queries).
     */
    private List<ContactAttributeValue> createContactAttributes(
            Long contactId,
            Map<String, String> attributes,
            Long organizationId
    ) {
        // 1. Batch-load all definitions that already exist for these keys
        List<String> keys = attributes.keySet().stream()
                .map(String::trim)
                .filter(k -> !k.isEmpty())
                .collect(Collectors.toList());

        Map<String, AttributeDefinition> existingDefs =
                attributeDefinitionRepository.findByOrganizationIdAndAttrKeyIn(organizationId, keys)
                        .stream()
                        .collect(Collectors.toMap(AttributeDefinition::getAttrKey, def -> def));

        List<ContactAttributeValue> savedAttributes = new ArrayList<>();

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey().trim();
            String value = entry.getValue();

            if (key.isEmpty() || value == null || value.trim().isEmpty()) continue;

            // 2. Create definition only if it does not exist
            AttributeDefinition definition = existingDefs.computeIfAbsent(key, k -> {
                log.info("Creating new attribute definition: {} for org: {}", k, organizationId);
                AttributeDefinition def = new AttributeDefinition();
                def.setOrganizationId(organizationId);
                def.setAttrKey(k);
                def.setLabel(capitalize(k));
                def.setCategory(AttributeDefinition.Category.user_defined);
                def.setDataType(AttributeDefinition.DataType.text);
                def.setIsEditable(true);
                def.setIsRequired(false);
                def.setIsSearchable(true);
                return attributeDefinitionRepository.save(def);
            });

            ContactAttributeValue attributeValue = new ContactAttributeValue();
            attributeValue.setContactId(contactId);
            attributeValue.setAttributeDefinitionId(definition.getId());
            attributeValue.setValueText(value);
            attributeValue.setUpdatedSource(ContactAttributeValue.UpdatedSource.user);
            savedAttributes.add(attributeValueRepository.save(attributeValue));
        }

        return savedAttributes;
    }

    /**
     * FIX: Throws TagNotFoundException (semantically correct) instead of InvalidAttributeException.
     * Also throws ContactAccessDeniedException when tag belongs to a different org.
     */
    private List<ContactTagAssignment> assignTagsToContact(
            Long contactId,
            List<Long> tagIds,
            Long organizationId,
            Long userId
    ) {
        List<ContactTagAssignment> assignments = new ArrayList<>();

        for (Long tagId : tagIds) {
            ContactTag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new TagNotFoundException(tagId));   // FIX: was InvalidAttributeException

            if (!tag.getOrganizationId().equals(organizationId)) {
                throw new ContactAccessDeniedException("Tag " + tagId + " does not belong to this organization");
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

    private Specification<Contact> buildSpecification(ContactSearchRequest request, Long organizationId) {
        Specification<Contact> spec = Specification.where(
                ContactSpecifications.belongsToOrganization(organizationId));

        if (request.getSearch() != null && !request.getSearch().trim().isEmpty())
            spec = spec.and(ContactSpecifications.searchByNameOrPhone(request.getSearch()));

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty())
            spec = spec.and(ContactSpecifications.hasPhoneLike(request.getPhone()));

        if (request.getSource() != null && !request.getSource().trim().isEmpty())
            spec = spec.and(ContactSpecifications.hasSource(request.getSource()));

        if (request.getTagIds() != null && !request.getTagIds().isEmpty())
            spec = spec.and(ContactSpecifications.hasTags(request.getTagIds()));

        if (request.getCreatedAfter() != null || request.getCreatedBefore() != null)
            spec = spec.and(ContactSpecifications.createdBetween(request.getCreatedAfter(), request.getCreatedBefore()));

        if (request.getLastSeenAfter() != null || request.getLastSeenBefore() != null)
            spec = spec.and(ContactSpecifications.lastSeenBetween(request.getLastSeenAfter(), request.getLastSeenBefore()));

        if (request.getAttributeKey() != null && !request.getAttributeKey().trim().isEmpty())
            spec = spec.and(ContactSpecifications.hasAttribute(request.getAttributeKey(), request.getAttributeValue()));

        return spec;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}