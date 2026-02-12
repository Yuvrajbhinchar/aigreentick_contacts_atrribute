package com.aigreentick.services.contacts.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to extract organization ID from request
 * In production, this would decode JWT token
 */
@Slf4j
@Component
public class OrganizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {

        // TODO: Extract from JWT token
        // For now, use header or default value

        String orgIdHeader = request.getHeader("X-Organization-ID");

        Long organizationId = 1L; // Default for testing

        if (orgIdHeader != null) {
            try {
                organizationId = Long.parseLong(orgIdHeader);
            } catch (NumberFormatException e) {
                log.warn("Invalid organization ID in header: {}", orgIdHeader);
            }
        }

        OrganizationContext.setOrganizationId(organizationId);

        log.debug("Organization context set: {}", organizationId);

        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        OrganizationContext.clear();
    }
}