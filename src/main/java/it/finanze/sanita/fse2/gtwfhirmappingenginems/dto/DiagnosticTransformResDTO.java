package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import org.bson.Document;

/** Mapping snapshots and validation results for the post-processed Bundle. */
public record DiagnosticTransformResDTO(Document preProcessingBundle, Document postProcessingBundle,
        FhirValidationDTO validationResponse) {}
