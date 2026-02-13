package com.aigreentick.services.contacts.specification;

import com.aigreentick.services.contacts.entity.AttributeDefinition;
import com.aigreentick.services.contacts.entity.Contact;
import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import com.aigreentick.services.contacts.entity.ContactTagAssignment;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Specifications for dynamic Contact queries
 * FIXED VERSION - Handles missing entity relationships properly
 */
public class ContactSpecifications {

    /**
     * Filter by organization (ALWAYS use this for multi-tenancy)
     */
    public static Specification<Contact> belongsToOrganization(Long organizationId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("organizationId"), organizationId);
    }

    /**
     * Search by name (case-insensitive, partial match)
     */
    public static Specification<Contact> hasNameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("displayName")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    /**
     * Search by phone number (partial match)
     */
    public static Specification<Contact> hasPhoneLike(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (phone == null || phone.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    root.get("waPhoneE164"),
                    "%" + phone + "%"
            );
        };
    }

    /**
     * Search by name OR phone (combined search)
     */
    public static Specification<Contact> searchByNameOrPhone(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("displayName")),
                    likePattern
            );

            Predicate phonePredicate = criteriaBuilder.like(
                    root.get("waPhoneE164"),
                    likePattern
            );

            return criteriaBuilder.or(namePredicate, phonePredicate);
        };
    }

    /**
     * Filter by source
     * FIXED: Handle source string to enum conversion properly
     */
    public static Specification<Contact> hasSource(String source) {
        return (root, query, criteriaBuilder) -> {
            if (source == null || source.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            try {
                // Map "import" to "import_" since import is reserved keyword
                String enumValue = source.equals("import") ? "import_" : source;
                Contact.Source sourceEnum = Contact.Source.valueOf(enumValue);
                return criteriaBuilder.equal(root.get("source"), sourceEnum);
            } catch (IllegalArgumentException e) {
                // Invalid source value, return no results
                return criteriaBuilder.disjunction();
            }
        };
    }

    /**
     * Filter by tag IDs
     * FIXED: Proper subquery without relationship dependency
     */
    public static Specification<Contact> hasTags(List<Long> tagIds) {
        return (root, query, criteriaBuilder) -> {
            if (tagIds == null || tagIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Subquery to find contacts with these tags
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactTagAssignment> tagRoot = subquery.from(ContactTagAssignment.class);

            subquery.select(tagRoot.get("contactId"))
                    .where(
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(tagRoot.get("contactId"), root.get("id")),
                                    tagRoot.get("tagId").in(tagIds)
                            )
                    );

            return criteriaBuilder.exists(subquery);
        };
    }

    /**
     * Filter by attribute key and value
     * FIXED: Removed broken join, use manual correlation instead
     */
    public static Specification<Contact> hasAttribute(String attributeKey, String attributeValue) {
        return (root, query, criteriaBuilder) -> {
            if (attributeKey == null || attributeKey.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Subquery to find contacts with this attribute
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactAttributeValue> attrValueRoot = subquery.from(ContactAttributeValue.class);
            Root<AttributeDefinition> attrDefRoot = query.from(AttributeDefinition.class);

            // Build predicates
            Predicate contactMatch = criteriaBuilder.equal(
                    attrValueRoot.get("contactId"),
                    root.get("id")
            );

            Predicate defIdMatch = criteriaBuilder.equal(
                    attrValueRoot.get("attributeDefinitionId"),
                    attrDefRoot.get("id")
            );

            Predicate keyMatch = criteriaBuilder.equal(
                    attrDefRoot.get("attrKey"),
                    attributeKey
            );

            Predicate valuePredicate;
            if (attributeValue != null && !attributeValue.trim().isEmpty()) {
                valuePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(attrValueRoot.get("valueText")),
                        "%" + attributeValue.toLowerCase() + "%"
                );
            } else {
                valuePredicate = criteriaBuilder.conjunction();
            }

            subquery.select(attrValueRoot.get("contactId"))
                    .where(contactMatch, defIdMatch, keyMatch, valuePredicate);

            return criteriaBuilder.exists(subquery);
        };
    }

    /**
     * Filter by created date range
     */
    public static Specification<Contact> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }

            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
            }
        };
    }

    /**
     * Filter by last seen date range
     */
    public static Specification<Contact> lastSeenBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }

            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("lastSeenAt"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("lastSeenAt"), startDate);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("lastSeenAt"), endDate);
            }
        };
    }

    /**
     * Filter contacts with no tags
     */
    public static Specification<Contact> hasNoTags() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactTagAssignment> tagRoot = subquery.from(ContactTagAssignment.class);

            subquery.select(tagRoot.get("contactId"))
                    .where(criteriaBuilder.equal(tagRoot.get("contactId"), root.get("id")));

            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }

    /**
     * Filter contacts with no attributes
     */
    public static Specification<Contact> hasNoAttributes() {
        return (root, query, criteriaBuilder) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactAttributeValue> attrRoot = subquery.from(ContactAttributeValue.class);

            subquery.select(attrRoot.get("contactId"))
                    .where(criteriaBuilder.equal(attrRoot.get("contactId"), root.get("id")));

            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }
}