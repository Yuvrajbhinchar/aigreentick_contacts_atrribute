package com.aigreentick.services.contacts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "created_at"),
                @Index(name = "idx_uuid", columnList = "uuid"),
                @Index(name = "idx_role_id", columnList = "role_id"),
                @Index(name = "idx_password_reset_token", columnList = "password_reset_token_hash"),
                @Index(name = "idx_password_reset_expires", columnList = "password_reset_expires_at"),
                @Index(name = "idx_email_verification_token", columnList = "email_verification_token_hash"),
                @Index(name = "idx_email_verification_expires", columnList = "email_verification_expires_at")
        }
)
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "UTC";

    @Column(name = "locale", nullable = false, length = 10)
    private String locale = "en";

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.pending_verification;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Password reset fields
    @Column(name = "password_reset_token_hash", length = 255)
    private String passwordResetTokenHash;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "password_reset_used_at")
    private LocalDateTime passwordResetUsedAt;

    // Email verification fields
    @Column(name = "email_verification_token_hash", length = 255)
    private String emailVerificationTokenHash;

    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;

    @Column(name = "email_verification_email", length = 190)
    private String emailVerificationEmail;

    // -------- ENUMS (must match DB exactly) --------
    public enum Status {
        active,
        blocked,
        deleted,
        pending_verification
    }
}