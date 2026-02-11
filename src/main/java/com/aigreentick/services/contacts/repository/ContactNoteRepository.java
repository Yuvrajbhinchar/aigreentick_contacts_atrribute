package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ContactNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactNoteRepository extends JpaRepository<ContactNote, Long> {

    List<ContactNote> findByContactId(Long contactId);

    List<ContactNote> findByContactIdOrderByCreatedAtDesc(Long contactId);

    List<ContactNote> findByProjectIdAndContactId(Long projectId, Long contactId);

    List<ContactNote> findByOrganizationId(Long organizationId);
}