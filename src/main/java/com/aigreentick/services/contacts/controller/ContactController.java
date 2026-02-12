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

/**
 * REST API Controller for Contact management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final ContactImportService contactImportService;
    private final ContactExportService contactExportService;

    // TODO: Get organization ID from security context (JWT token)
    // For now, hardcoded for testing
    private static final Long ORGANIZATION_ID = 1L;

    /**
     * Create new contact
     * POST /api/v1/contacts
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createContact(
            @Valid @RequestBody ContactCreateRequest request
    ) {

        log.info("Creating contact: {}", request.getName());

        ContactResponse contact = contactService.createContact(request, ORGANIZATION_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact created successfully");
        response.put("data", contact);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get contact by ID
     * GET /api/v1/contacts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getContactById(@PathVariable Long id) {

        log.info("Fetching contact: {}", id);

        ContactResponse contact = contactService.getContactById(id, ORGANIZATION_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contact);

        return ResponseEntity.ok(response);
    }

    /**
     * List contacts with pagination and filters
     * GET /api/v1/contacts?search=john&page=0&size=50&sortBy=name&sortDirection=ASC
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listContacts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String tagIds, // Comma-separated: "1,2,3"
            @RequestParam(required = false) String attributeKey,
            @RequestParam(required = false) String attributeValue,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {

        log.info("Listing contacts - page: {}, size: {}, search: {}", page, size, search);

        // Build search request
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

        // Parse tag IDs
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
                contactService.listContacts(searchRequest, ORGANIZATION_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contacts);

        return ResponseEntity.ok(response);
    }

    /**
     * Update contact
     * PUT /api/v1/contacts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactUpdateRequest request
    ) {

        log.info("Updating contact: {}", id);

        ContactResponse contact = contactService.updateContact(id, request, ORGANIZATION_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact updated successfully");
        response.put("data", contact);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete contact
     * DELETE /api/v1/contacts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long id) {

        log.info("Deleting contact: {}", id);

        contactService.deleteContact(id, ORGANIZATION_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Import contacts from CSV
     * POST /api/v1/contacts/import
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importContacts(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "true") Boolean updateExisting,
            @RequestParam(defaultValue = "true") Boolean createNewAttributes
    ) {

        log.info("Importing contacts from file: {}", file.getOriginalFilename());

        try {
            // Parse CSV file
            ContactImportRequest request = CSVUtil.parseCSV(file);
            request.setUpdateExisting(updateExisting);
            request.setCreateNewAttributes(createNewAttributes);

            // Import
            ContactImportResponse importResponse =
                    contactImportService.importContacts(request, ORGANIZATION_ID);

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
     * Export contacts to CSV
     * GET /api/v1/contacts/export
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportContacts() {

        log.info("Exporting contacts to CSV");

        String csv = contactExportService.exportContactsToCSV(ORGANIZATION_ID);

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
     * Download sample CSV template
     * GET /api/v1/contacts/sample-csv
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
     * Search contacts (advanced)
     * POST /api/v1/contacts/search
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchContacts(
            @Valid @RequestBody ContactSearchRequest searchRequest
    ) {

        log.info("Searching contacts with filters");

        PageResponse<ContactListItemResponse> contacts =
                contactService.listContacts(searchRequest, ORGANIZATION_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", contacts);

        return ResponseEntity.ok(response);
    }

    /**
     * Get contact count
     * GET /api/v1/contacts/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getContactCount() {

        long count = contactService.getContactCount(ORGANIZATION_ID);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }
}