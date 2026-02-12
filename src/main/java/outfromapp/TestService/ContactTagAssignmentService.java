package outfromapp.TestService;

import com.aigreentick.services.contacts.entity.ContactTagAssignment;
import outfromapp.TestService.TestRepository.TContactTagAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactTagAssignmentService {

    private final TContactTagAssignmentRepository contactTagAssignmentRepository;

    // CREATE
    public ContactTagAssignment create(ContactTagAssignment contactTagAssignment) {
        // Check if this assignment already exists
        if (contactTagAssignmentRepository.existsByContactIdAndTagId(
                contactTagAssignment.getContactId(), contactTagAssignment.getTagId())) {
            throw new RuntimeException("This tag is already assigned to this contact");
        }
        return contactTagAssignmentRepository.save(contactTagAssignment);
    }

    // READ - ALL
    public List<ContactTagAssignment> getAll() {
        return contactTagAssignmentRepository.findAll();
    }

    // READ - BY ID
    public ContactTagAssignment getById(Long id) {
        return contactTagAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactTagAssignment not found"));
    }

    // READ - BY CONTACT
    public List<ContactTagAssignment> getByContactId(Long contactId) {
        return contactTagAssignmentRepository.findByContactId(contactId);
    }

    // READ - BY TAG
    public List<ContactTagAssignment> getByTagId(Long tagId) {
        return contactTagAssignmentRepository.findByTagId(tagId);
    }

    // UPDATE
    public ContactTagAssignment update(Long id, ContactTagAssignment updatedContactTagAssignment) {
        ContactTagAssignment existing = getById(id);

        if (updatedContactTagAssignment.getContactId() != null) {
            existing.setContactId(updatedContactTagAssignment.getContactId());
        }
        if (updatedContactTagAssignment.getTagId() != null) {
            existing.setTagId(updatedContactTagAssignment.getTagId());
        }
        if (updatedContactTagAssignment.getAssignedBy() != null) {
            existing.setAssignedBy(updatedContactTagAssignment.getAssignedBy());
        }

        return contactTagAssignmentRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        ContactTagAssignment contactTagAssignment = getById(id);
        contactTagAssignmentRepository.delete(contactTagAssignment);
    }

    // DELETE BY CONTACT AND TAG
    public void deleteByContactIdAndTagId(Long contactId, Long tagId) {
        ContactTagAssignment assignment = contactTagAssignmentRepository
                .findByContactIdAndTagId(contactId, tagId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        contactTagAssignmentRepository.delete(assignment);
    }
}