package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByUuid(String uuid);

    Optional<Organization> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByUuid(String uuid);
}