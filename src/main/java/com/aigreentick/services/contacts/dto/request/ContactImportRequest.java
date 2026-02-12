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

    // Whether to update existing contacts or skip them
    private Boolean updateExisting = true;

    // Whether to create attribute definitions for new attributes
    private Boolean createNewAttributes = true;

    @Data
    public static class ContactImportItem {

        @NotBlank(message = "Name is required")
        private String name;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
        private String phoneNumber;

        // Any additional columns from CSV
        private Map<String, String> attributes;
    }
}