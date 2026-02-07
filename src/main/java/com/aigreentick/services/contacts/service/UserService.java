package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.dto.UserRequest;
import com.aigreentick.services.contacts.dto.UserResponse;
import com.aigreentick.services.contacts.entity.User;
import com.aigreentick.services.contacts.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Hash password using SHA-256 (for production, use BCrypt or Argon2)
     */
    private String hashPassword(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Create a new user
     */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
        }

        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setRoleId(request.getRoleId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Hash the password
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(hashPassword(request.getPassword()));
        } else {
            throw new IllegalArgumentException("Password is required");
        }

        user.setAvatarUrl(request.getAvatarUrl());
        user.setTimezone(request.getTimezone() != null ? request.getTimezone() : "UTC");
        user.setLocale(request.getLocale() != null ? request.getLocale() : "en");
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() != null ? request.getStatus() : User.Status.pending_verification);
        user.setFailedLoginAttempts(0);

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    /**
     * Get user by UUID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUuid(String uuid) {
        User user = userRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("User not found with uuid: " + uuid));
        return UserResponse.fromEntity(user);
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return UserResponse.fromEntity(user);
    }

    /**
     * Get all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update user
     */
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Check if email is being changed and if new email already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("User with email '" + request.getEmail() + "' already exists");
            }
            user.setEmail(request.getEmail());
            // Reset email verification when email changes
            user.setEmailVerifiedAt(null);
        }

        if (request.getRoleId() != null) {
            user.setRoleId(request.getRoleId());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(hashPassword(request.getPassword()));
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        if (request.getLocale() != null) {
            user.setLocale(request.getLocale());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    /**
     * Verify user email
     */
    @Transactional
    public UserResponse verifyEmail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setEmailVerifiedAt(LocalDateTime.now());
        if (user.getStatus() == User.Status.pending_verification) {
            user.setStatus(User.Status.active);
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    /**
     * Verify user phone
     */
    @Transactional
    public UserResponse verifyPhone(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setPhoneVerifiedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    /**
     * Record login attempt
     */
    @Transactional
    public UserResponse recordLoginAttempt(Long id, boolean successful, String ipAddress) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (successful) {
            user.setLastLoginAt(LocalDateTime.now());
            user.setLastLoginIp(ipAddress);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        } else {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // Lock account after 5 failed attempts for 30 minutes
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            }
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    /**
     * Block user
     */
    @Transactional
    public UserResponse blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setStatus(User.Status.blocked);

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    /**
     * Unblock user
     */
    @Transactional
    public UserResponse unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (user.getStatus() == User.Status.blocked) {
            user.setStatus(User.Status.active);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    /**
     * Soft delete user (set deleted_at timestamp)
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(User.Status.deleted);
        userRepository.save(user);
    }

    /**
     * Hard delete user (permanently remove from database)
     */
    @Transactional
    public void hardDeleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}