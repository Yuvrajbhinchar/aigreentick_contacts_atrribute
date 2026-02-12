package outfromapp.TestService;

import com.aigreentick.services.contacts.entity.ContactTag;
import com.aigreentick.services.contacts.repository.ContactTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactTagService {

    private final ContactTagRepository contactTagRepository;

    // CREATE
    public ContactTag create(ContactTag contactTag) {
        // Check if tag with same name already exists for this organization
        if (contactTagRepository.existsByOrganizationIdAndName(
                contactTag.getOrganizationId(), contactTag.getName())) {
            throw new RuntimeException("Tag with name '" + contactTag.getName() +
                    "' already exists for this organization");
        }
        return contactTagRepository.save(contactTag);
    }

    // READ - ALL
    public List<ContactTag> getAll() {
        return contactTagRepository.findAll();
    }

    // READ - BY ID
    public ContactTag getById(Long id) {
        return contactTagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactTag not found"));
    }

    // READ - BY ORGANIZATION
    public List<ContactTag> getByOrganizationId(Long organizationId) {
        return contactTagRepository.findByOrganizationId(organizationId);
    }

    // READ - BY ORGANIZATION AND ACTIVE STATUS
    public List<ContactTag> getByOrganizationIdAndIsActive(Long organizationId, Boolean isActive) {
        return contactTagRepository.findByOrganizationIdAndIsActive(organizationId, isActive);
    }

    // UPDATE
    public ContactTag update(Long id, ContactTag updatedContactTag) {
        ContactTag existing = getById(id);

        if (updatedContactTag.getName() != null &&
                !updatedContactTag.getName().equals(existing.getName())) {
            // Check if new name already exists
            if (contactTagRepository.existsByOrganizationIdAndName(
                    existing.getOrganizationId(), updatedContactTag.getName())) {
                throw new RuntimeException("Tag with name '" + updatedContactTag.getName() +
                        "' already exists for this organization");
            }
            existing.setName(updatedContactTag.getName());
        }

        if (updatedContactTag.getColor() != null) {
            existing.setColor(updatedContactTag.getColor());
        }
        if (updatedContactTag.getIsSystem() != null) {
            existing.setIsSystem(updatedContactTag.getIsSystem());
        }
        if (updatedContactTag.getIsActive() != null) {
            existing.setIsActive(updatedContactTag.getIsActive());
        }

        return contactTagRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        ContactTag contactTag = getById(id);
        contactTagRepository.delete(contactTag);
    }
}