package outfromapp.TestService.TestController;

import com.aigreentick.services.contacts.entity.ProjectContact;
import outfromapp.TestService.ProjectContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/project-contacts")
@RequiredArgsConstructor
public class ProjectContactController {

    private final ProjectContactService projectContactService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProjectContact projectContact) {
        ProjectContact saved = projectContactService.create(projectContact);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<ProjectContact> projectContacts = projectContactService.getAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", projectContacts.size(),
                "data", projectContacts
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", projectContactService.getById(id)
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody ProjectContact projectContact
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", projectContactService.update(id, projectContact)
        ));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        projectContactService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "ProjectContact deleted successfully"
        ));
    }
}