package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import java.io.IOException;
import java.net.URISyntaxException;

import ch.ahdis.matchbox.engine.CdaMappingEngine;

public final class FhirEngineFactory {

    private FhirEngineFactory() {
    }

    public static synchronized CdaMappingEngine createR4() throws IOException, URISyntaxException {
        // FHIR engine construction accesses shared, non-thread-safe library caches.
        // Use one lock for both mapping and normative validation initialization.
        return new CdaMappingEngine.CdaMappingEngineBuilder().getCdaEngineR4();
    }
}
