package com.aigreentick.services.contacts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "organizations",
        indexes = {
                @Index(name = "idx_parent_org", columnList = "parent_organization_id"),
                @Index(name = "idx_org_type", columnList = "organization_type")
        }
)
@Getter
@Setter
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "parent_organization_id")
    private Long parentOrganizationId;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website", length = 500)
    private String website;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false)
    private OrganizationType organizationType;

    @Column(name = "reseller_level", nullable = false)
    private Integer resellerLevel = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_strategy", nullable = false)
    private PricingStrategy pricingStrategy = PricingStrategy.inherit;

    @Column(name = "max_child_resellers")
    private Integer maxChildResellers;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.active;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    @Column(name = "metadata", columnDefinition = "json DEFAULT '{}'")
    private String metadata = "{}";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // -------- ENUMS (must match DB exactly) --------
    public enum OrganizationType {
        platform,
        organization,
        reseller,
        customer
    }

    public enum PricingStrategy {
        inherit,
        override
    }

    public enum Status {
        active,
        suspended,
        trial,
        cancelled
    }
}