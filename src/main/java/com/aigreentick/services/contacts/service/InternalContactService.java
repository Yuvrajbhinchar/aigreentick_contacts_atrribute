package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.dto.response.ContactInfo;
import com.aigreentick.services.contacts.entity.Contact;
import com.aigreentick.services.contacts.repository.ContactRepository;
import com.aigreentick.services.contacts.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Serves the two internal endpoints consumed by the Messaging Service:
 *
 *   POST /internal/contacts/resolve
 *     → pure lookup, no side-effects, unknown phones are omitted from the result
 *
 *   POST /internal/contacts/attributes
 *     → delegates to ContactBulkAttributeService which auto-creates missing contacts
 *       and links them to the project (full spec behaviour, same as /api/v1/contacts/bulk-attributes)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InternalContactService {

    private final ContactRepository contactRepository;
    private final ContactBulkAttributeService contactBulkAttributeService;

    /**
     * Resolve phone numbers → contactId map.
     *
     * Contract (from inter-service API doc):
     *  - Input:  { projectId, phoneNumbers: ["919876543210", ...] }
     *  - Output: { contacts: { "919876543210": 4001, "918765432109": 4002 } }
     *  - Phones that don't exist in the org are OMITTED (no auto-create, no error)
     *
     * @param organizationId  resolved from X-Organization-ID header
     * @param phoneNumbers    raw phone strings (10-digit or E.164)
     * @return map of normalised E.164 phone → contactId for phones that exist
     */
    @Transactional(readOnly = true)
    public Map<String, Long> resolveContacts(Long organizationId, List<String> phoneNumbers) {
        log.info("Resolve contacts — org: {}, phones: {}", organizationId, phoneNumbers.size());

        Map<String, Long> result = new LinkedHashMap<>();

        for (String raw : phoneNumbers) {
            String e164;
            try {
                e164 = PhoneNumberUtil.normalizeToE164(raw);
            } catch (Exception ex) {
                // Can't normalize → treat as unknown, omit per spec
                log.debug("Cannot normalize '{}', omitting from resolve result: {}", raw, ex.getMessage());
                continue;
            }

            // Look up contact — if not found, just omit (per spec: "Omit unknown phone numbers")
            contactRepository
                    .findByOrganizationIdAndWaPhoneE164(organizationId, e164)
                    .ifPresent(contact -> result.put(e164, contact.getId()));
        }

        log.info("Resolve complete — found {}/{} phones", result.size(), phoneNumbers.size());
        return result;
    }

    /**
     * Bulk-fetch contact attributes.
     *
     * Contract (from inter-service API doc):
     *  - Input:  { projectId, phoneNumbers, attributeKeys }
     *  - Output: [ { contactId, phoneNumber, attributes: [{key, value}] } ]
     *  - Missing contacts are auto-created and linked to the project (same as bulk-attributes)
     *
     * Delegates entirely to the existing ContactBulkAttributeService so all business rules
     * (auto-create, auto-link, fallback key-as-value) are applied consistently.
     */
    @Transactional
    public List<ContactInfo> getAttributes(
            Long organizationId,
            Long projectId,
            List<String> phoneNumbers,
            List<String> attributeKeys
    ) {
        return contactBulkAttributeService.getAttributes(projectId, organizationId, phoneNumbers, attributeKeys);
    }
}