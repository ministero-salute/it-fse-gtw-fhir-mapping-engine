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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import java.util.List;

import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.EngineCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class NormativeR4ValidationRule implements ValidationRule {

	private final FhirContext fhirContext;
	private volatile FhirValidator validator;
	@Getter

	private volatile boolean initializing;

	@Getter
	private volatile boolean initialized;

	@Override
	public ValidationType type() {
		return ValidationType.NORMATIVE_R4;
	}

	@Async(EngineCFG.VALIDATOR_EXECUTOR)
	@EventListener(ApplicationStartedEvent.class)
	public void initialize() {

		initializing = true;
		initialized = false;
		try {
			ValidationSupportChain supportChain = new ValidationSupportChain(
					new DefaultProfileValidationSupport(fhirContext),
					new InMemoryTerminologyServerValidationSupport(fhirContext),
					new CommonCodeSystemsTerminologyService(fhirContext));
			FhirValidator r4Validator = fhirContext.newValidator();
			r4Validator.registerValidatorModule(new FhirInstanceValidator(supportChain));
			validator = r4Validator;
			initialized = true;
			log.info("Normative R4 validation engine initialization completed");
		} catch (Exception exception) {
			validator = null;
			log.error("Unable to initialize the normative R4 validation engine", exception);
		} finally {
			initializing = false;
		}
	}

	@Override
	public List<ValidationIssueDTO> validate(ValidationContext context) {

		if (!initialized) {
			throw new EngineInitException("The normative R4 validation engine is not ready");
		}

		try {
			// Retain warnings for diagnostics; only ERROR and FATAL affect validity.
			return validator.validateWithResult(context.json()).getMessages().stream()
					.filter(message -> message.getSeverity() == ResultSeverityEnum.WARNING
							|| message.getSeverity() == ResultSeverityEnum.ERROR
							|| message.getSeverity() == ResultSeverityEnum.FATAL)
					.map(message -> new ValidationIssueDTO(message.getSeverity().name(),
							"INVALID",
							message.getLocationString() == null ? List.of() : List.of(message.getLocationString()),
							message.getMessage()))
					.toList();
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to perform normative R4 validation", exception);
		}

	}

}
