package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.dto.request.*;
import com.aigreentick.services.contacts.dto.response.*;
import com.aigreentick.services.contacts.service.*;
import com.aigreentick.services.contacts.util.CSVUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final ContactImportService contactImportService;
    private final ContactExportService contactExportService;

    /**
     * Create new contact
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createContact(
            @Valid @RequestBody ContactCreateRequest request,
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Creating contact: {} for org: {}", request.getName(), organizationId);

        ContactResponse contact = contactService.createContact(request, organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact created successfully");
        response.put("data", contact);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get contact by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getContactById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Fetching contact: {} for org: {}", id, organizationId);

        ContactResponse contact = contactService.getContactById(id, organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contact);

        return ResponseEntity.ok(response);
    }

    /**
     * List contacts
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listContacts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String tagIds,
            @RequestParam(required = false) String attributeKey,
            @RequestParam(required = false) String attributeValue,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Listing contacts for org: {}, page: {}, size: {}", organizationId, page, size);

        ContactSearchRequest searchRequest = new ContactSearchRequest();
        searchRequest.setSearch(search);
        searchRequest.setPhone(phone);
        searchRequest.setSource(source);
        searchRequest.setAttributeKey(attributeKey);
        searchRequest.setAttributeValue(attributeValue);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortDirection(sortDirection);

        if (tagIds != null && !tagIds.trim().isEmpty()) {
            try {
                java.util.List<Long> tagIdList = java.util.Arrays.stream(tagIds.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(java.util.stream.Collectors.toList());
                searchRequest.setTagIds(tagIdList);
            } catch (NumberFormatException e) {
                log.warn("Invalid tag IDs format: {}", tagIds);
            }
        }

        PageResponse<ContactListItemResponse> contacts =
                contactService.listContacts(searchRequest, organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contacts);

        return ResponseEntity.ok(response);
    }

    /**
     * Update contact
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactUpdateRequest request,
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Updating contact: {} for org: {}", id, organizationId);

        ContactResponse contact = contactService.updateContact(id, request, organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact updated successfully");
        response.put("data", contact);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete contact
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContact(
            @PathVariable Long id,
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Deleting contact: {} for org: {}", id, organizationId);

        contactService.deleteContact(id, organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Import contacts
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importContacts(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") Boolean updateExisting,
            @RequestParam(defaultValue = "true") Boolean createNewAttributes,
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Importing contacts for org: {} from file: {}", organizationId, file.getOriginalFilename());

        try {
            ContactImportRequest request = CSVUtil.parseCSV(file);
            request.setUpdateExisting(updateExisting);
            request.setCreateNewAttributes(createNewAttributes);

            ContactImportResponse importResponse =
                    contactImportService.importContacts(request, organizationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Import completed");
            response.put("data", importResponse);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error parsing CSV file", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error parsing CSV file: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Export contacts
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportContacts(
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Exporting contacts for org: {}", organizationId);

        String csv = contactExportService.exportContactsToCSV(organizationId);

        ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

        String filename = "contacts_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    /**
     * Download sample CSV
     */
    @GetMapping("/sample-csv")
    public ResponseEntity<Resource> downloadSampleCSV() {
        log.info("Downloading sample CSV");

        String csv = CSVUtil.generateSampleCSV();

        ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample_contacts.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    /**
     * Advanced search
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchContacts(
            @Valid @RequestBody ContactSearchRequest searchRequest,
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Searching contacts for org: {}", organizationId);

        PageResponse<ContactListItemResponse> contacts =
                contactService.listContacts(searchRequest, organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contacts);

        return ResponseEntity.ok(response);
    }

    /**
     * Get contact count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getContactCount(
            @RequestHeader(value = "X-Organization-ID", required = true) Long organizationId
    ) {
        log.info("Getting contact count for org: {}", organizationId);

        long count = contactService.getContactCount(organizationId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }
}