package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import com.aigreentick.services.contacts.repository.ContactAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactAttributeValueService {

    private final ContactAttributeValueRepository contactAttributeValueRepository;

    // CREATE
    public ContactAttributeValue create(ContactAttributeValue contactAttributeValue) {
        return contactAttributeValueRepository.save(contactAttributeValue);
    }

    // READ - ALL
    public List<ContactAttributeValue> getAll() {
        return contactAttributeValueRepository.findAll();
    }

    // READ - BY ID
    public ContactAttributeValue getById(Long id) {
        return contactAttributeValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactAttributeValue not found"));
    }

    // UPDATE
    public ContactAttributeValue update(Long id, ContactAttributeValue updatedContactAttributeValue) {
        ContactAttributeValue existing = getById(id);

        existing.setContactId(updatedContactAttributeValue.getContactId());
        existing.setAttributeDefinitionId(updatedContactAttributeValue.getAttributeDefinitionId());
        existing.setValueText(updatedContactAttributeValue.getValueText());
        existing.setValueNumber(updatedContactAttributeValue.getValueNumber());
        existing.setValueDecimal(updatedContactAttributeValue.getValueDecimal());
        existing.setValueBool(updatedContactAttributeValue.getValueBool());
        existing.setValueDate(updatedContactAttributeValue.getValueDate());
        existing.setValueDatetime(updatedContactAttributeValue.getValueDatetime());
        existing.setValueJson(updatedContactAttributeValue.getValueJson());
        existing.setUpdatedSource(updatedContactAttributeValue.getUpdatedSource());

        return contactAttributeValueRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        ContactAttributeValue contactAttributeValue = getById(id);
        contactAttributeValueRepository.delete(contactAttributeValue);
    }
}