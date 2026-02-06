package com.aigreentick.services.contacts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "projects",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_org_slug", columnNames = {"organization_id", "slug"})
        },
        indexes = {
                @Index(name = "idx_org_id", columnList = "organization_id"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_by", columnList = "created_by"),
                @Index(name = "idx_uuid", columnList = "uuid")
        }
)
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", nullable = false, length = 150)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "icon", length = 50)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.active;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.private_;

    @Column(name = "settings", columnDefinition = "json DEFAULT '{}'")
    private String settings = "{}";

    @Column(name = "metadata", columnDefinition = "json DEFAULT '{}'")
    private String metadata = "{}";

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "archived_by")
    private Long archivedBy;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // -------- ENUMS (must match DB exactly) --------
    public enum Status {
        active,
        archived,
        deleted
    }

    public enum Visibility {
        @Column(name = "private")
        private_,
        organization,
        @Column(name = "public")
        public_
    }
}