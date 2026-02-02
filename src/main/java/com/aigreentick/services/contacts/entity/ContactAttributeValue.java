package com.aigreentick.services.contacts.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "contact_attribute_values",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cav_contact_attr",
                        columnNames = {"tenant_id", "contact_id", "attribute_definition_id"}
                )
        },
        indexes = {
                @Index(name = "idx_cav_contact", columnList = "tenant_id, contact_id"),
                @Index(name = "idx_cav_attr_number", columnList = "tenant_id, attribute_definition_id, value_number"),
                @Index(name = "idx_cav_attr_datetime", columnList = "tenant_id, attribute_definition_id, value_datetime")
                // NOTE: prefix index on value_text(500) cannot be represented in JPA
        }
)
@Getter
@Setter
public class ContactAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "contact_id", nullable = false)
    private Long contactId;

    @Column(name = "attribute_definition_id", nullable = false)
    private Long attributeDefinitionId;

    @Column(name = "value_text", length = 1024)
    private String valueText;

    @Column(name = "value_number")
    private Long valueNumber;

    @Column(name = "value_decimal", precision = 18, scale = 6)
    private BigDecimal valueDecimal;

    @Column(name = "value_bool")
    private Boolean valueBool;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @Column(name = "value_datetime")
    private LocalDateTime valueDatetime;

    @Column(name = "value_json", columnDefinition = "json")
    private String valueJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "updated_source", nullable = false)
    private UpdatedSource updatedSource = UpdatedSource.user;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // -------- ENUM (must match DB exactly) --------
    public enum UpdatedSource {
        system,
        user,
        integration
    }
}

