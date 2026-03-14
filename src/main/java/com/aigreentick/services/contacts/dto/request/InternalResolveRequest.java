package com.aigreentick.services.contacts.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InternalResolveRequest {

    @NotNull(message = "projectId is required")
    private Long projectId;

    @NotEmpty(message = "phoneNumbers cannot be empty")
    private List<String> phoneNumbers;
}