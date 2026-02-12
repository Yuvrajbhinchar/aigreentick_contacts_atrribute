package outfromapp.TestService.TestRepository;

import com.aigreentick.services.contacts.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TAttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {

    List<AttributeDefinition> findByOrganizationId(Long organizationId);

    List<AttributeDefinition> findByCategory(AttributeDefinition.Category category);
}