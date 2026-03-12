package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.dto.response.ContactInfo;
import com.aigreentick.services.contacts.entity.*;
import com.aigreentick.services.contacts.repository.*;
import com.aigreentick.services.contacts.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for bulk fetching contact attributes by phone numbers within a project.
 *
 * Flow steps:
 *  1. Normalize each phone → E.164
 *  2. Find existing contacts in org by phone
 *  3. Auto-create missing contacts + add them to project_contacts
 *  4. Ensure existing contacts are linked to the project (upsert project_contacts)
 *  5. Load attribute definitions for requested keys (auto-create missing ones)
 *  6. Load attribute values for all contacts
 *  7. Build response — if no value stored, return key name as value
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactBulkAttributeService {

    private final ContactRepository contactRepository;
    private final ContactAttributeValueRepository attributeValueRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final ProjectContactRepository projectContactRepository;

    /**
     * Main entry point.
     *
     * @param projectId      project scope (contacts are linked here)
     * @param organizationId org scope (multi-tenancy)
     * @param phoneNumbers   phones to look up / create
     * @param attributeKeys  attribute keys to fetch per contact
     * @return list of ContactInfo, one per phone number, in the same order as input
     */
    @Transactional
    public List<ContactInfo> getAttributes(
            Long projectId,
            Long organizationId,
            List<String> phoneNumbers,
            List<String> attributeKeys
    ) {
        log.info("Bulk attribute fetch — project: {}, org: {}, phones: {}, keys: {}",
                projectId, organizationId, phoneNumbers.size(), attributeKeys.size());

        // ── Step 1: Normalize phone numbers ──────────────────────────────────
        // Keep a map of normalizedPhone → originalPhone so we can preserve
        // the caller's original value in the response if normalization fails.
        Map<String, String> normalizedToOriginal = new LinkedHashMap<>();
        for (String raw : phoneNumbers) {
            try {
                String e164 = PhoneNumberUtil.normalizeToE164(raw);
                normalizedToOriginal.put(e164, raw);
            } catch (Exception ex) {
                log.warn("Could not normalize phone '{}', using as-is: {}", raw, ex.getMessage());
                // Keep the raw value; it will fail the DB lookup and be auto-created
                // or returned with fallback values.
                normalizedToOriginal.put(raw, raw);
            }
        }

        List<String> e164Phones = new ArrayList<>(normalizedToOriginal.keySet());

        // ── Step 2: Batch-load existing contacts for this org ─────────────────
        Map<String, Contact> phoneToContact = new HashMap<>();
        for (String e164 : e164Phones) {
            contactRepository
                    .findByOrganizationIdAndWaPhoneE164(organizationId, e164)
                    .ifPresent(c -> phoneToContact.put(e164, c));
        }

        // ── Step 3: Auto-create missing contacts + link to project ────────────
        for (String e164 : e164Phones) {
            if (!phoneToContact.containsKey(e164)) {
                Contact newContact = createContact(e164, organizationId);
                phoneToContact.put(e164, newContact);
                log.debug("Auto-created contact {} for phone {}", newContact.getId(), e164);
            }

            // Upsert project_contacts (link contact to project if not already linked)
            Contact contact = phoneToContact.get(e164);
            linkContactToProject(contact.getId(), projectId);
        }

        // ── Step 4: Resolve attribute definitions ─────────────────────────────
        // Load existing definitions for the requested keys in this org
        Map<String, AttributeDefinition> keyToDefinition =
                loadOrCreateAttributeDefinitions(organizationId, attributeKeys);

        // ── Step 5: Batch-load all attribute values for these contacts ─────────
        List<Long> contactIds = e164Phones.stream()
                .map(p -> phoneToContact.get(p).getId())
                .collect(Collectors.toList());

        List<ContactAttributeValue> allValues =
                attributeValueRepository.findByContactIdIn(contactIds);

        // Index: contactId → (attributeDefinitionId → value)
        Map<Long, Map<Long, ContactAttributeValue>> valueIndex = new HashMap<>();
        for (ContactAttributeValue v : allValues) {
            valueIndex
                    .computeIfAbsent(v.getContactId(), k -> new HashMap<>())
                    .put(v.getAttributeDefinitionId(), v);
        }

        // ── Step 6: Build response ────────────────────────────────────────────
        List<ContactInfo> result = new ArrayList<>();

        for (String e164 : e164Phones) {
            Contact contact = phoneToContact.get(e164);
            Map<Long, ContactAttributeValue> contactValues =
                    valueIndex.getOrDefault(contact.getId(), Collections.emptyMap());

            List<ContactInfo.Attribute> attributes = new ArrayList<>();

            for (String key : attributeKeys) {
                AttributeDefinition def = keyToDefinition.get(key);

                String value;
                if (def == null) {
                    // Definition could not be found or created — return key as value
                    value = key;
                } else {
                    ContactAttributeValue attrValue = contactValues.get(def.getId());
                    if (attrValue == null) {
                        // No value stored for this contact → return key name as value
                        value = key;
                    } else {
                        value = extractValue(attrValue, key);
                    }
                }

                attributes.add(ContactInfo.Attribute.builder()
                        .key(key)
                        .value(value)
                        .build());
            }

            result.add(ContactInfo.builder()
                    .contactId(contact.getId())
                    .phoneNumber(e164)           // return normalized E.164 phone
                    .attributes(attributes)
                    .build());
        }

        log.info("Bulk attribute fetch complete — returned {} contacts", result.size());
        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Create a new contact using the phone number as display name.
     */
    private Contact createContact(String e164Phone, Long organizationId) {
        Contact contact = new Contact();
        contact.setOrganizationId(organizationId);
        contact.setWaPhoneE164(e164Phone);
        contact.setWaId(PhoneNumberUtil.generateWhatsAppId(e164Phone));
        contact.setDisplayName("Unknown");           // phone as name per spec
        contact.setSource(Contact.Source.INTEGRATION);
        contact.setFirstSeenAt(LocalDateTime.now());
        return contactRepository.save(contact);
    }

    /**
     * Upsert a row in project_contacts for the given contact + project.
     * Does nothing if the link already exists.
     */
    private void linkContactToProject(Long contactId, Long projectId) {
        boolean alreadyLinked =
                projectContactRepository.existsByProjectIdAndContactId(projectId, contactId);

        if (!alreadyLinked) {
            ProjectContact pc = new ProjectContact();
            pc.setProjectId(projectId);
            pc.setContactId(contactId);
            pc.setLastMessageAt(LocalDateTime.now());
            pc.setUnreadCount(0);
            projectContactRepository.save(pc);
            log.debug("Linked contact {} to project {}", contactId, projectId);
        }
    }

    /**
     * For each requested attribute key:
     *   - Return existing AttributeDefinition if found
     *   - Create a new one (text type, user_defined) if missing
     *
     * Returns a map of key → AttributeDefinition (never null values for successfully
     * resolved keys).
     */
    private Map<String, AttributeDefinition> loadOrCreateAttributeDefinitions(
            Long organizationId,
            List<String> attributeKeys
    ) {
        // Batch-load all existing definitions for these keys
        List<AttributeDefinition> existing =
                attributeDefinitionRepository.findByOrganizationIdAndAttrKeyIn(
                        organizationId, attributeKeys);

        Map<String, AttributeDefinition> result = new HashMap<>();
        for (AttributeDefinition def : existing) {
            result.put(def.getAttrKey(), def);
        }

        // Auto-create any that are missing
        for (String key : attributeKeys) {
            if (!result.containsKey(key)) {
                try {
                    AttributeDefinition def = new AttributeDefinition();
                    def.setOrganizationId(organizationId);
                    def.setAttrKey(key);
                    def.setLabel(capitalize(key));
                    def.setCategory(AttributeDefinition.Category.user_defined);
                    def.setDataType(AttributeDefinition.DataType.text);
                    def.setIsEditable(true);
                    def.setIsRequired(false);
                    def.setIsSearchable(true);
                    def = attributeDefinitionRepository.save(def);
                    result.put(key, def);
                    log.debug("Auto-created attribute definition: {}", key);
                } catch (Exception ex) {
                    // Race condition — another thread may have created it; try to reload
                    log.warn("Could not create attribute definition '{}': {}", key, ex.getMessage());
                    attributeDefinitionRepository
                            .findByOrganizationIdAndAttrKey(organizationId, key)
                            .ifPresent(d -> result.put(key, d));
                }
            }
        }

        return result;
    }

    /**
     * Extract the stored string value from a ContactAttributeValue.
     * Falls back to the key name if every field is null/empty.
     */
    private String extractValue(ContactAttributeValue attr, String fallback) {
        if (attr.getValueText() != null && !attr.getValueText().isEmpty()) {
            return attr.getValueText();
        }
        if (attr.getValueNumber() != null) {
            return attr.getValueNumber().toString();
        }
        if (attr.getValueDecimal() != null) {
            return attr.getValueDecimal().toPlainString();
        }
        if (attr.getValueBool() != null) {
            return attr.getValueBool().toString();
        }
        if (attr.getValueDate() != null) {
            return attr.getValueDate().toString();
        }
        if (attr.getValueDatetime() != null) {
            return attr.getValueDatetime().toString();
        }
        if (attr.getValueJson() != null && !attr.getValueJson().isEmpty()) {
            return attr.getValueJson();
        }
        // Nothing stored → return key name as value (per spec)
        return fallback;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}