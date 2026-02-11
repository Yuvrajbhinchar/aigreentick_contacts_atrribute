package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.entity.ContactTagAssignment;
import com.aigreentick.services.contacts.service.ContactTagAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact-tag-assignments")
@RequiredArgsConstructor
public class ContactTagAssignmentController {

    private final ContactTagAssignmentService contactTagAssignmentService;

    // CREATE (Assign tag to contact)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ContactTagAssignment contactTagAssignment) {
        ContactTagAssignment saved = contactTagAssignmentService.create(contactTagAssignment);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<ContactTagAssignment> assignments = contactTagAssignmentService.getAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", assignments.size(),
                "data", assignments
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactTagAssignmentService.getById(id)
        ));
    }

    // READ BY CONTACT
    @GetMapping("/contact/{contactId}")
    public ResponseEntity<?> getByContactId(@PathVariable Long contactId) {
        List<ContactTagAssignment> assignments = contactTagAssignmentService.getByContactId(contactId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", assignments.size(),
                "data", assignments
        ));
    }

    // READ BY TAG
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<?> getByTagId(@PathVariable Long tagId) {
        List<ContactTagAssignment> assignments = contactTagAssignmentService.getByTagId(tagId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", assignments.size(),
                "data", assignments
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ContactTagAssignment contactTagAssignment) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactTagAssignmentService.update(id, contactTagAssignment)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        contactTagAssignmentService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ContactTagAssignment deleted successfully"
        ));
    }

    // DELETE BY CONTACT AND TAG (Unassign tag from contact)
    @DeleteMapping("/contact/{contactId}/tag/{tagId}")
    public ResponseEntity<?> deleteByContactIdAndTagId(
            @PathVariable Long contactId,
            @PathVariable Long tagId) {
        contactTagAssignmentService.deleteByContactIdAndTagId(contactId, tagId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tag unassigned from contact successfully"
        ));
    }
}