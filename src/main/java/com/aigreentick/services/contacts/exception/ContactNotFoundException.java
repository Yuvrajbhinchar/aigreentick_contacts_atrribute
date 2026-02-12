package com.aigreentick.services.contacts.exception;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException(String message) {
        super(message);
    }

    public ContactNotFoundException(Long id) {
        super("Contact not found with id: " + id);
    }
}