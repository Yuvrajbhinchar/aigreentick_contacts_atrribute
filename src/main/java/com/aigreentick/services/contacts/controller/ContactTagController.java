package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.entity.ContactTag;
import com.aigreentick.services.contacts.service.ContactTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact-tags")
@RequiredArgsConstructor
public class ContactTagController {

    private final ContactTagService contactTagService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ContactTag contactTag) {
        ContactTag saved = contactTagService.create(contactTag);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<ContactTag> contactTags = contactTagService.getAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", contactTags.size(),
                "data", contactTags
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactTagService.getById(id)
        ));
    }

    // READ BY ORGANIZATION
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<?> getByOrganizationId(@PathVariable Long organizationId) {
        List<ContactTag> tags = contactTagService.getByOrganizationId(organizationId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", tags.size(),
                "data", tags
        ));
    }

    // READ BY ORGANIZATION AND ACTIVE STATUS
    @GetMapping("/organization/{organizationId}/active/{isActive}")
    public ResponseEntity<?> getByOrganizationIdAndIsActive(
            @PathVariable Long organizationId,
            @PathVariable Boolean isActive) {
        List<ContactTag> tags = contactTagService.getByOrganizationIdAndIsActive(organizationId, isActive);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", tags.size(),
                "data", tags
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ContactTag contactTag) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactTagService.update(id, contactTag)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        contactTagService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ContactTag deleted successfully"
        ));
    }
}