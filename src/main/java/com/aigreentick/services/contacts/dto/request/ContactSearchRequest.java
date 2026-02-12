package com.aigreentick.services.contacts.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContactSearchRequest {

    // Search term (searches in name and phone)
    private String search;

    // Filter by specific phone
    private String phone;

    // Filter by tags
    private List<Long> tagIds;

    // Filter by source
    private String source; // manual, import, integration, inbound

    // Filter by date range
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime lastSeenAfter;
    private LocalDateTime lastSeenBefore;

    // Filter by attribute
    private String attributeKey;
    private String attributeValue;

    // Pagination
    private Integer page = 0;
    private Integer size = 50;

    // Sorting
    private String sortBy = "updatedAt"; // name, phoneNumber, createdAt, updatedAt, lastSeenAt
    private String sortDirection = "DESC"; // ASC or DESC
}