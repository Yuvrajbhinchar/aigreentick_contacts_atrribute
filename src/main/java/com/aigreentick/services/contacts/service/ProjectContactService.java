package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.entity.ProjectContact;
import com.aigreentick.services.contacts.repository.ProjectContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectContactService {

    private final ProjectContactRepository projectContactRepository;

    // CREATE
    public ProjectContact create(ProjectContact projectContact) {
        return projectContactRepository.save(projectContact);
    }

    // READ - ALL
    public List<ProjectContact> getAll() {
        return projectContactRepository.findAll();
    }

    // READ - BY ID
    public ProjectContact getById(Long id) {
        return projectContactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProjectContact not found"));
    }

    // UPDATE
    public ProjectContact update(Long id, ProjectContact updatedProjectContact) {
        ProjectContact existing = getById(id);

        existing.setProjectId(updatedProjectContact.getProjectId());
        existing.setContactId(updatedProjectContact.getContactId());
        existing.setConversationId(updatedProjectContact.getConversationId());
        existing.setLastMessageAt(updatedProjectContact.getLastMessageAt());
        existing.setLastMessageId(updatedProjectContact.getLastMessageId());
        existing.setUnreadCount(updatedProjectContact.getUnreadCount());

        return projectContactRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        ProjectContact projectContact = getById(id);
        projectContactRepository.delete(projectContact);
    }
}