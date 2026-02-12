package com.aigreentick.services.contacts.exception;

public class DuplicateContactException extends RuntimeException {
    private final String phoneNumber;
    private final Long existingContactId;

    public DuplicateContactException(String phoneNumber, Long existingContactId) {
        super("Contact already exists with phone number: " + phoneNumber);
        this.phoneNumber = phoneNumber;
        this.existingContactId = existingContactId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Long getExistingContactId() {
        return existingContactId;
    }
}