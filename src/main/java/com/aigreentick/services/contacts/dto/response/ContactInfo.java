package com.aigreentick.services.contacts.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ContactInfo {

    private Long contactId;
    private String phoneNumber;
    private List<Attribute> attributes;

    @Data
    @Builder
    public static class Attribute {
        private String key;
        private String value;
    }
}