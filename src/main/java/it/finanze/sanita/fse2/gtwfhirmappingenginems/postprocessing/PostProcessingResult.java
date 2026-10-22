package it.finanze.sanita.fse2.gtwfhirmappingenginems.postprocessing;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;

/** Final Bundle and diagnostics produced by post-processing. */
public record PostProcessingResult(Bundle bundle, List<DataQualityIssueDTO> issues) {}
