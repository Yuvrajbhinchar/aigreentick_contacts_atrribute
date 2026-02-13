package com.aigreentick.services.contacts.util;

import com.aigreentick.services.contacts.dto.request.ContactImportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility for CSV import/export operations
 * Supports both formats:
 * 1. New format: phone_number,name,city,age
 * 2. Legacy format: Name,Phone Number
 */
@Slf4j
public class CSVUtil {

    private static final String CSV_SEPARATOR = ",";

    // New format (lowercase, underscore)
    private static final String NEW_PHONE_COLUMN = "phone_number";
    private static final String NEW_NAME_COLUMN = "name";

    // Legacy format (capitalized, space)
    private static final String LEGACY_PHONE_COLUMN = "Phone Number";
    private static final String LEGACY_NAME_COLUMN = "Name";

    /**
     * Parse CSV file to ContactImportRequest
     * Handles both new and legacy CSV formats
     */
    public static ContactImportRequest parseCSV(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".csv")) {
            throw new IllegalArgumentException("File must be in CSV format");
        }

        List<ContactImportRequest.ContactImportItem> contacts = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Read header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            // Parse headers with proper CSV handling (supports quoted headers)
            List<String> headers = parseCsvLine(headerLine);

            // Detect format and find column indices
            int phoneIndex = -1;
            int nameIndex = -1;
            boolean isLegacyFormat = false;

            // Try new format first
            phoneIndex = findHeaderIndex(headers, NEW_PHONE_COLUMN);
            nameIndex = findHeaderIndex(headers, NEW_NAME_COLUMN);

            if (phoneIndex == -1 || nameIndex == -1) {
                // Try legacy format
                phoneIndex = findHeaderIndex(headers, LEGACY_PHONE_COLUMN);
                nameIndex = findHeaderIndex(headers, LEGACY_NAME_COLUMN);
                isLegacyFormat = true;
            }

            if (phoneIndex == -1) {
                throw new IllegalArgumentException(
                        "CSV must contain 'phone_number' or 'Phone Number' column"
                );
            }
            if (nameIndex == -1) {
                throw new IllegalArgumentException(
                        "CSV must contain 'name' or 'Name' column"
                );
            }

            log.info("Detected {} CSV format", isLegacyFormat ? "legacy" : "new");

            // Identify attribute columns (all columns except phone and name)
            List<String> attributeColumns = new ArrayList<>();
            for (int i = 0; i < headers.size(); i++) {
                if (i != phoneIndex && i != nameIndex) {
                    attributeColumns.add(headers.get(i));
                }
            }

            // Read data lines
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    // Parse CSV line properly (handles quoted values)
                    List<String> values = parseCsvLine(line);

                    if (values.size() < Math.max(phoneIndex, nameIndex) + 1) {
                        log.warn("Line {} has fewer columns than expected, skipping", lineNumber);
                        continue;
                    }

                    ContactImportRequest.ContactImportItem item = new ContactImportRequest.ContactImportItem();

                    // Extract phone and name
                    String phoneValue = phoneIndex < values.size() ? values.get(phoneIndex).trim() : "";
                    String nameValue = nameIndex < values.size() ? values.get(nameIndex).trim() : "";

                    // Clean phone number (remove quotes, spaces, etc.)
                    phoneValue = cleanPhoneNumber(phoneValue);

                    // Clean name (remove quotes)
                    nameValue = cleanValue(nameValue);

                    if (phoneValue.isEmpty()) {
                        log.warn("Line {} has empty phone number, skipping", lineNumber);
                        continue;
                    }

                    if (nameValue.isEmpty()) {
                        // Use phone as name if name is empty
                        nameValue = phoneValue;
                    }

                    item.setPhoneNumber(phoneValue);
                    item.setName(nameValue);

                    // Extract attributes
                    Map<String, String> attributes = new HashMap<>();
                    for (String attrColumn : attributeColumns) {
                        int attrIndex = headers.indexOf(attrColumn);
                        if (attrIndex >= 0 && attrIndex < values.size()) {
                            String attrValue = cleanValue(values.get(attrIndex));
                            if (!attrValue.isEmpty()) {
                                // Convert attribute key to lowercase with underscores
                                String attrKey = attrColumn.toLowerCase().replace(" ", "_");
                                attributes.put(attrKey, attrValue);
                            }
                        }
                    }

                    if (!attributes.isEmpty()) {
                        item.setAttributes(attributes);
                    }

                    contacts.add(item);

                } catch (Exception e) {
                    log.error("Error parsing line {}: {}", lineNumber, e.getMessage());
                    // Continue processing other lines instead of failing completely
                }
            }

            if (contacts.isEmpty()) {
                throw new IllegalArgumentException("No valid contacts found in CSV file");
            }

            log.info("Successfully parsed {} contacts from CSV", contacts.size());

        }

        ContactImportRequest request = new ContactImportRequest();
        request.setContacts(contacts);
        return request;
    }

    /**
     * Parse a single CSV line properly handling quoted values
     */
    private static List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote
                    currentValue.append('"');
                    i++; // Skip next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                result.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }

        // Add last field
        result.add(currentValue.toString());

        return result;
    }

    /**
     * Find header index (case-insensitive)
     */
    private static int findHeaderIndex(List<String> headers, String headerName) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).trim().equalsIgnoreCase(headerName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Clean phone number (remove quotes, spaces, dashes)
     */
    private static String cleanPhoneNumber(String phone) {
        return phone.replaceAll("[\"'\\s-]", "").trim();
    }

    /**
     * Clean value (remove surrounding quotes)
     */
    private static String cleanValue(String value) {
        return value.replaceAll("^\"|\"$", "").trim();
    }

    /**
     * Generate sample CSV content (legacy format to match current project)
     */
    public static String generateSampleCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name,Phone Number,City,Age\n");
        sb.append("John Doe,9876543210,Delhi,30\n");
        sb.append("Jane Smith,8765432109,Mumbai,28\n");
        sb.append("Bob Johnson,7654321098,Bangalore,35\n");
        return sb.toString();
    }

    /**
     * Export contacts to CSV format (legacy format to match current project)
     */
    public static String exportToCSV(List<Map<String, String>> contacts) {
        if (contacts.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Collect all unique attribute keys
        Set<String> allAttributes = new LinkedHashSet<>();
        for (Map<String, String> contact : contacts) {
            contact.keySet().stream()
                    .filter(key -> !key.equals("phone_number") && !key.equals("name"))
                    .forEach(allAttributes::add);
        }

        // Write header in legacy format (capitalized with spaces)
        sb.append("Name,Phone Number");
        for (String attr : allAttributes) {
            // Convert to capitalized format: city -> City, company_name -> Company Name
            String displayName = capitalize(attr.replace("_", " "));
            sb.append(",").append(displayName);
        }
        sb.append("\n");

        // Write data
        for (Map<String, String> contact : contacts) {
            // Name first, then phone (matching legacy format)
            sb.append(escapeCsv(contact.get("name"))).append(",");
            sb.append(escapeCsv(contact.get("phone_number")));

            for (String attr : allAttributes) {
                sb.append(",").append(escapeCsv(contact.getOrDefault(attr, "")));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Capitalize first letter of each word
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    /**
     * Escape CSV special characters
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Always quote if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}