package com.aigreentick.services.contacts.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ContactImportRequest {

    @NotEmpty(message = "Contacts list cannot be empty")
    @Valid
    private List<ContactImportItem> contacts;

    private Boolean updateExisting = true;

    private Boolean createNewAttributes = true;

    @Data
    public static class ContactImportItem {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Phone number is required")
        /**
         * FIX: Original pattern accepted ONLY 10-digit numbers ("^[0-9]{10}$"), while
         * ContactCreateRequest and ContactUpdateRequest both accepted 10-digit OR E.164.
         * This caused valid international numbers to be rejected at import but accepted
         * everywhere else. The pattern now matches both formats consistently.
         */
        @Pattern(
                regexp = "^[0-9]{10}$|^\\+[1-9]\\d{1,14}$",
                message = "Phone number must be either 10 digits or valid E.164 format"
        )
        private String phoneNumber;

        private Map<String, String> attributes;
    }
}