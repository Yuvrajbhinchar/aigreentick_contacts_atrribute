package outfromapp.TestService.TestController;

import com.aigreentick.services.contacts.entity.Contact;
import outfromapp.TestService.TContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class TContactController {

    private final TContactService contactService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Contact contact) {
        Contact saved = contactService.create(contact);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Contact> contacts = contactService.getAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", contacts.size(),
                "data", contacts
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactService.getById(id)
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Contact contact
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactService.update(id, contact)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Contact deleted successfully"
        ));
    }
}