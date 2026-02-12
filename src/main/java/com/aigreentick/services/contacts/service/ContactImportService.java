package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.dto.request.ContactImportRequest;
import com.aigreentick.services.contacts.dto.response.ContactImportResponse;
import com.aigreentick.services.contacts.entity.*;
import com.aigreentick.services.contacts.repository.*;
import com.aigreentick.services.contacts.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for bulk import/export operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactImportService {

    private final ContactRepository contactRepository;
    private final ContactAttributeValueRepository attributeValueRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;

    /**
     * Import contacts in bulk
     * Handles create, update, and error tracking
     */
    @Transactional
    public ContactImportResponse importContacts(
            ContactImportRequest request,
            Long organizationId
    ) {

        log.info("Starting bulk import for organization: {}, count: {}",
                organizationId, request.getContacts().size());

        int totalProcessed = 0;
        int successCount = 0;
        int failedCount = 0;
        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;

        List<ContactImportResponse.ImportError> errors = new ArrayList<>();

        // Pre-load existing contacts by phone for this organization
        Map<String, Contact> existingContactsMap = new HashMap<>();
        List<Contact> existingContacts = contactRepository.findByOrganizationId(organizationId);
        for (Contact contact : existingContacts) {
            existingContactsMap.put(contact.getWaPhoneE164(), contact);
        }

        // Pre-load all attribute definitions for this organization
        Map<String, AttributeDefinition> attributeDefinitionsMap = new HashMap<>();
        List<AttributeDefinition> existingDefinitions =
                attributeDefinitionRepository.findByOrganizationId(organizationId);
        for (AttributeDefinition def : existingDefinitions) {
            attributeDefinitionsMap.put(def.getAttrKey(), def);
        }

        int rowNumber = 1; // Start from 1 (header is row 0)

        for (ContactImportRequest.ContactImportItem item : request.getContacts()) {
            rowNumber++;
            totalProcessed++;

            try {
                // 1. Validate and normalize phone number
                String e164Phone;
                try {
                    e164Phone = PhoneNumberUtil.normalizeToE164(item.getPhoneNumber());
                } catch (Exception e) {
                    errors.add(ContactImportResponse.ImportError.builder()
                            .rowNumber(rowNumber)
                            .phoneNumber(item.getPhoneNumber())
                            .name(item.getName())
                            .errorMessage("Invalid phone number format: " + e.getMessage())
                            .errorType("VALIDATION")
                            .build());
                    failedCount++;
                    continue;
                }

                // 2. Check if contact exists
                Contact contact = existingContactsMap.get(e164Phone);

                if (contact != null) {
                    // Contact exists
                    if (request.getUpdateExisting()) {
                        // Update existing contact
                        contact.setDisplayName(item.getName());
                        contact = contactRepository.save(contact);

                        // Update attributes
                        updateContactAttributes(
                                contact.getId(),
                                item.getAttributes(),
                                organizationId,
                                attributeDefinitionsMap,
                                request.getCreateNewAttributes()
                        );

                        updatedCount++;
                        successCount++;

                        log.debug("Updated contact: {} ({})", contact.getId(), e164Phone);

                    } else {
                        // Skip existing contact
                        skippedCount++;
                        log.debug("Skipped existing contact: {}", e164Phone);
                    }

                } else {
                    // Create new contact
                    contact = new Contact();
                    contact.setOrganizationId(organizationId);
                    contact.setWaPhoneE164(e164Phone);
                    contact.setWaId(PhoneNumberUtil.generateWhatsAppId(e164Phone));
                    contact.setDisplayName(item.getName());
                    contact.setSource(Contact.Source.import_);
                    contact.setFirstSeenAt(LocalDateTime.now());

                    contact = contactRepository.save(contact);
                    existingContactsMap.put(e164Phone, contact); // Add to map for subsequent rows

                    // Create attributes
                    if (item.getAttributes() != null && !item.getAttributes().isEmpty()) {
                        createContactAttributes(
                                contact.getId(),
                                item.getAttributes(),
                                organizationId,
                                attributeDefinitionsMap,
                                request.getCreateNewAttributes()
                        );
                    }

                    createdCount++;
                    successCount++;

                    log.debug("Created contact: {} ({})", contact.getId(), e164Phone);
                }

            } catch (Exception e) {
                log.error("Error importing row {}: {}", rowNumber, e.getMessage(), e);

                errors.add(ContactImportResponse.ImportError.builder()
                        .rowNumber(rowNumber)
                        .phoneNumber(item.getPhoneNumber())
                        .name(item.getName())
                        .errorMessage("System error: " + e.getMessage())
                        .errorType("SYSTEM")
                        .build());

                failedCount++;
            }
        }

        log.info("Import completed - Total: {}, Success: {}, Failed: {}, Created: {}, Updated: {}, Skipped: {}",
                totalProcessed, successCount, failedCount, createdCount, updatedCount, skippedCount);

        return ContactImportResponse.builder()
                .totalProcessed(totalProcessed)
                .successCount(successCount)
                .failedCount(failedCount)
                .createdCount(createdCount)
                .updatedCount(updatedCount)
                .skippedCount(skippedCount)
                .errors(errors)
                .build();
    }

    /**
     * Helper: Create attributes for a contact during import
     */
    private void createContactAttributes(
            Long contactId,
            Map<String, String> attributes,
            Long organizationId,
            Map<String, AttributeDefinition> attributeDefinitionsMap,
            boolean createNewAttributes
    ) {

        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey().trim();
            String value = entry.getValue();

            if (key.isEmpty() || value == null || value.trim().isEmpty()) {
                continue;
            }

            // Find or create attribute definition
            AttributeDefinition definition = attributeDefinitionsMap.get(key);

            if (definition == null) {
                if (createNewAttributes) {
                    // Create new attribute definition
                    definition = new AttributeDefinition();
                    definition.setOrganizationId(organizationId);
                    definition.setAttrKey(key);
                    definition.setLabel(capitalize(key));
                    definition.setCategory(AttributeDefinition.Category.user_defined);
                    definition.setDataType(AttributeDefinition.DataType.text);
                    definition.setIsEditable(true);
                    definition.setIsRequired(false);
                    definition.setIsSearchable(true);

                    definition = attributeDefinitionRepository.save(definition);
                    attributeDefinitionsMap.put(key, definition); // Add to cache

                    log.debug("Created new attribute definition: {}", key);
                } else {
                    log.warn("Attribute definition not found and creation disabled: {}", key);
                    continue;
                }
            }

            // Create attribute value
            ContactAttributeValue attributeValue = new ContactAttributeValue();
            attributeValue.setContactId(contactId);
            attributeValue.setAttributeDefinitionId(definition.getId());
            attributeValue.setValueText(value);
            attributeValue.setUpdatedSource(ContactAttributeValue.UpdatedSource.integration);

            attributeValueRepository.save(attributeValue);
        }
    }

    /**
     * Helper: Update attributes for existing contact during import
     */
    private void updateContactAttributes(
            Long contactId,
            Map<String, String> attributes,
            Long organizationId,
            Map<String, AttributeDefinition> attributeDefinitionsMap,
            boolean createNewAttributes
    ) {

        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        // Load existing attributes
        List<ContactAttributeValue> existingAttributes =
                attributeValueRepository.findByContactId(contactId);

        Map<Long, ContactAttributeValue> existingAttributesMap = new HashMap<>();
        for (ContactAttributeValue attr : existingAttributes) {
            existingAttributesMap.put(attr.getAttributeDefinitionId(), attr);
        }

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey().trim();
            String value = entry.getValue();

            if (key.isEmpty() || value == null || value.trim().isEmpty()) {
                continue;
            }

            // Find or create attribute definition
            AttributeDefinition definition = attributeDefinitionsMap.get(key);

            if (definition == null) {
                if (createNewAttributes) {
                    definition = new AttributeDefinition();
                    definition.setOrganizationId(organizationId);
                    definition.setAttrKey(key);
                    definition.setLabel(capitalize(key));
                    definition.setCategory(AttributeDefinition.Category.user_defined);
                    definition.setDataType(AttributeDefinition.DataType.text);
                    definition.setIsEditable(true);
                    definition.setIsRequired(false);
                    definition.setIsSearchable(true);

                    definition = attributeDefinitionRepository.save(definition);
                    attributeDefinitionsMap.put(key, definition);
                } else {
                    continue;
                }
            }

            // Update or create attribute value
            ContactAttributeValue attributeValue = existingAttributesMap.get(definition.getId());

            if (attributeValue != null) {
                // Update existing
                attributeValue.setValueText(value);
                attributeValue.setUpdatedSource(ContactAttributeValue.UpdatedSource.integration);
                attributeValueRepository.save(attributeValue);
            } else {
                // Create new
                attributeValue = new ContactAttributeValue();
                attributeValue.setContactId(contactId);
                attributeValue.setAttributeDefinitionId(definition.getId());
                attributeValue.setValueText(value);
                attributeValue.setUpdatedSource(ContactAttributeValue.UpdatedSource.integration);
                attributeValueRepository.save(attributeValue);
            }
        }
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