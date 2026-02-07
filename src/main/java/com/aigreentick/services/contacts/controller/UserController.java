package com.aigreentick.services.contacts.controller;

import com.aigreentick.services.contacts.dto.UserRequest;
import com.aigreentick.services.contacts.dto.UserResponse;
import com.aigreentick.services.contacts.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Create a new user
     * POST /api/v1/users
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody UserRequest request) {
        try {
            UserResponse response = userService.createUser(request);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User created successfully");
            result.put("data", response);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all users
     * GET /api/v1/users
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Users retrieved successfully");
            result.put("data", users);
            result.put("count", users.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            UserResponse response = userService.getUserById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User retrieved successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get user by UUID
     * GET /api/v1/users/uuid/{uuid}
     */
    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<Map<String, Object>> getUserByUuid(@PathVariable String uuid) {
        try {
            UserResponse response = userService.getUserByUuid(uuid);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User retrieved successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get user by email
     * GET /api/v1/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@PathVariable String email) {
        try {
            UserResponse response = userService.getUserByEmail(email);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User retrieved successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update user
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest request) {
        try {
            UserResponse response = userService.updateUser(id, request);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User updated successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Verify user email
     * POST /api/v1/users/{id}/verify-email
     */
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@PathVariable Long id) {
        try {
            UserResponse response = userService.verifyEmail(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Email verified successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to verify email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Verify user phone
     * POST /api/v1/users/{id}/verify-phone
     */
    @PostMapping("/{id}/verify-phone")
    public ResponseEntity<Map<String, Object>> verifyPhone(@PathVariable Long id) {
        try {
            UserResponse response = userService.verifyPhone(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Phone verified successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to verify phone: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Record login attempt
     * POST /api/v1/users/{id}/login-attempt
     */
    @PostMapping("/{id}/login-attempt")
    public ResponseEntity<Map<String, Object>> recordLoginAttempt(
            @PathVariable Long id,
            @RequestParam boolean successful,
            @RequestParam(required = false) String ipAddress) {
        try {
            UserResponse response = userService.recordLoginAttempt(id, successful, ipAddress);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Login attempt recorded");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to record login attempt: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Block user
     * POST /api/v1/users/{id}/block
     */
    @PostMapping("/{id}/block")
    public ResponseEntity<Map<String, Object>> blockUser(@PathVariable Long id) {
        try {
            UserResponse response = userService.blockUser(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User blocked successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to block user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Unblock user
     * POST /api/v1/users/{id}/unblock
     */
    @PostMapping("/{id}/unblock")
    public ResponseEntity<Map<String, Object>> unblockUser(@PathVariable Long id) {
        try {
            UserResponse response = userService.unblockUser(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User unblocked successfully");
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to unblock user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Soft delete user (sets deleted_at timestamp)
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User deleted successfully (soft delete)");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Hard delete user (permanently removes from database)
     * DELETE /api/v1/users/{id}/hard
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Map<String, Object>> hardDeleteUser(@PathVariable Long id) {
        try {
            userService.hardDeleteUser(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "User permanently deleted");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}