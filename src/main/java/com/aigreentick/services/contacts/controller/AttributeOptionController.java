package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.entity.AttributeOption;
import com.aigreentick.services.contacts.service.AttributeOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attribute-options")
@RequiredArgsConstructor
public class AttributeOptionController {

    private final AttributeOptionService attributeOptionService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AttributeOption attributeOption) {
        AttributeOption saved = attributeOptionService.create(attributeOption);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<AttributeOption> attributeOptions = attributeOptionService.getAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", attributeOptions.size(),
                "data", attributeOptions
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", attributeOptionService.getById(id)
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody AttributeOption attributeOption
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", attributeOptionService.update(id, attributeOption)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        attributeOptionService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "AttributeOption deleted successfully"
        ));
    }
}