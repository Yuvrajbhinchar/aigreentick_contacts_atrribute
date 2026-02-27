package com.aigreentick.services.contacts.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkContactAttributeRequest {

    @NotEmpty(message = "Phone numbers list cannot be empty")
    private List<String> phoneNumbers;

    @NotEmpty(message = "Attribute keys list cannot be empty")
    private List<String> attributeKeys;
}