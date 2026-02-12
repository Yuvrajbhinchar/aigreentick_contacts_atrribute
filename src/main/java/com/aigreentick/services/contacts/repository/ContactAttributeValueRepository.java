package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactAttributeValueRepository extends JpaRepository<ContactAttributeValue, Long> {

    /**
     * Find all attributes for a contact
     */
    List<ContactAttributeValue> findByContactId(Long contactId);

    /**
     * Batch load attributes for multiple contacts (efficient for list view)
     */
    @Query("SELECT av FROM ContactAttributeValue av WHERE av.contactId IN :contactIds")
    List<ContactAttributeValue> findByContactIdIn(@Param("contactIds") List<Long> contactIds);

    /**
     * Delete all attributes for a contact (for update operation)
     */
    @Modifying
    @Query("DELETE FROM ContactAttributeValue av WHERE av.contactId = :contactId")
    void deleteByContactId(@Param("contactId") Long contactId);

    /**
     * Find attribute value by contact and attribute definition
     */
    @Query("SELECT av FROM ContactAttributeValue av " +
            "WHERE av.contactId = :contactId " +
            "AND av.attributeDefinitionId = :attributeDefinitionId")
    ContactAttributeValue findByContactIdAndAttributeDefinitionId(
            @Param("contactId") Long contactId,
            @Param("attributeDefinitionId") Long attributeDefinitionId
    );

    /**
     * Count attributes for a contact
     */
    long countByContactId(Long contactId);
}