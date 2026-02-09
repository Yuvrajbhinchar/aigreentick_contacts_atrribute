package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByUuid(String uuid);

    List<Project> findByOrganizationId(Long organizationId);

    boolean existsByOrganizationIdAndSlug(Long organizationId, String slug);
}
