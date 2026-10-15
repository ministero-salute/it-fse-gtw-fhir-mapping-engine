package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import java.util.List;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;

public interface ValidationRule {
	ValidationType type();
	List<DataQualityIssueDTO> validate(ValidationContext context);
}
