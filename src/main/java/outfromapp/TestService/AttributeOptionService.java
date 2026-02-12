package outfromapp.TestService;

import com.aigreentick.services.contacts.entity.AttributeOption;
import outfromapp.TestService.TestRepository.AttributeOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttributeOptionService {

    private final AttributeOptionRepository attributeOptionRepository;

    // CREATE
    public AttributeOption create(AttributeOption attributeOption) {
        return attributeOptionRepository.save(attributeOption);
    }

    // READ - ALL
    public List<AttributeOption> getAll() {
        return attributeOptionRepository.findAll();
    }

    // READ - BY ID
    public AttributeOption getById(Long id) {
        return attributeOptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AttributeOption not found"));
    }

    // UPDATE
    public AttributeOption update(Long id, AttributeOption updatedAttributeOption) {
        AttributeOption existing = getById(id);

        existing.setAttributeDefinitionId(updatedAttributeOption.getAttributeDefinitionId());
        existing.setOptionKey(updatedAttributeOption.getOptionKey());
        existing.setOptionLabel(updatedAttributeOption.getOptionLabel());
        existing.setSortOrder(updatedAttributeOption.getSortOrder());
        existing.setIsActive(updatedAttributeOption.getIsActive());

        return attributeOptionRepository.save(existing);
    }

    // DELETE
    public void delete(Long id) {
        AttributeOption attributeOption = getById(id);
        attributeOptionRepository.delete(attributeOption);
    }
}