package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {

    List<AttributeDefinition> findByOrganizationId(Long organizationId);

    List<AttributeDefinition> findByCategory(AttributeDefinition.Category category);
}