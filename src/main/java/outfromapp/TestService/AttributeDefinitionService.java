package outfromapp.TestService;

import com.aigreentick.services.contacts.entity.AttributeDefinition;
import outfromapp.TestService.TestRepository.TAttributeDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeDefinitionService {

    private final TAttributeDefinitionRepository attributeDefinitionRepository;

    // CREATE
    public AttributeDefinition create(AttributeDefinition attributeDefinition) {
        return attributeDefinitionRepository.save(attributeDefinition);
    }

    // READ - ALL
    public List<AttributeDefinition> getAll() {
        return attributeDefinitionRepository.findAll();
    }

    // READ - BY ID
    public AttributeDefinition getById(Long id) {
        return attributeDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AttributeDefinition not found"));
    }

    // UPDATE
    public AttributeDefinition update(Long id, AttributeDefinition updatedAttributeDefinition) {
        AttributeDefinition existing = getById(id);

        existing.setOrganizationId(updatedAttributeDefinition.getOrganizationId());
        existing.setAttrKey(updatedAttributeDefinition.getAttrKey());
        existing.setLabel(updatedAttributeDefinition.getLabel());
        existing.setCategory(updatedAttributeDefinition.getCategory());
        existing.setDataType(updatedAttributeDefinition.getDataType());
        existing.setIsEditable(updatedAttributeDefinition.getIsEditable());
        existing.setIsRequired(updatedAttributeDefinition.getIsRequired());
        existing.setIsSearchable(updatedAttributeDefinition.getIsSearchable());
        existing.setDefaultValueJson(updatedAttributeDefinition.getDefaultValueJson());
        existing.setValidationJson(updatedAttributeDefinition.getValidationJson());

        return attributeDefinitionRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        AttributeDefinition attributeDefinition = getById(id);
        attributeDefinitionRepository.delete(attributeDefinition);
    }
}