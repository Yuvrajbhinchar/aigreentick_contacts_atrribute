package outfromapp.TestService.TestController;

import com.aigreentick.services.contacts.entity.ContactNote;
import outfromapp.TestService.ContactNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contact-notes")
@RequiredArgsConstructor
public class ContactNoteController {

    private final ContactNoteService contactNoteService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ContactNote contactNote) {
        ContactNote saved = contactNoteService.create(contactNote);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<ContactNote> contactNotes = contactNoteService.getAll();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", contactNotes.size(),
                "data", contactNotes
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactNoteService.getById(id)
        ));
    }

    // READ BY CONTACT
    @GetMapping("/contact/{contactId}")
    public ResponseEntity<?> getByContactId(@PathVariable Long contactId) {
        List<ContactNote> notes = contactNoteService.getByContactId(contactId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", notes.size(),
                "data", notes
        ));
    }

    // READ BY PROJECT AND CONTACT
    @GetMapping("/project/{projectId}/contact/{contactId}")
    public ResponseEntity<?> getByProjectIdAndContactId(
            @PathVariable Long projectId,
            @PathVariable Long contactId) {
        List<ContactNote> notes = contactNoteService.getByProjectIdAndContactId(projectId, contactId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", notes.size(),
                "data", notes
        ));
    }

    // READ BY ORGANIZATION
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<?> getByOrganizationId(@PathVariable Long organizationId) {
        List<ContactNote> notes = contactNoteService.getByOrganizationId(organizationId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", notes.size(),
                "data", notes
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ContactNote contactNote) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", contactNoteService.update(id, contactNote)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        contactNoteService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ContactNote deleted successfully"
        ));
    }
}