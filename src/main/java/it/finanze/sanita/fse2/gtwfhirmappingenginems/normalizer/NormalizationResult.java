package it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer;

import java.util.List;

import org.hl7.fhir.r4.model.Bundle;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;

public record NormalizationResult(Bundle bundle, List<DataQualityIssueDTO> issues) {}
