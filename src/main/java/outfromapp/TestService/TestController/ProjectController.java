package outfromapp.TestService.TestController;

import com.aigreentick.services.contacts.entity.Project;
import outfromapp.TestService.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // CREATE
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Project project) {
        Project saved = projectService.create(project);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", saved
        ));
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Project> projects = projectService.getAll();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", projects.size(),
                "data", projects
        ));
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", projectService.getById(id)
        ));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestBody Project project
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", projectService.update(id, project)
        ));
    }

    // DELETE (Soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Project deleted successfully"
        ));
    }
}
