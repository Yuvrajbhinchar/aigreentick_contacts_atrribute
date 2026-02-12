package outfromapp.TestService;

import com.aigreentick.services.contacts.entity.ContactNote;
import com.aigreentick.services.contacts.repository.ContactNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactNoteService {

    private final ContactNoteRepository contactNoteRepository;

    // CREATE
    public ContactNote create(ContactNote contactNote) {
        return contactNoteRepository.save(contactNote);
    }

    // READ - ALL
    public List<ContactNote> getAll() {
        return contactNoteRepository.findAll();
    }

    // READ - BY ID
    public ContactNote getById(Long id) {
        return contactNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ContactNote not found"));
    }

    // READ - BY CONTACT
    public List<ContactNote> getByContactId(Long contactId) {
        return contactNoteRepository.findByContactIdOrderByCreatedAtDesc(contactId);
    }

    // READ - BY PROJECT AND CONTACT
    public List<ContactNote> getByProjectIdAndContactId(Long projectId, Long contactId) {
        return contactNoteRepository.findByProjectIdAndContactId(projectId, contactId);
    }

    // READ - BY ORGANIZATION
    public List<ContactNote> getByOrganizationId(Long organizationId) {
        return contactNoteRepository.findByOrganizationId(organizationId);
    }

    // UPDATE
    public ContactNote update(Long id, ContactNote updatedContactNote) {
        ContactNote existing = getById(id);

        if (updatedContactNote.getNoteText() != null) {
            existing.setNoteText(updatedContactNote.getNoteText());
        }
        if (updatedContactNote.getVisibility() != null) {
            existing.setVisibility(updatedContactNote.getVisibility());
        }
        if (updatedContactNote.getProjectId() != null) {
            existing.setProjectId(updatedContactNote.getProjectId());
        }

        return contactNoteRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        ContactNote contactNote = getById(id);
        contactNoteRepository.delete(contactNote);
    }
}