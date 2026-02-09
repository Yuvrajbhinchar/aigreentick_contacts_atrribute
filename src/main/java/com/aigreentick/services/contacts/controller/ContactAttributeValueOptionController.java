package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.entity.ContactAttributeValueOption;
import com.aigreentick.services.contacts.service.ContactAttributeValueOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact-attribute-value-options")
@RequiredArgsConstructor
public class ContactAttributeValueOptionController {

    private final ContactAttributeValueOptionService contactAttributeValueOptionService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ContactAttributeValueOption contactAttributeValueOption) {
        ContactAttributeValueOption saved = contactAttributeValueOptionService.create(contactAttributeValueOption);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<ContactAttributeValueOption> contactAttributeValueOptions = contactAttributeValueOptionService.getAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", contactAttributeValueOptions.size(),
                "data", contactAttributeValueOptions
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactAttributeValueOptionService.getById(id)
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ContactAttributeValueOption contactAttributeValueOption
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactAttributeValueOptionService.update(id, contactAttributeValueOption)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        contactAttributeValueOptionService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ContactAttributeValueOption deleted successfully"
        ));
    }
}