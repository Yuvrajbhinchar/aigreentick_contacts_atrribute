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
 * Internal endpoints for the Messaging Service (broadcast campaign preparation).
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  SPEC CONSTRAINTS                                                       │
 * │  • Phone numbers: country code included, NO '+' prefix                  │
 * │    e.g. "919876543210" not "+919876543210"                              │
 * │  • All endpoints internal only — restrict at network/gateway level      │
 * │  • Target p99 < 2 s (synchronous during campaign prep)                  │
 * │  • 4xx → campaign marked FAILED                                         │
 * │  • 5xx → infrastructure failure, caller retries / alerts               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * ─────────────────────────────────────────────────────────────────────────
 * POST /internal/contacts/resolve
 *
 *   Request:
 *     { "projectId": 101, "phoneNumbers": ["919876543210", "918765432109"] }
 *
 *   Response 200:
 *     { "contacts": { "919876543210": 4001, "918765432109": 4002 } }
 *
 *   Rules:
 *     - Pure lookup — NO contacts are created or modified
 *     - Unknown phone numbers → OMITTED from the map
 *
 * ─────────────────────────────────────────────────────────────────────────
 * POST /internal/contacts/attributes
 *
 *   Request:
 *     {
 *       "projectId": 101,
 *       "phoneNumbers":   ["919876543210", "918765432109"],
 *       "attributeKeys":  ["first_name", "city", "account_type"]
 *     }
 *
 *   Response 200 (plain array, no envelope):
 *     [
 *       {
 *         "contactId": 4001,
 *         "phoneNumber": "919876543210",
 *         "attributes": [
 *           { "key": "first_name",   "value": "Ashish"  },
 *           { "key": "city",         "value": "Delhi"   },
 *           { "key": "account_type", "value": "premium" }
 *         ]
 *       }
 *     ]
 *
 *   Rules:
 *     - Lookup only — unknown phones OMITTED (NOT auto-created)
 *     - Missing attribute value → null  (spec: "null or omit, both fine")
 * ─────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@RestController
@RequestMapping("/internal/contacts")
@RequiredArgsConstructor
public class InternalContactController {

    private final InternalContactService internalContactService;

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

        // Exact response shape from spec: { "contacts": { "919876543210": 4001 } }
        return ResponseEntity.ok(Map.of("contacts", contacts));
    }

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
                request.getPhoneNumbers(),
                request.getAttributeKeys()
        );

        // Plain array response — no wrapper envelope (matches spec exactly)
        return ResponseEntity.ok(result);
    }
}