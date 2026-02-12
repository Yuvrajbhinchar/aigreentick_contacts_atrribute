package com.aigreentick.services.contacts.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ContactResponse {

    private Long id;
    private String name;
    private String phoneNumber;
    private String waId;
    private String source;

    // Attributes as list of objects (not just map)
    private List<AttributeValueResponse> attributes;

    // Tags with full details
    private List<TagResponse> tags;

    // Recent notes
    private List<NoteResponse> recentNotes;

    // Projects this contact belongs to
    private List<ProjectSummaryResponse> projects;

    // Timestamps
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class AttributeValueResponse {
        private Long id;
        private String key;
        private String label;
        private String value;
        private String dataType;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class TagResponse {
        private Long id;
        private String name;
        private String color;
        private LocalDateTime assignedAt;
    }

    @Data
    @Builder
    public static class NoteResponse {
        private Long id;
        private String text;
        private String createdByName;
        private String visibility;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class ProjectSummaryResponse {
        private Long id;
        private String name;
        private String slug;
        private Integer unreadCount;
        private LocalDateTime lastMessageAt;
    }
}