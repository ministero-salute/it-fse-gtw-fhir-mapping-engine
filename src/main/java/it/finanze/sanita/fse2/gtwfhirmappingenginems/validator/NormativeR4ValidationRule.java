package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.cli.model.CliContext;
import org.hl7.fhir.validation.cli.services.ValidationService;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.EngineCFG;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
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

	@Override
	public List<DataQualityIssueDTO> validate(ValidationContext context) {
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

	@Async(EngineCFG.VALIDATOR_EXECUTOR)
	@EventListener(ApplicationStartedEvent.class)
	public void initialize() {
		initializing = true;
		initialized = false;
		try {
			CliContext context = new CliContext();
			context.setSv("4.0");
			context.setTxServer(null);
			engine = new ValidationService().initializeValidator(
					context, "hl7.fhir.r4.core#4.0.1", new TimeTracker());
			initialized = true;
			log.info("Normative R4 validation engine initialization completed");
		} catch (Exception exception) {
			engine = null;
			log.error("Unable to initialize the normative R4 validation engine", exception);
		} finally {
			initializing = false;
		}
	}
}
