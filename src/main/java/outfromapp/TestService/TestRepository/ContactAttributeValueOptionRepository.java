package outfromapp.TestService.TestRepository;

import com.aigreentick.services.contacts.entity.ContactAttributeValueOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactAttributeValueOptionRepository extends JpaRepository<ContactAttributeValueOption, Long> {

    List<ContactAttributeValueOption> findByContactAttributeValueId(Long contactAttributeValueId);

    List<ContactAttributeValueOption> findByAttributeOptionId(Long attributeOptionId);
}