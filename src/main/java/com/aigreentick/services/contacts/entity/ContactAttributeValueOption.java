package com.aigreentick.services.contacts.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contact_attribute_value_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cavo_unique",
                        columnNames = {"tenant_id", "contact_attribute_value_id", "attribute_option_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_cavo_tenant_cav",
                        columnList = "tenant_id, contact_attribute_value_id"
                ),
                @Index(
                        name = "idx_cavo_tenant_opt",
                        columnList = "tenant_id, attribute_option_id"
                )
        }
)
@Getter
@Setter
public class ContactAttributeValueOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "contact_attribute_value_id", nullable = false)
    private Long contactAttributeValueId;

    @Column(name = "attribute_option_id", nullable = false)
    private Long attributeOptionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

