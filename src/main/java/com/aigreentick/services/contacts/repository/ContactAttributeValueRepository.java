package com.aigreentick.services.contacts.repository;

import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactAttributeValueRepository extends JpaRepository<ContactAttributeValue, Long> {

    List<ContactAttributeValue> findByContactId(Long contactId);

    List<ContactAttributeValue> findByAttributeDefinitionId(Long attributeDefinitionId);
}