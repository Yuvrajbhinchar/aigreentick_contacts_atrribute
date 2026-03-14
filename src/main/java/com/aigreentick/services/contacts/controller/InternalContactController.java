package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.dto.request.InternalAttributeRequest;
import com.aigreentick.services.contacts.dto.request.InternalResolveRequest;
import com.aigreentick.services.contacts.dto.response.ContactInfo;
import com.aigreentick.services.contacts.service.InternalContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Internal endpoints consumed by the Messaging Service during broadcast campaign preparation.
 *
 * These routes are NOT exposed publicly — they live under /internal/** which should be
 * network-restricted (e.g. only reachable from within the cluster / VPC).
 *
 * They intentionally bypass the public /api/v1 versioning and do NOT go through
 * OrganizationInterceptor's ThreadLocal; the organizationId is read directly from the
 * X-Organization-ID header in each method.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. POST /internal/contacts/resolve
 *    Resolve phone numbers → contact IDs.  Omits unknown phones — no auto-create.
 *
 *    Request:
 *      { "projectId": 101, "phoneNumbers": ["919876543210", "918765432109"] }
 *
 *    Response 200:
 *      { "contacts": { "919876543210": 4001, "918765432109": 4002 } }
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * 2. POST /internal/contacts/attributes
 *    Bulk-fetch contact attributes.  Auto-creates missing contacts + links to project.
 *
 *    Request:
 *      {
 *        "projectId": 101,
 *        "phoneNumbers": ["919876543210", "918765432109"],
 *        "attributeKeys": ["first_name", "city", "account_type"]
 *      }
 *
 *    Response 200:
 *      [
 *        {
 *          "contactId": 4001,
 *          "phoneNumber": "919876543210",
 *          "attributes": [
 *            { "key": "first_name", "value": "Ashish" },
 *            { "key": "city",       "value": "Delhi"  },
 *            { "key": "account_type", "value": "premium" }
 *          ]
 *        }
 *      ]
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@RestController
@RequestMapping("/internal/contacts")
@RequiredArgsConstructor
public class InternalContactController {

    private final InternalContactService internalContactService;

    /**
     * Endpoint 1 — resolve phones to contact IDs.
     *
     * Unknown phones are silently omitted from the map (per contract).
     * No contacts are created or modified.
     */
    @PostMapping("/resolve")
    public ResponseEntity<Map<String, Object>> resolveContacts(
            @Valid @RequestBody InternalResolveRequest request,
            @RequestHeader("X-Organization-ID") Long organizationId
    ) {
        log.info("Internal resolve — org: {}, project: {}, phones: {}",
                organizationId, request.getProjectId(), request.getPhoneNumbers().size());

        Map<String, Long> contacts = internalContactService.resolveContacts(
                organizationId,
                request.getPhoneNumbers()
        );

        // Response shape: { "contacts": { "919876543210": 4001, ... } }
        return ResponseEntity.ok(Map.of("contacts", contacts));
    }

    /**
     * Endpoint 2 — bulk-fetch attributes (auto-creates missing contacts + project links).
     *
     * Response is a flat JSON array, not wrapped in a "data" envelope,
     * matching the inter-service contract exactly.
     */
    @PostMapping("/attributes")
    public ResponseEntity<List<ContactInfo>> getAttributes(
            @Valid @RequestBody InternalAttributeRequest request,
            @RequestHeader("X-Organization-ID") Long organizationId
    ) {
        log.info("Internal attributes — org: {}, project: {}, phones: {}, keys: {}",
                organizationId, request.getProjectId(),
                request.getPhoneNumbers().size(), request.getAttributeKeys().size());

        List<ContactInfo> result = internalContactService.getAttributes(
                organizationId,
                request.getProjectId(),
                request.getPhoneNumbers(),
                request.getAttributeKeys()
        );

        // Response is a plain array — no wrapper envelope
        return ResponseEntity.ok(result);
    }
}