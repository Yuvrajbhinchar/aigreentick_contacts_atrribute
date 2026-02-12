package com.aigreentick.services.contacts.exception;

public class ContactAccessDeniedException extends RuntimeException {
    public ContactAccessDeniedException(String message) {
        super(message);
    }
}