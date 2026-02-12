package outfromapp.TestService.TestRepository;

import com.aigreentick.services.contacts.entity.ContactAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TContactAttributeValueRepository extends JpaRepository<ContactAttributeValue, Long> {

    List<ContactAttributeValue> findByContactId(Long contactId);

    List<ContactAttributeValue> findByAttributeDefinitionId(Long attributeDefinitionId);
}