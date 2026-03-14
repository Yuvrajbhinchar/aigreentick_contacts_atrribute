package com.aigreentick.services.contacts.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InternalAttributeRequest {

    @NotNull(message = "projectId is required")
    private Long projectId;

    @NotEmpty(message = "phoneNumbers cannot be empty")
    private List<String> phoneNumbers;

    @NotEmpty(message = "attributeKeys cannot be empty")
    private List<String> attributeKeys;
}