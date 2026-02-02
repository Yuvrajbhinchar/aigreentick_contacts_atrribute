package com.aigreentick.services.contacts.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contacts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contacts_tenant_phone",
                        columnNames = {"tenant_id", "wa_phone_e164"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_contacts_tenant_updated",
                        columnList = "tenant_id, updated_at"
                )
        }
)
@Getter
@Setter
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "wa_phone_e164", nullable = false, length = 20)
    private String waPhoneE164;

    @Column(name = "wa_id", length = 64)
    private String waId;

    @Column(name = "display_name", length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private Source source = Source.manual;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // -------- ENUM (must match DB exactly) --------
    public enum Source {
        manual
    }
}

