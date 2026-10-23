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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.validation.ValidationEngine;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.EngineCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FhirEngineFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON, proxyMode = ScopedProxyMode.TARGET_CLASS)
@RequiredArgsConstructor
public class NormativeR4ValidationRule implements ValidationRule {

	private volatile ValidationEngine engine;
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
			engine = FhirEngineFactory.createR4();
			initialized = true;
			log.info("Normative R4 validation engine initialization completed");
		} catch (Exception exception) {
			engine = null;
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

		try (ByteArrayInputStream input = new ByteArrayInputStream(context.json().getBytes(StandardCharsets.UTF_8))) {
			return ValidationOutcomeMapper.toDataQualityIssues(engine.validate(FhirFormat.JSON, input, null)).stream()
					.filter(issue -> issue.getSeverity() != null
							&& !IssueSeverity.INFORMATION.name().equals(issue.getSeverity())
							&& !IssueSeverity.SUCCESS.name().equals(issue.getSeverity())
							&& !IssueSeverity.NULL.name().equals(issue.getSeverity()))
					.toList();
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to perform normative R4 validation", exception);
		}

	}

}
