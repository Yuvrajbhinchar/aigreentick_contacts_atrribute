package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.dto.OrganizationRequest;
import com.aigreentick.services.contacts.dto.OrganizationResponse;
import com.aigreentick.services.contacts.entity.Organization;
import com.aigreentick.services.contacts.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    /**
     * Create a new organization
     */
    @Transactional
    public OrganizationResponse createOrganization(OrganizationRequest request) {
        // Validate slug uniqueness
        if (organizationRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Organization with slug '" + request.getSlug() + "' already exists");
        }

        Organization organization = new Organization();
        organization.setUuid(UUID.randomUUID().toString());
        organization.setParentOrganizationId(request.getParentOrganizationId());
        organization.setSlug(request.getSlug());
        organization.setName(request.getName());
        organization.setDisplayName(request.getDisplayName());
        organization.setDescription(request.getDescription());
        organization.setLogoUrl(request.getLogoUrl());
        organization.setWebsite(request.getWebsite());
        organization.setOrganizationType(request.getOrganizationType() != null ?
                request.getOrganizationType() : Organization.OrganizationType.customer);
        organization.setResellerLevel(request.getResellerLevel() != null ? request.getResellerLevel() : 0);
        organization.setPricingStrategy(request.getPricingStrategy() != null ?
                request.getPricingStrategy() : Organization.PricingStrategy.inherit);
        organization.setMaxChildResellers(request.getMaxChildResellers());
        organization.setOwnerUserId(request.getOwnerUserId());
        organization.setStatus(request.getStatus() != null ?
                request.getStatus() : Organization.Status.active);
        organization.setMetadata(request.getMetadata() != null ? request.getMetadata() : "{}");

        Organization saved = organizationRepository.save(organization);
        return OrganizationResponse.fromEntity(saved);
    }

    /**
     * Get organization by ID
     */
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + id));
        return OrganizationResponse.fromEntity(organization);
    }

    /**
     * Get organization by UUID
     */
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationByUuid(String uuid) {
        Organization organization = organizationRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with uuid: " + uuid));
        return OrganizationResponse.fromEntity(organization);
    }

    /**
     * Get organization by slug
     */
    @Transactional(readOnly = true)
    public OrganizationResponse getOrganizationBySlug(String slug) {
        Organization organization = organizationRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with slug: " + slug));
        return OrganizationResponse.fromEntity(organization);
    }

    /**
     * Get all organizations
     */
    @Transactional(readOnly = true)
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(OrganizationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update organization
     */
    @Transactional
    public OrganizationResponse updateOrganization(Long id, OrganizationRequest request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + id));

        // Check if slug is being changed and if new slug already exists
        if (request.getSlug() != null && !request.getSlug().equals(organization.getSlug())) {
            if (organizationRepository.existsBySlug(request.getSlug())) {
                throw new IllegalArgumentException("Organization with slug '" + request.getSlug() + "' already exists");
            }
            organization.setSlug(request.getSlug());
        }

        if (request.getParentOrganizationId() != null) {
            organization.setParentOrganizationId(request.getParentOrganizationId());
        }
        if (request.getName() != null) {
            organization.setName(request.getName());
        }
        if (request.getDisplayName() != null) {
            organization.setDisplayName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            organization.setDescription(request.getDescription());
        }
        if (request.getLogoUrl() != null) {
            organization.setLogoUrl(request.getLogoUrl());
        }
        if (request.getWebsite() != null) {
            organization.setWebsite(request.getWebsite());
        }
        if (request.getOrganizationType() != null) {
            organization.setOrganizationType(request.getOrganizationType());
        }
        if (request.getResellerLevel() != null) {
            organization.setResellerLevel(request.getResellerLevel());
        }
        if (request.getPricingStrategy() != null) {
            organization.setPricingStrategy(request.getPricingStrategy());
        }
        if (request.getMaxChildResellers() != null) {
            organization.setMaxChildResellers(request.getMaxChildResellers());
        }
        if (request.getOwnerUserId() != null) {
            organization.setOwnerUserId(request.getOwnerUserId());
        }
        if (request.getStatus() != null) {
            organization.setStatus(request.getStatus());
        }
        if (request.getMetadata() != null) {
            organization.setMetadata(request.getMetadata());
        }

        Organization updated = organizationRepository.save(organization);
        return OrganizationResponse.fromEntity(updated);
    }

    /**
     * Soft delete organization (set deleted_at timestamp)
     */
    @Transactional
    public void deleteOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + id));

        organization.setDeletedAt(LocalDateTime.now());
        organization.setStatus(Organization.Status.cancelled);
        organizationRepository.save(organization);
    }

    /**
     * Hard delete organization (permanently remove from database)
     */
    @Transactional
    public void hardDeleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new IllegalArgumentException("Organization not found with id: " + id);
        }
        organizationRepository.deleteById(id);
    }
}