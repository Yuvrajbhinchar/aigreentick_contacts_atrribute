package com.aigreentick.services.contacts.specification;

import com.aigreentick.services.contacts.entity.Contact;
import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import com.aigreentick.services.contacts.entity.ContactTagAssignment;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Specifications for dynamic Contact queries
 * Allows building complex, composable queries
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
     */
    public static Specification<Contact> hasSource(String source) {
        return (root, query, criteriaBuilder) -> {
            if (source == null || source.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("source"), Contact.Source.valueOf(source));
        };
    }

    /**
     * Filter by tag IDs
     */
    public static Specification<Contact> hasTags(List<Long> tagIds) {
        return (root, query, criteriaBuilder) -> {
            if (tagIds == null || tagIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Join with tag assignments
            Join<Contact, ContactTagAssignment> tagJoin = root.join("tagAssignments", JoinType.INNER);

            // Contact must have at least one of the specified tags
            return tagJoin.get("tagId").in(tagIds);
        };
    }

    /**
     * Filter by attribute key and value
     */
    public static Specification<Contact> hasAttribute(String attributeKey, String attributeValue) {
        return (root, query, criteriaBuilder) -> {
            if (attributeKey == null || attributeKey.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Subquery to check if contact has this attribute
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactAttributeValue> attrRoot = subquery.from(ContactAttributeValue.class);

            subquery.select(attrRoot.get("contactId"));

            Predicate contactIdMatch = criteriaBuilder.equal(
                    attrRoot.get("contactId"),
                    root.get("id")
            );

            // Join with attribute definition to check key
            Join<ContactAttributeValue, com.aigreentick.services.contacts.entity.AttributeDefinition> attrDefJoin =
                    attrRoot.join("attributeDefinition");

            Predicate keyMatch = criteriaBuilder.equal(
                    attrDefJoin.get("attrKey"),
                    attributeKey
            );

            Predicate valuePredicate = criteriaBuilder.conjunction();
            if (attributeValue != null && !attributeValue.trim().isEmpty()) {
                valuePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(attrRoot.get("valueText")),
                        "%" + attributeValue.toLowerCase() + "%"
                );
            }

            subquery.where(contactIdMatch, keyMatch, valuePredicate);

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

            subquery.select(tagRoot.get("contactId"));
            subquery.where(criteriaBuilder.equal(tagRoot.get("contactId"), root.get("id")));

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

            subquery.select(attrRoot.get("contactId"));
            subquery.where(criteriaBuilder.equal(attrRoot.get("contactId"), root.get("id")));

            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }
}