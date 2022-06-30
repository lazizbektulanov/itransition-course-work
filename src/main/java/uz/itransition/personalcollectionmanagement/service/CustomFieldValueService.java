package uz.itransition.personalcollectionmanagement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.itransition.personalcollectionmanagement.entity.CustomField;
import uz.itransition.personalcollectionmanagement.entity.CustomFieldValue;
import uz.itransition.personalcollectionmanagement.entity.Item;
import uz.itransition.personalcollectionmanagement.repository.CustomFieldRepository;
import uz.itransition.personalcollectionmanagement.repository.CustomFieldValueRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomFieldValueService {

    private final CustomFieldRepository customFieldRepository;

    private final CustomFieldValueRepository customFieldValueRepository;

    public void saveItemCustomFieldValues(HttpServletRequest request, Item savedItem) {
        List<CustomField> itemCustomFields =
                customFieldRepository.findByCollectionId(savedItem.getCollection().getId());
        List<CustomFieldValue> itemCustomFieldValues = new ArrayList<>();
        for (CustomField itemCustomField : itemCustomFields) {
            itemCustomFieldValues.add(new CustomFieldValue(
                    request.getParameter(itemCustomField.getFieldName()),
                    savedItem,
                    itemCustomField
            ));
        }
        customFieldValueRepository.saveAll(itemCustomFieldValues);
    }
}