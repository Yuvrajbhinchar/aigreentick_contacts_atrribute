package com.aigreentick.services.contacts.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ContactUpdateRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$|^\\+[1-9]\\d{1,14}$",
            message = "Phone number must be either 10 digits or valid E.164 format"
    )
    private String phoneNumber;

    // Complete replacement of attributes (not merge)
    private Map<String, String> attributes;

    // Complete replacement of tags
    private List<Long> tagIds;
}