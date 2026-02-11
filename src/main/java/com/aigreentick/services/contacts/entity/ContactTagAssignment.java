package com.aigreentick.services.contacts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contact_tag_assignments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_contact_tag", columnNames = {"contact_id", "tag_id"})
        },
        indexes = {
                @Index(name = "idx_org_tag", columnList = "organization_id, tag_id"),
                @Index(name = "idx_contact", columnList = "contact_id")
        }
)
@Getter
@Setter
public class ContactTagAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "contact_id", nullable = false)
    private Long contactId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @Column(name = "assigned_by")
    private Long assignedBy;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
}