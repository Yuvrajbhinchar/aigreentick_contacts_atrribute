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
                        name = "uk_value_option",
                        columnNames = {"contact_attribute_value_id", "attribute_option_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_option",
                        columnList = "attribute_option_id"
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

    @Column(name = "contact_attribute_value_id", nullable = false)
    private Long contactAttributeValueId;

    @Column(name = "attribute_option_id", nullable = false)
    private Long attributeOptionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}