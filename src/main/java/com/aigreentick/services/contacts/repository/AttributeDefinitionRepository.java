package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {

    /**
     * Find all attribute definitions for an organization
     */
    List<AttributeDefinition> findByOrganizationId(Long organizationId);

    /**
     * Find attribute definition by key and organization
     */
    Optional<AttributeDefinition> findByOrganizationIdAndAttrKey(
            Long organizationId,
            String attrKey
    );

    /**
     * Check if attribute key exists in organization
     */
    boolean existsByOrganizationIdAndAttrKey(Long organizationId, String attrKey);

    /**
     * Find by category
     */
    List<AttributeDefinition> findByOrganizationIdAndCategory(
            Long organizationId,
            AttributeDefinition.Category category
    );

    /**
     * Find searchable attributes
     */
    @Query("SELECT ad FROM AttributeDefinition ad " +
            "WHERE ad.organizationId = :organizationId " +
            "AND ad.isSearchable = true")
    List<AttributeDefinition> findSearchableByOrganizationId(
            @Param("organizationId") Long organizationId
    );

    /**
     * Batch find by keys (for import validation)
     */
    @Query("SELECT ad FROM AttributeDefinition ad " +
            "WHERE ad.organizationId = :organizationId " +
            "AND ad.attrKey IN :keys")
    List<AttributeDefinition> findByOrganizationIdAndAttrKeyIn(
            @Param("organizationId") Long organizationId,
            @Param("keys") List<String> keys
    );
}