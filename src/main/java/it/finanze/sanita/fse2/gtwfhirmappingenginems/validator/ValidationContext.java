package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import org.hl7.fhir.r4.model.Bundle;

public record ValidationContext(String json, Bundle bundle) {}
