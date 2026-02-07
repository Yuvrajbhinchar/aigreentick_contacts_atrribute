package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.dto.OrganizationRequest;
import com.aigreentick.services.contacts.dto.OrganizationResponse;
import com.aigreentick.services.contacts.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Create a new organization
     * POST /api/v1/organizations
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrganization(@RequestBody OrganizationRequest request) {
        try {
            OrganizationResponse response = organizationService.createOrganization(request);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organization created successfully");
            result.put("data", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to create organization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all organizations
     * GET /api/v1/organizations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrganizations() {
        try {
            List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organizations retrieved successfully");
            result.put("data", organizations);
            result.put("count", organizations.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve organizations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get organization by ID
     * GET /api/v1/organizations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrganizationById(@PathVariable Long id) {
        try {
            OrganizationResponse response = organizationService.getOrganizationById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organization retrieved successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve organization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get organization by UUID
     * GET /api/v1/organizations/uuid/{uuid}
     */
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Map<String, Object>> getOrganizationByUuid(@PathVariable String uuid) {
        try {
            OrganizationResponse response = organizationService.getOrganizationByUuid(uuid);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organization retrieved successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve organization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get organization by slug
     * GET /api/v1/organizations/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> getOrganizationBySlug(@PathVariable String slug) {
        try {
            OrganizationResponse response = organizationService.getOrganizationBySlug(slug);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organization retrieved successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve organization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update organization
     * PUT /api/v1/organizations/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOrganization(
            @PathVariable Long id,
            @RequestBody OrganizationRequest request) {
        try {
            OrganizationResponse response = organizationService.updateOrganization(id, request);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organization updated successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to update organization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Soft delete organization (sets deleted_at timestamp)
     * DELETE /api/v1/organizations/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrganization(@PathVariable Long id) {
        try {
            organizationService.deleteOrganization(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organization deleted successfully (soft delete)");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete organization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Hard delete organization (permanently removes from database)
     * DELETE /api/v1/organizations/{id}/hard
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Map<String, Object>> hardDeleteOrganization(@PathVariable Long id) {
        try {
            organizationService.hardDeleteOrganization(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Organization permanently deleted");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete organization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}