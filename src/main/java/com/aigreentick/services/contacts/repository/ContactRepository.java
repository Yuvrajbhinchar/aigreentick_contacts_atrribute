package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long>,
        JpaSpecificationExecutor<Contact> {

    /**
     * Find contact by ID and organization (security check)
     */
    Optional<Contact> findByIdAndOrganizationId(Long id, Long organizationId);

    /**
     * Check if phone exists in organization (duplicate check)
     */
    boolean existsByOrganizationIdAndWaPhoneE164(Long organizationId, String waPhoneE164);

    /**
     * Find contact by phone and organization
     */
    Optional<Contact> findByOrganizationIdAndWaPhoneE164(Long organizationId, String waPhoneE164);

    /**
     * Find all contacts by organization
     */
    List<Contact> findByOrganizationId(Long organizationId);

    /**
     * Count contacts by organization
     */
    long countByOrganizationId(Long organizationId);

    /**
     * Batch load contacts by IDs (for efficient list loading)
     */
    @Query("SELECT c FROM Contact c WHERE c.id IN :ids AND c.organizationId = :organizationId")
    List<Contact> findAllByIdsAndOrganizationId(
            @Param("ids") List<Long> ids,
            @Param("organizationId") Long organizationId
    );

    /**
     * Search contacts by name or phone
     */
    @Query("SELECT c FROM Contact c WHERE c.organizationId = :organizationId " +
            "AND (LOWER(c.displayName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR c.waPhoneE164 LIKE CONCAT('%', :search, '%'))")
    List<Contact> searchByNameOrPhone(
            @Param("organizationId") Long organizationId,
            @Param("search") String search
    );
}