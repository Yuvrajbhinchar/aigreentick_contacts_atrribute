package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.dto.request.BulkContactAttributeRequest;
import com.aigreentick.services.contacts.dto.response.ContactInfo;
import com.aigreentick.services.contacts.service.ContactBulkAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactBulkAttributeController {

    private final ContactBulkAttributeService contactBulkAttributeService;

    /**
     * Bulk fetch contact attributes by phone numbers within a project..
     *
     * - If a phone number does not exist → contact is auto-created and linked to the project.
     * - If a phone number exists but is not in the project → it is linked automatically.
     * - If an attribute key has no stored value → the key name itself is returned as the value.
     * - If an attribute definition does not exist → it is auto-created (text type).
     *
     * Headers:
     *   X-Organization-ID  — required, org scope
     *   X-Project-ID       — required, project scope
     *
     * Request body:
     * {
     *   "phoneNumbers": ["9876543210", "+919876543211"],
     *   "attributeKeys": ["city", "age", "company"]
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "contactId": 1,
     *       "phoneNumber": "+919876543210",
     *       "attributes": [
     *         { "key": "city",    "value": "Delhi"   },
     *         { "key": "age",     "value": "age"     },  ← no value stored, key used as value
     *         { "key": "company", "value": "company" }   ← no value stored, key used as value
     *       ]
     *     }
     *   ]
     * }
     */
    @PostMapping("/bulk-attributes")
    public ResponseEntity<Map<String, Object>> getBulkContactAttributes(
            @Valid @RequestBody BulkContactAttributeRequest request,
            @RequestHeader(value = "X-Organization-ID") Long organizationId,
            @RequestHeader(value = "X-Project-ID") Long projectId
    ) {
        log.info("Bulk attribute request — org: {}, project: {}, phones: {}, keys: {}",
                organizationId, projectId,
                request.getPhoneNumbers().size(),
                request.getAttributeKeys().size());

        List<ContactInfo> contacts = contactBulkAttributeService.getAttributes(
                projectId,
                organizationId,
                request.getPhoneNumbers(),
                request.getAttributeKeys()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", contacts.size());
        response.put("data", contacts);

        return ResponseEntity.ok(response);
    }
}