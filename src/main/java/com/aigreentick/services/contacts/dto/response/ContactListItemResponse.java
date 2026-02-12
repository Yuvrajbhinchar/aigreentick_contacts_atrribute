package com.aigreentick.services.contacts.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lightweight DTO for contact list view
 * Only includes essential fields for performance
 */
@Data
@Builder
public class ContactListItemResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String source;

    // Simplified attributes (just key-value for display)
    private List<SimpleAttribute> attributes;

    // Just tag names and colors (no full details)
    private List<SimpleTag> tags;

    // Counts
    private Integer noteCount;
    private Integer projectCount;

    // Timestamps
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class SimpleAttribute {
        private String key;
        private String value;
    }

    @Data
    @Builder
    public static class SimpleTag {
        private Long id;
        private String name;
        private String color;
    }
}