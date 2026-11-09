package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import org.bson.Document;

/** Snapshots from one mapping invocation, before and after Bundle post-processing. */
public record DiagnosticTransformResDTO(Document preProcessingBundle, Document postProcessingBundle) {}
