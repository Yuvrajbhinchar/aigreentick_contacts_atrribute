package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import com.aigreentick.services.contacts.service.ContactAttributeValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact-attribute-values")
@RequiredArgsConstructor
public class ContactAttributeValueController {

    private final ContactAttributeValueService contactAttributeValueService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ContactAttributeValue contactAttributeValue) {
        ContactAttributeValue saved = contactAttributeValueService.create(contactAttributeValue);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<ContactAttributeValue> contactAttributeValues = contactAttributeValueService.getAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", contactAttributeValues.size(),
                "data", contactAttributeValues
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactAttributeValueService.getById(id)
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ContactAttributeValue contactAttributeValue
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactAttributeValueService.update(id, contactAttributeValue)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        contactAttributeValueService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ContactAttributeValue deleted successfully"
        ));
    }
}