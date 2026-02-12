package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ContactTagAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactTagAssignmentRepository extends JpaRepository<ContactTagAssignment, Long> {

    /**
     * Find all tag assignments for a contact
     */
    List<ContactTagAssignment> findByContactId(Long contactId);

    /**
     * Batch load tag assignments for multiple contacts
     */
    @Query("SELECT ta FROM ContactTagAssignment ta WHERE ta.contactId IN :contactIds")
    List<ContactTagAssignment> findByContactIdIn(@Param("contactIds") List<Long> contactIds);

    /**
     * Find all contacts with a specific tag
     */
    List<ContactTagAssignment> findByTagId(Long tagId);

    /**
     * Find by organization and tag
     */
    List<ContactTagAssignment> findByOrganizationIdAndTagId(Long organizationId, Long tagId);

    /**
     * Find specific assignment
     */
    Optional<ContactTagAssignment> findByContactIdAndTagId(Long contactId, Long tagId);

    /**
     * Check if assignment exists
     */
    boolean existsByContactIdAndTagId(Long contactId, Long tagId);

    /**
     * Delete all assignments for a contact
     */
    @Modifying
    @Query("DELETE FROM ContactTagAssignment ta WHERE ta.contactId = :contactId")
    void deleteByContactId(@Param("contactId") Long contactId);

    /**
     * Delete specific assignment
     */
    @Modifying
    @Query("DELETE FROM ContactTagAssignment ta WHERE ta.contactId = :contactId AND ta.tagId = :tagId")
    void deleteByContactIdAndTagId(@Param("contactId") Long contactId, @Param("tagId") Long tagId);
}