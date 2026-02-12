package com.aigreentick.services.contacts.util;

import com.aigreentick.services.contacts.exception.InvalidPhoneNumberException;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for phone number validation and formatting
 * Handles both 10-digit format and E.164 international format
 */
@Slf4j
public class PhoneNumberUtil {

    private static final String DEFAULT_COUNTRY_CODE = "+91"; // India

    /**
     * Normalize phone number to E.164 format
     *
     * @param phoneNumber Input phone number (10 digits or E.164)
     * @return E.164 formatted phone number
     */
    public static String normalizeToE164(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new InvalidPhoneNumberException("Phone number cannot be null or empty");
        }

        // Remove all non-digit characters except +
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");

        // Already in E.164 format
        if (cleaned.startsWith("+")) {
            if (!isValidE164(cleaned)) {
                throw new InvalidPhoneNumberException("Invalid E.164 format: " + phoneNumber);
            }
            return cleaned;
        }

        // 10 digit format - add default country code
        if (cleaned.length() == 10) {
            return DEFAULT_COUNTRY_CODE + cleaned;
        }

        // 11 digit with country code but no +
        if (cleaned.length() == 12 && cleaned.startsWith("91")) {
            return "+" + cleaned;
        }

        throw new InvalidPhoneNumberException(
                "Phone number must be either 10 digits or valid E.164 format: " + phoneNumber
        );
    }

    /**
     * Validate E.164 format
     * Format: +[1-9]\d{1,14}
     */
    public static boolean isValidE164(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        return phoneNumber.matches("^\\+[1-9]\\d{1,14}$");
    }

    /**
     * Validate 10-digit format
     */
    public static boolean isValid10Digit(String phoneNumber) {
        if (phoneNumber == null) {
            return false;
        }
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");
        return cleaned.matches("^[6-9]\\d{9}$"); // Indian mobile numbers
    }

    /**
     * Format for display (removes country code if present)
     */
    public static String formatForDisplay(String e164Number) {
        if (e164Number == null) {
            return "";
        }

        if (e164Number.startsWith("+91")) {
            return e164Number.substring(3);
        }

        if (e164Number.startsWith("+")) {
            return e164Number.substring(1);
        }

        return e164Number;
    }

    /**
     * Extract WhatsApp ID from E.164 number
     * Format: 919876543210@s.whatsapp.net
     */
    public static String generateWhatsAppId(String e164Number) {
        if (e164Number == null || !e164Number.startsWith("+")) {
            throw new InvalidPhoneNumberException("Invalid E.164 number for WhatsApp ID");
        }

        String numberWithoutPlus = e164Number.substring(1);
        return numberWithoutPlus + "@s.whatsapp.net";
    }
}