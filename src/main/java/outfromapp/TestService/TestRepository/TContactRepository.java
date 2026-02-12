package outfromapp.TestService.TestRepository;

import com.aigreentick.services.contacts.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByOrganizationId(Long organizationId);
    Optional<Contact> findByWaPhoneE164(String waPhoneE164);
}