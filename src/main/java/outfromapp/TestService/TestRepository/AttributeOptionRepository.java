package outfromapp.TestService.TestRepository;

import com.aigreentick.services.contacts.entity.AttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttributeOptionRepository extends JpaRepository<AttributeOption, Long> {

    List<AttributeOption> findByAttributeDefinitionId(Long attributeDefinitionId);

    List<AttributeOption> findByIsActive(Boolean isActive);
}