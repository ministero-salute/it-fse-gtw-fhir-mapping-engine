package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import java.util.List;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;

public interface IValidateSRV {
	DataQualityValidationResponseDTO validate(String json, List<ValidationType> validationTypes);
}
