/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirValidationResponseDTO;
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
	public FhirValidationResponseDTO validate(String json, List<ValidationType> validationTypes) {

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

		return new FhirValidationResponseDTO(results);
	}

}
