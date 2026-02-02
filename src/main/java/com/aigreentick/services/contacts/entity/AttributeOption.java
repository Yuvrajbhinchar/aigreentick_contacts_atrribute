package com.aigreentick.services.contacts.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attribute_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attropt_attrdef_key",
                        columnNames = {"attribute_definition_id", "option_key"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_attropt_tenant_attrdef",
                        columnList = "tenant_id, attribute_definition_id"
                )
        }
)
@Getter
@Setter
public class AttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "attribute_definition_id", nullable = false)
    private Long attributeDefinitionId;

    @Column(name = "option_key", nullable = false, length = 120)
    private String optionKey;

    @Column(name = "option_label", nullable = false, length = 150)
    private String optionLabel;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

