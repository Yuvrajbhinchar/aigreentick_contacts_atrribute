package outfromapp.TestService.TestRepository;

import com.aigreentick.services.contacts.entity.ProjectContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectContactRepository extends JpaRepository<ProjectContact, Long> {

    List<ProjectContact> findByProjectId(Long projectId);

    List<ProjectContact> findByContactId(Long contactId);
}