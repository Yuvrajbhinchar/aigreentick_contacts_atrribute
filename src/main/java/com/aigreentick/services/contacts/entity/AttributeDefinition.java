package com.aigreentick.services.contacts.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attribute_definitions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attrdef_tenant_key", columnNames = {"tenant_id", "key"})
        },
        indexes = {
                @Index(name = "idx_attrdef_tenant_category", columnList = "tenant_id, category"),
                @Index(name = "idx_attrdef_tenant_datatype", columnList = "tenant_id, data_type")
        }
)
@Getter
@Setter
public class AttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "key", nullable = false, length = 120)
    private String key;

    @Column(name = "label", nullable = false, length = 150)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;

    @Column(name = "is_editable", nullable = false)
    private Boolean isEditable = true;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "default_value_json", columnDefinition = "json")
    private String defaultValueJson;

    @Column(name = "validation_json", columnDefinition = "json")
    private String validationJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.all;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ---------- ENUMS (match DB exactly) ----------

    public enum Category {
        system,
        user_defined,
        integration
    }

    public enum DataType {
        text,
        number,
        decimal,
        date,
        datetime,
        json,
        single_select,
        multi_select
        }

    public enum Visibility {
        all,
        internal_only
    }
}

