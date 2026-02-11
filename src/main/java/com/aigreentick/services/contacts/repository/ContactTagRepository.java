package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ContactTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactTagRepository extends JpaRepository<ContactTag, Long> {

    List<ContactTag> findByOrganizationId(Long organizationId);

    List<ContactTag> findByOrganizationIdAndIsActive(Long organizationId, Boolean isActive);

    Optional<ContactTag> findByOrganizationIdAndName(Long organizationId, String name);

    boolean existsByOrganizationIdAndName(Long organizationId, String name);
}