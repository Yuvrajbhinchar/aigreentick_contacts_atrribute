package com.aigreentick.services.contacts.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });
        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder().success(false).message("Validation failed")
                        .errors(errors).timestamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            errors.put(v.getPropertyPath().toString(), v.getMessage());
        }
        log.warn("Constraint violation: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder().success(false).message("Constraint violation")
                        .errors(errors).timestamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler(ContactNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleContactNotFound(ContactNotFoundException ex) {
        log.warn("Contact not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder().success(false).message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler(DuplicateContactException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateContact(DuplicateContactException ex) {
        Map<String, Object> data = new HashMap<>();
        data.put("phoneNumber", ex.getPhoneNumber());
        data.put("existingContactId", ex.getExistingContactId());
        log.warn("Duplicate contact: phone={}, existingId={}", ex.getPhoneNumber(), ex.getExistingContactId());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErrorResponse.builder().success(false).message(ex.getMessage())
                        .data(data).timestamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler(InvalidAttributeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAttribute(InvalidAttributeException ex) {
        log.warn("Invalid attribute: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder().success(false).message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler(ContactAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(ContactAccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder().success(false).message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPhoneNumber(InvalidPhoneNumberException ex) {
        log.warn("Invalid phone number: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder().success(false).message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }

    /**
     * FIX: Added handler for TagNotFoundException, which is now thrown by assignTagsToContact()
     * instead of InvalidAttributeException (which was semantically wrong for a missing tag).
     */
    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTagNotFound(TagNotFoundException ex) {
        log.warn("Tag not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder().success(false).message(ex.getMessage())
                        .timestamp(LocalDateTime.now()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder().success(false).message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now()).build());
    }

    @Data
    @lombok.Builder
    public static class ErrorResponse {
        private Boolean success;
        private String message;
        private Map<String, String> errors;
        private Map<String, Object> data;
        private LocalDateTime timestamp;
    }
}