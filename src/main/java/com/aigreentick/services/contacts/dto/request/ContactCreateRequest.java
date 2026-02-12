package com.aigreentick.services.contacts.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ContactCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$|^\\+[1-9]\\d{1,14}$",
            message = "Phone number must be either 10 digits or valid E.164 format"
    )
    private String phoneNumber;

    // Custom attributes as key-value pairs
    // Example: {"city": "Jaipur", "age": "25", "company": "Tech Corp"}
    private Map<String, String> attributes;

    // Tag IDs to assign
    private List<Long> tagIds;

    // Optional: Add to specific project
    private Long projectId;

    // Optional: Initial note
    @Size(max = 5000, message = "Note cannot exceed 5000 characters")
    private String initialNote;
}