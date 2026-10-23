package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import java.util.List;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;

public interface IValidateSRV {
	FhirValidationResponseDTO validate(String json, List<ValidationType> validationTypes);
}
