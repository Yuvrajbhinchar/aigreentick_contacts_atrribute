package com.aigreentick.services.contacts.util;

import com.aigreentick.services.contacts.dto.request.ContactImportRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility for CSV import/export operations
 */
@Slf4j
public class CSVUtil {

    private static final String CSV_SEPARATOR = ",";
    private static final String REQUIRED_COLUMN_PHONE = "phone_number";
    private static final String REQUIRED_COLUMN_NAME = "name";

    /**
     * Parse CSV file to ContactImportRequest
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

            List<String> headers = Arrays.asList(headerLine.split(CSV_SEPARATOR));

            // Validate required columns
            if (!headers.contains(REQUIRED_COLUMN_PHONE)) {
                throw new IllegalArgumentException("CSV must contain 'phone_number' column");
            }
            if (!headers.contains(REQUIRED_COLUMN_NAME)) {
                throw new IllegalArgumentException("CSV must contain 'name' column");
            }

            int phoneIndex = headers.indexOf(REQUIRED_COLUMN_PHONE);
            int nameIndex = headers.indexOf(REQUIRED_COLUMN_NAME);

            // Identify attribute columns (all columns except phone and name)
            List<String> attributeColumns = new ArrayList<>();
            for (String header : headers) {
                if (!header.equals(REQUIRED_COLUMN_PHONE) && !header.equals(REQUIRED_COLUMN_NAME)) {
                    attributeColumns.add(header);
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
                    String[] values = line.split(CSV_SEPARATOR, -1); // -1 to keep trailing empty strings

                    if (values.length < headers.size()) {
                        log.warn("Line {} has fewer columns than header, skipping", lineNumber);
                        continue;
                    }

                    ContactImportRequest.ContactImportItem item = new ContactImportRequest.ContactImportItem();

                    // Extract phone and name
                    item.setPhoneNumber(values[phoneIndex].trim());
                    item.setName(values[nameIndex].trim());

                    // Extract attributes
                    Map<String, String> attributes = new HashMap<>();
                    for (String attrColumn : attributeColumns) {
                        int attrIndex = headers.indexOf(attrColumn);
                        if (attrIndex < values.length && !values[attrIndex].trim().isEmpty()) {
                            attributes.put(attrColumn, values[attrIndex].trim());
                        }
                    }

                    if (!attributes.isEmpty()) {
                        item.setAttributes(attributes);
                    }

                    contacts.add(item);

                } catch (Exception e) {
                    log.error("Error parsing line {}: {}", lineNumber, e.getMessage());
                    throw new IllegalArgumentException("Error parsing line " + lineNumber + ": " + e.getMessage());
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
     * Generate sample CSV content
     */
    public static String generateSampleCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("phone_number,name,city,age\n");
        sb.append("9876543210,John Doe,Delhi,30\n");
        sb.append("8765432109,Jane Smith,Mumbai,28\n");
        sb.append("7654321098,Bob Johnson,Bangalore,35\n");
        return sb.toString();
    }

    /**
     * Export contacts to CSV format
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

        // Write header
        sb.append("phone_number,name");
        for (String attr : allAttributes) {
            sb.append(",").append(attr);
        }
        sb.append("\n");

        // Write data
        for (Map<String, String> contact : contacts) {
            sb.append(escapeCsv(contact.get("phone_number"))).append(",");
            sb.append(escapeCsv(contact.get("name")));

            for (String attr : allAttributes) {
                sb.append(",").append(escapeCsv(contact.getOrDefault(attr, "")));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Escape CSV special characters
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}