package outfromapp.TestService;

import com.aigreentick.services.contacts.entity.Contact;
import outfromapp.TestService.TestRepository.TContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TContactService {

    private final TContactRepository contactRepository;

    // CREATE
    public Contact create(Contact contact) {
        return contactRepository.save(contact);
    }

    // READ - ALL
    public List<Contact> getAll() {
        return contactRepository.findAll();
    }

    // READ - BY ID
    public Contact getById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found"));
    }

    // UPDATE
    public Contact update(Long id, Contact updatedContact) {
        Contact existing = getById(id);

        existing.setOrganizationId(updatedContact.getOrganizationId());
        existing.setWaPhoneE164(updatedContact.getWaPhoneE164());
        existing.setWaId(updatedContact.getWaId());
        existing.setDisplayName(updatedContact.getDisplayName());
        existing.setSource(updatedContact.getSource());
        existing.setLastSeenAt(updatedContact.getLastSeenAt());

        return contactRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        Contact contact = getById(id);
        contactRepository.delete(contact);
    }
}