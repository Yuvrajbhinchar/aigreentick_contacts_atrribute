package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.dto.response.ContactInfo;
import com.aigreentick.services.contacts.entity.AttributeDefinition;
import com.aigreentick.services.contacts.entity.Contact;
import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import com.aigreentick.services.contacts.repository.AttributeDefinitionRepository;
import com.aigreentick.services.contacts.repository.ContactAttributeValueRepository;
import com.aigreentick.services.contacts.repository.ContactRepository;
import com.aigreentick.services.contacts.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Serves the two internal endpoints consumed by the Messaging Service.
 *
 * Key spec constraints (from inter-service API contract):
 *
 *  1. Phone numbers are strings with country code but NO '+' prefix.
 *     Input:  "919876543210"
 *     Output: "919876543210"   ← same format, no '+'
 *
 *  2. /resolve  → pure lookup, NO side-effects, unknown phones OMITTED.
 *
 *  3. /attributes → lookup only (NO auto-create), unknown phones OMITTED,
 *                   missing attribute value → null (or omit — both fine per spec).
 *
 * Both endpoints differ fundamentally from the public /api/v1/contacts/bulk-attributes
 * which auto-creates missing contacts. Do NOT delegate to ContactBulkAttributeService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalContactService {

    private final ContactRepository contactRepository;
    private final ContactAttributeValueRepository attributeValueRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;

    // ── 1. Resolve ────────────────────────────────────────────────────────────

    /**
     * Resolve phone numbers → contactId map.
     *
     * Contract:
     *   Input  phones: "919876543210"  (no '+')
     *   Output phones: "919876543210"  (no '+', same as input)
     *   Unknown phones → silently omitted from the result map.
     *   No contacts are created or modified.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> resolveContacts(Long organizationId, List<String> rawPhones) {
        log.info("Internal resolve — org: {}, phones: {}", organizationId, rawPhones.size());

        // Preserve original input string as the map key so the caller gets back
        // exactly the format they sent (no '+').
        Map<String, Long> result = new LinkedHashMap<>();

        for (String raw : rawPhones) {
            String e164 = toE164(raw);
            if (e164 == null) {
                log.debug("Cannot normalise '{}', omitting", raw);
                continue;
            }

            contactRepository
                    .findByOrganizationIdAndWaPhoneE164(organizationId, e164)
                    .ifPresent(contact -> result.put(stripPlus(e164), contact.getId()));
        }

        log.info("Resolve complete — matched {}/{}", result.size(), rawPhones.size());
        return result;
    }

    // ── 2. Attributes ─────────────────────────────────────────────────────────

    /**
     * Bulk-fetch contact attributes — lookup only, no side-effects.
     *
     * Contract:
     *   - Unknown phones → OMITTED from the response array.
     *   - Known phones with no stored value for an attribute key → value is null.
     *   - phoneNumber in the response has NO '+' prefix.
     */
    @Transactional(readOnly = true)
    public List<ContactInfo> getAttributes(
            Long organizationId,
            List<String> rawPhones,
            List<String> attributeKeys
    ) {
        log.info("Internal attributes — org: {}, phones: {}, keys: {}",
                organizationId, rawPhones.size(), attributeKeys.size());

        // ── Step 1: Normalize + look up only existing contacts ─────────────
        // Build ordered map: normalised E.164 → Contact (only known ones)
        Map<String, Contact> e164ToContact = new LinkedHashMap<>();
        for (String raw : rawPhones) {
            String e164 = toE164(raw);
            if (e164 == null) {
                log.debug("Cannot normalise '{}', omitting", raw);
                continue;
            }
            contactRepository
                    .findByOrganizationIdAndWaPhoneE164(organizationId, e164)
                    .ifPresent(c -> e164ToContact.put(e164, c));
        }

        if (e164ToContact.isEmpty()) {
            return Collections.emptyList();
        }

        // ── Step 2: Batch-load attribute definitions for requested keys ────
        List<AttributeDefinition> defs =
                attributeDefinitionRepository.findByOrganizationIdAndAttrKeyIn(
                        organizationId, attributeKeys);

        // key → definition  (only keys that actually exist in the org)
        Map<String, AttributeDefinition> keyToDef = defs.stream()
                .collect(Collectors.toMap(AttributeDefinition::getAttrKey, d -> d));

        // ── Step 3: Batch-load all attribute values for known contacts ─────
        List<Long> contactIds = e164ToContact.values().stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

        List<ContactAttributeValue> allValues =
                attributeValueRepository.findByContactIdIn(contactIds);

        // contactId → (attributeDefinitionId → value)
        Map<Long, Map<Long, ContactAttributeValue>> valueIndex = new HashMap<>();
        for (ContactAttributeValue v : allValues) {
            valueIndex
                    .computeIfAbsent(v.getContactId(), k -> new HashMap<>())
                    .put(v.getAttributeDefinitionId(), v);
        }

        // ── Step 4: Build response ─────────────────────────────────────────
        List<ContactInfo> result = new ArrayList<>();

        for (Map.Entry<String, Contact> entry : e164ToContact.entrySet()) {
            String e164 = entry.getKey();
            Contact contact = entry.getValue();
            Map<Long, ContactAttributeValue> contactValues =
                    valueIndex.getOrDefault(contact.getId(), Collections.emptyMap());

            List<ContactInfo.Attribute> attributes = new ArrayList<>();

            for (String key : attributeKeys) {
                AttributeDefinition def = keyToDef.get(key);

                String value = null; // default: null (per spec "missing → null or omit, both fine")
                if (def != null) {
                    ContactAttributeValue attrValue = contactValues.get(def.getId());
                    if (attrValue != null) {
                        value = extractValue(attrValue); // null if all fields empty
                    }
                }

                attributes.add(ContactInfo.Attribute.builder()
                        .key(key)
                        .value(value)   // null when not stored
                        .build());
            }

            result.add(ContactInfo.builder()
                    .contactId(contact.getId())
                    .phoneNumber(stripPlus(e164))   // no '+' in response
                    .attributes(attributes)
                    .build());
        }

        log.info("Internal attributes complete — returned {}/{} contacts",
                result.size(), rawPhones.size());
        return result;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Normalise a raw phone string to E.164.
     * Accepts "919876543210" (no '+'), "+919876543210", or "9876543210" (10-digit).
     * Returns null if the number cannot be normalised.
     */
    private String toE164(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            // PhoneNumberUtil.normalizeToE164 handles:
            //   "+919876543210" → "+919876543210"
            //   "919876543210"  → "+919876543210"  (12-digit starting with 91)
            //   "9876543210"    → "+919876543210"  (10-digit → default country code)
            return PhoneNumberUtil.normalizeToE164(raw);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Strip leading '+' from an E.164 string.
     * "+919876543210" → "919876543210"
     */
    private String stripPlus(String e164) {
        return e164.startsWith("+") ? e164.substring(1) : e164;
    }

    /**
     * Extract the stored value from a ContactAttributeValue.
     * Returns null (not a fallback string) when every field is empty —
     * matching the spec: "missing attribute value → null or omit, both fine".
     */
    private String extractValue(ContactAttributeValue attr) {
        if (attr.getValueText() != null && !attr.getValueText().isEmpty())
            return attr.getValueText();
        if (attr.getValueNumber() != null)
            return attr.getValueNumber().toString();
        if (attr.getValueDecimal() != null)
            return attr.getValueDecimal().toPlainString();
        if (attr.getValueBool() != null)
            return attr.getValueBool().toString();
        if (attr.getValueDate() != null)
            return attr.getValueDate().toString();
        if (attr.getValueDatetime() != null)
            return attr.getValueDatetime().toString();
        if (attr.getValueJson() != null && !attr.getValueJson().isEmpty())
            return attr.getValueJson();
        return null; // ← null, not the key name (that was the public-API fallback behaviour)
    }
}