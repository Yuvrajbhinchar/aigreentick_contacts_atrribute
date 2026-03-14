package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ProjectContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectContactRepository extends JpaRepository<ProjectContact, Long> {

    Optional<ProjectContact> findByProjectIdAndContactId(Long projectId, Long contactId);

    boolean existsByProjectIdAndContactId(Long projectId, Long contactId);

    List<ProjectContact> findByProjectId(Long projectId);

    List<ProjectContact> findByContactId(Long contactId);

    /**
     * FIX: Added to support cascade delete when a contact is removed.
     * Without this, project_contacts rows referencing a deleted contact were left as orphans.
     */
    @Modifying
    @Query("DELETE FROM ProjectContact pc WHERE pc.contactId = :contactId")
    void deleteByContactId(@Param("contactId") Long contactId);
}