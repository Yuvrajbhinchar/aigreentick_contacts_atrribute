package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.entity.AttributeDefinition;
import com.aigreentick.services.contacts.service.AttributeDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attribute-definitions")
@RequiredArgsConstructor
public class AttributeDefinitionController {

    private final AttributeDefinitionService attributeDefinitionService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody AttributeDefinition attributeDefinition) {
        AttributeDefinition saved = attributeDefinitionService.create(attributeDefinition);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<AttributeDefinition> attributeDefinitions = attributeDefinitionService.getAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", attributeDefinitions.size(),
                "data", attributeDefinitions
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", attributeDefinitionService.getById(id)
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody AttributeDefinition attributeDefinition
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", attributeDefinitionService.update(id, attributeDefinition)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        attributeDefinitionService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "AttributeDefinition deleted successfully"
        ));
    }
}