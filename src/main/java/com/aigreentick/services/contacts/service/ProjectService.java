package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.entity.Project;
import com.aigreentick.services.contacts.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    // CREATE
    public Project create(Project project) {
        project.setUuid(UUID.randomUUID().toString());
        return projectRepository.save(project);
    }

    // READ - ALL
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    // READ - BY ID
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    // UPDATE
    public Project update(Long id, Project updatedProject) {
        Project existing = getById(id);

        existing.setName(updatedProject.getName());
        existing.setSlug(updatedProject.getSlug());
        existing.setDescription(updatedProject.getDescription());
        existing.setColor(updatedProject.getColor());
        existing.setIcon(updatedProject.getIcon());
        existing.setStatus(updatedProject.getStatus());
        existing.setVisibility(updatedProject.getVisibility());
        existing.setSettings(updatedProject.getSettings());
        existing.setMetadata(updatedProject.getMetadata());

        return projectRepository.save(existing);
    }

    // DELETE (Soft delete)
    public void delete(Long id) {
        Project project = getById(id);
        project.setDeletedAt(java.time.LocalDateTime.now());
        project.setStatus(Project.Status.deleted);
        projectRepository.save(project);
    }
}
