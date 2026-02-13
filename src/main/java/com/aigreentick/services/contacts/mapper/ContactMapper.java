package com.aigreentick.services.contacts.mapper;

import com.aigreentick.services.contacts.dto.response.ContactListItemResponse;
import com.aigreentick.services.contacts.dto.response.ContactResponse;
import com.aigreentick.services.contacts.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Contact entities and DTOs
 */
@Component
@RequiredArgsConstructor
public class ContactMapper {

    /**
     * Convert Contact entity to full ContactResponse
     * Used for detail view (GET /contacts/{id})
     */
    public ContactResponse toResponse(
            Contact contact,
            List<ContactAttributeValue> attributes,
            List<ContactTagAssignment> tagAssignments,
            List<ContactTag> tags,
            List<ContactNote> notes,
            Map<Long, AttributeDefinition> attributeDefinitions
    ) {

        // Map attributes
        List<ContactResponse.AttributeValueResponse> attributeResponses = attributes.stream()
                .map(attr -> {
                    AttributeDefinition definition = attributeDefinitions.get(attr.getAttributeDefinitionId());

                    return ContactResponse.AttributeValueResponse.builder()
                            .id(attr.getId())
                            .key(definition != null ? definition.getAttrKey() : "unknown")
                            .label(definition != null ? definition.getLabel() : "Unknown")
                            .value(extractAttributeValue(attr))
                            .dataType(definition != null ? definition.getDataType().name() : "text")
                            .updatedAt(attr.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        // Map tags
        Map<Long, ContactTag> tagMap = tags.stream()
                .collect(Collectors.toMap(ContactTag::getId, tag -> tag));

        List<ContactResponse.TagResponse> tagResponses = tagAssignments.stream()
                .map(assignment -> {
                    ContactTag tag = tagMap.get(assignment.getTagId());
                    if (tag == null) return null;

                    return ContactResponse.TagResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .assignedAt(assignment.getAssignedAt())
                            .build();
                })
                .filter(tag -> tag != null)
                .collect(Collectors.toList());

        // Map notes (recent 5)
        List<ContactResponse.NoteResponse> noteResponses = notes.stream()
                .limit(5)
                .map(note -> ContactResponse.NoteResponse.builder()
                        .id(note.getId())
                        .text(note.getNoteText())
                        .createdByName("User-" + note.getCreatedBy()) // TODO: Get actual user name
                        .visibility(note.getVisibility().name())
                        .createdAt(note.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ContactResponse.builder()
                .id(contact.getId())
                .name(contact.getDisplayName())
                .phoneNumber(contact.getWaPhoneE164())
                .waId(contact.getWaId())
                .source(contact.getSource().name())
                .attributes(attributeResponses)
                .tags(tagResponses)
                .recentNotes(noteResponses)
                .projects(new ArrayList<>()) // TODO: Load project associations
                .firstSeenAt(contact.getFirstSeenAt())
                .lastSeenAt(contact.getLastSeenAt())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .source(contact.getSource().name().toLowerCase())
                .build();
    }

    /**
     * Convert Contact entity to lightweight ContactListItemResponse
     * Used for list view (GET /contacts)
     */
    public ContactListItemResponse toListItemResponse(
            Contact contact,
            List<ContactAttributeValue> attributes,
            List<ContactTagAssignment> tagAssignments,
            List<ContactTag> tags,
            Map<Long, AttributeDefinition> attributeDefinitions,
            long noteCount
    ) {

        // Map attributes (simplified - just key-value)
        List<ContactListItemResponse.SimpleAttribute> simpleAttributes = attributes.stream()
                .limit(5) // Only show first 5 attributes in list
                .map(attr -> {
                    AttributeDefinition definition = attributeDefinitions.get(attr.getAttributeDefinitionId());

                    return ContactListItemResponse.SimpleAttribute.builder()
                            .key(definition != null ? definition.getAttrKey() : "unknown")
                            .value(extractAttributeValue(attr))
                            .build();
                })
                .collect(Collectors.toList());

        // Map tags (simplified)
        Map<Long, ContactTag> tagMap = tags.stream()
                .collect(Collectors.toMap(ContactTag::getId, tag -> tag));

        List<ContactListItemResponse.SimpleTag> simpleTags = tagAssignments.stream()
                .map(assignment -> {
                    ContactTag tag = tagMap.get(assignment.getTagId());
                    if (tag == null) return null;

                    return ContactListItemResponse.SimpleTag.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .color(tag.getColor())
                            .build();
                })
                .filter(tag -> tag != null)
                .collect(Collectors.toList());

        return ContactListItemResponse.builder()
                .id(contact.getId())
                .name(contact.getDisplayName())
                .phoneNumber(contact.getWaPhoneE164())
                .source(contact.getSource().name())
                .attributes(simpleAttributes)
                .tags(simpleTags)
                .noteCount((int) noteCount)
                .projectCount(0) // TODO: Get actual project count
                .lastSeenAt(contact.getLastSeenAt())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .source(contact.getSource().name().toLowerCase())
                .build();
    }

    /**
     * Extract value from ContactAttributeValue based on data type
     */
    private String extractAttributeValue(ContactAttributeValue attr) {
        // Priority: text > number > decimal > boolean > date > datetime
        if (attr.getValueText() != null && !attr.getValueText().isEmpty()) {
            return attr.getValueText();
        }
        if (attr.getValueNumber() != null) {
            return attr.getValueNumber().toString();
        }
        if (attr.getValueDecimal() != null) {
            return attr.getValueDecimal().toString();
        }
        if (attr.getValueBool() != null) {
            return attr.getValueBool().toString();
        }
        if (attr.getValueDate() != null) {
            return attr.getValueDate().toString();
        }
        if (attr.getValueDatetime() != null) {
            return attr.getValueDatetime().toString();
        }
        if (attr.getValueJson() != null) {
            return attr.getValueJson();
        }
        return "";
    }
}