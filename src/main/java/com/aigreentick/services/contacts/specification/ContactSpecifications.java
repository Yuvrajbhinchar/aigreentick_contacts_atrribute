package com.aigreentick.services.contacts.specification;

import com.aigreentick.services.contacts.entity.AttributeDefinition;
import com.aigreentick.services.contacts.entity.Contact;
import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import com.aigreentick.services.contacts.entity.ContactTagAssignment;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class ContactSpecifications {

    public static Specification<Contact> belongsToOrganization(Long organizationId) {
        return (root, query, cb) ->
                cb.equal(root.get("organizationId"), organizationId);
    }

    public static Specification<Contact> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null || name.trim().isEmpty()) return cb.conjunction();
            return cb.like(cb.lower(root.get("displayName")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Contact> hasPhoneLike(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.trim().isEmpty()) return cb.conjunction();
            return cb.like(root.get("waPhoneE164"), "%" + phone + "%");
        };
    }

    public static Specification<Contact> searchByNameOrPhone(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) return cb.conjunction();
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("displayName")), pattern),
                    cb.like(root.get("waPhoneE164"), pattern)
            );
        };
    }

    public static Specification<Contact> hasSource(String source) {
        return (root, query, cb) -> {
            if (source == null || source.trim().isEmpty()) return cb.conjunction();
            try {
                Contact.Source sourceEnum = Contact.Source.valueOf(source.toUpperCase());
                return cb.equal(root.get("source"), sourceEnum);
            } catch (IllegalArgumentException e) {
                return cb.disjunction();
            }
        };
    }

    public static Specification<Contact> hasTags(List<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) return cb.conjunction();

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactTagAssignment> tagRoot = subquery.from(ContactTagAssignment.class);
            subquery.select(tagRoot.get("contactId"))
                    .where(
                            cb.equal(tagRoot.get("contactId"), root.get("id")),
                            tagRoot.get("tagId").in(tagIds)
                    );
            return cb.exists(subquery);
        };
    }

    /**
     * FIX: The original used query.from(AttributeDefinition.class) which added a cross join
     * to the root query causing a Cartesian product. Both roots must live inside the subquery.
     */
    public static Specification<Contact> hasAttribute(String attributeKey, String attributeValue) {
        return (root, query, cb) -> {
            if (attributeKey == null || attributeKey.trim().isEmpty()) return cb.conjunction();

            // Outer subquery: find contact IDs that have this attribute key (+ optional value)
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactAttributeValue> valueRoot = subquery.from(ContactAttributeValue.class);

            // Inner correlated subquery: find the attribute definition ID for the given key
            Subquery<Long> defSubquery = subquery.subquery(Long.class);
            Root<AttributeDefinition> defRoot = defSubquery.from(AttributeDefinition.class);
            defSubquery.select(defRoot.get("id"))
                    .where(cb.equal(defRoot.get("attrKey"), attributeKey));

            // Predicates for the outer subquery
            Predicate contactMatch = cb.equal(valueRoot.get("contactId"), root.get("id"));
            Predicate defMatch = valueRoot.get("attributeDefinitionId").in(defSubquery);

            Predicate valuePredicate = (attributeValue != null && !attributeValue.trim().isEmpty())
                    ? cb.like(cb.lower(valueRoot.get("valueText")), "%" + attributeValue.toLowerCase() + "%")
                    : cb.conjunction();

            subquery.select(valueRoot.get("contactId"))
                    .where(contactMatch, defMatch, valuePredicate);

            return cb.exists(subquery);
        };
    }

    public static Specification<Contact> createdBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return cb.conjunction();
            if (startDate != null && endDate != null)
                return cb.between(root.get("createdAt"), startDate, endDate);
            if (startDate != null)
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    public static Specification<Contact> lastSeenBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return cb.conjunction();
            if (startDate != null && endDate != null)
                return cb.between(root.get("lastSeenAt"), startDate, endDate);
            if (startDate != null)
                return cb.greaterThanOrEqualTo(root.get("lastSeenAt"), startDate);
            return cb.lessThanOrEqualTo(root.get("lastSeenAt"), endDate);
        };
    }

    public static Specification<Contact> hasNoTags() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactTagAssignment> tagRoot = subquery.from(ContactTagAssignment.class);
            subquery.select(tagRoot.get("contactId"))
                    .where(cb.equal(tagRoot.get("contactId"), root.get("id")));
            return cb.not(cb.exists(subquery));
        };
    }

    public static Specification<Contact> hasNoAttributes() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ContactAttributeValue> attrRoot = subquery.from(ContactAttributeValue.class);
            subquery.select(attrRoot.get("contactId"))
                    .where(cb.equal(attrRoot.get("contactId"), root.get("id")));
            return cb.not(cb.exists(subquery));
        };
    }
}