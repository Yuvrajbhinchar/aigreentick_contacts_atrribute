package com.aigreentick.services.contacts.service;

import com.aigreentick.services.contacts.entity.ContactAttributeValueOption;
import com.aigreentick.services.contacts.repository.ContactAttributeValueOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactAttributeValueOptionService {

    private final ContactAttributeValueOptionRepository contactAttributeValueOptionRepository;

    // CREATE
    public ContactAttributeValueOption create(ContactAttributeValueOption contactAttributeValueOption) {
        return contactAttributeValueOptionRepository.save(contactAttributeValueOption);
    }

    // READ - ALL
    public List<ContactAttributeValueOption> getAll() {
        return contactAttributeValueOptionRepository.findAll();
    }

    // READ - BY ID
    public ContactAttributeValueOption getById(Long id) {
        return contactAttributeValueOptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactAttributeValueOption not found"));
    }

    // UPDATE
    public ContactAttributeValueOption update(Long id, ContactAttributeValueOption updatedContactAttributeValueOption) {
        ContactAttributeValueOption existing = getById(id);

        existing.setContactAttributeValueId(updatedContactAttributeValueOption.getContactAttributeValueId());
        existing.setAttributeOptionId(updatedContactAttributeValueOption.getAttributeOptionId());

        return contactAttributeValueOptionRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        ContactAttributeValueOption contactAttributeValueOption = getById(id);
        contactAttributeValueOptionRepository.delete(contactAttributeValueOption);
    }
}