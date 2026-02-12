package com.aigreentick.services.contacts.security;

/**
 * Thread-local storage for organization context
 * In production, this would be populated from JWT token
 */
public class OrganizationContext {

    private static final ThreadLocal<Long> ORGANIZATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    public static void setOrganizationId(Long organizationId) {
        ORGANIZATION_ID.set(organizationId);
    }

    public static Long getOrganizationId() {
        Long orgId = ORGANIZATION_ID.get();
        if (orgId == null) {
            throw new IllegalStateException("Organization context not set");
        }
        return orgId;
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        ORGANIZATION_ID.remove();
        USER_ID.remove();
    }
}