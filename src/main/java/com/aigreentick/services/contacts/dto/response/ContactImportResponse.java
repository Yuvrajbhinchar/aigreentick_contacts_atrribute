package com.aigreentick.services.contacts.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ContactImportResponse {

    private Integer totalProcessed;
    private Integer successCount;
    private Integer failedCount;
    private Integer updatedCount;
    private Integer createdCount;
    private Integer skippedCount;

    private List<ImportError> errors;

    @Data
    @Builder
    public static class ImportError {
        private Integer rowNumber;
        private String phoneNumber;
        private String name;
        private String errorMessage;
        private String errorType; // DUPLICATE, VALIDATION, SYSTEM
    }
}