package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ProjectContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectContactRepository extends JpaRepository<ProjectContact, Long> {

    /**
     * Find a project-contact link by project and contact
     */
    Optional<ProjectContact> findByProjectIdAndContactId(Long projectId, Long contactId);

    /**
     * Check if a contact is already linked to a project
     */
    boolean existsByProjectIdAndContactId(Long projectId, Long contactId);

    /**
     * Find all contacts linked to a project
     */
    List<ProjectContact> findByProjectId(Long projectId);

    /**
     * Find all projects a contact belongs to
     */
    List<ProjectContact> findByContactId(Long contactId);
}