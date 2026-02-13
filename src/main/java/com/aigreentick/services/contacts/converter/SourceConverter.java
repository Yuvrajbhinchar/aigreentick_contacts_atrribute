package com.aigreentick.services.contacts.converter;

import com.aigreentick.services.contacts.entity.Contact;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts Contact.Source enum to/from database
 * Java: IMPORT (uppercase) → Database: "import" (lowercase)
 */
@Converter(autoApply = true)
public class SourceConverter implements AttributeConverter<Contact.Source, String> {

    @Override
    public String convertToDatabaseColumn(Contact.Source source) {
        if (source == null) {
            return null;
        }
        // Convert IMPORT → "import", MANUAL → "manual", etc.
        return source.name().toLowerCase();
    }

    @Override
    public Contact.Source convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // Convert "import" → IMPORT, "manual" → MANUAL, etc.
        return Contact.Source.valueOf(dbData.toUpperCase());
    }
}