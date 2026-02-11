package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ContactTagAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactTagAssignmentRepository extends JpaRepository<ContactTagAssignment, Long> {

    List<ContactTagAssignment> findByContactId(Long contactId);

    List<ContactTagAssignment> findByTagId(Long tagId);

    List<ContactTagAssignment> findByOrganizationIdAndTagId(Long organizationId, Long tagId);

    Optional<ContactTagAssignment> findByContactIdAndTagId(Long contactId, Long tagId);

    boolean existsByContactIdAndTagId(Long contactId, Long tagId);
}