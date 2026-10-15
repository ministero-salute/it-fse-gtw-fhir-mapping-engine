package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityValidationResponseDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationResultDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IValidateSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.validator.ValidationContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.validator.ValidationRule;

@Service
public class ValidateSRV implements IValidateSRV {
	private final FhirContext fhirContext;
	private final Map<ValidationType, ValidationRule> rules = new EnumMap<>(ValidationType.class);

	public ValidateSRV(FhirContext fhirContext, List<ValidationRule> rules) {
		this.fhirContext = fhirContext;
		rules.forEach(rule -> this.rules.put(rule.type(), rule));
	}

	@Override
	public DataQualityValidationResponseDTO validate(String json, List<ValidationType> validationTypes) {
		if (validationTypes == null || validationTypes.isEmpty()) {
			return null;
		}
		Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, json);
		ValidationContext context = new ValidationContext(json, bundle);
		List<ValidationResultDTO> results = validationTypes.stream().distinct().map(type -> {
			ValidationRule rule = rules.get(type);
			if (rule == null) {
				throw new IllegalArgumentException("Unsupported validation type: " + type);
			}
			return new ValidationResultDTO(type, rule.validate(context));
		}).toList();
		return new DataQualityValidationResponseDTO(results);
	}
}
