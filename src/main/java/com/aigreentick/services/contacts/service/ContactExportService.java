package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.entity.Contact;
import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import com.aigreentick.services.contacts.entity.AttributeDefinition;
import com.aigreentick.services.contacts.repository.*;
import com.aigreentick.services.contacts.util.CSVUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for exporting contacts
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactExportService {

    private final ContactRepository contactRepository;
    private final ContactAttributeValueRepository attributeValueRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;

    /**
     * Export all contacts to CSV format
     */
    @Transactional(readOnly = true)
    public String exportContactsToCSV(Long organizationId) {

        log.info("Exporting contacts to CSV for organization: {}", organizationId);

        // 1. Load all contacts for organization
        List<Contact> contacts = contactRepository.findByOrganizationId(organizationId);

        if (contacts.isEmpty()) {
            return CSVUtil.generateSampleCSV(); // Return sample if no contacts
        }

        // 2. Load all attributes for these contacts
        List<Long> contactIds = contacts.stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

        List<ContactAttributeValue> allAttributes =
                attributeValueRepository.findByContactIdIn(contactIds);

        // Group by contact ID
        Map<Long, List<ContactAttributeValue>> attributesByContact =
                allAttributes.stream()
                        .collect(Collectors.groupingBy(ContactAttributeValue::getContactId));

        // 3. Load attribute definitions
        Set<Long> attrDefIds = allAttributes.stream()
                .map(ContactAttributeValue::getAttributeDefinitionId)
                .collect(Collectors.toSet());

        Map<Long, AttributeDefinition> attributeDefinitions =
                attributeDefinitionRepository.findAllById(attrDefIds).stream()
                        .collect(Collectors.toMap(AttributeDefinition::getId, def -> def));

        // 4. Build contact maps (for CSV export)
        List<Map<String, String>> contactMaps = new ArrayList<>();

        for (Contact contact : contacts) {
            Map<String, String> contactMap = new HashMap<>();

            // Add basic fields
            contactMap.put("phone_number", contact.getWaPhoneE164().replace("+91", "")); // Remove country code for export
            contactMap.put("name", contact.getDisplayName());

            // Add attributes
            List<ContactAttributeValue> contactAttrs =
                    attributesByContact.getOrDefault(contact.getId(), new ArrayList<>());

            for (ContactAttributeValue attr : contactAttrs) {
                AttributeDefinition def = attributeDefinitions.get(attr.getAttributeDefinitionId());
                if (def != null) {
                    String value = extractAttributeValue(attr);
                    contactMap.put(def.getAttrKey(), value);
                }
            }

            contactMaps.add(contactMap);
        }

        // 5. Generate CSV
        String csv = CSVUtil.exportToCSV(contactMaps);

        log.info("Exported {} contacts to CSV", contacts.size());

        return csv;
    }

    /**
     * Extract value from ContactAttributeValue
     */
    private String extractAttributeValue(ContactAttributeValue attr) {
        if (attr.getValueText() != null && !attr.getValueText().isEmpty()) {
            return attr.getValueText();
        }
        if (attr.getValueNumber() != null) {
            return attr.getValueNumber().toString();
        }
        if (attr.getValueDecimal() != null) {
            return attr.getValueDecimal().toString();
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
        return "";
    }
}