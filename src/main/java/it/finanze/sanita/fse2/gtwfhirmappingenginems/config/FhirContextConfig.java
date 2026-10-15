package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.uhn.fhir.context.FhirContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for reusable singleton FhirContext R4.
 */
@Configuration
@Slf4j
public class FhirContextConfig {

    @Bean
    public FhirContext fhirContext() {
        log.info("Initializing FhirContext R4 singleton...");
        long startTime = System.currentTimeMillis();
        
        FhirContext ctx = FhirContext.forR4();
        ctx.getParserOptions().setStripVersionsFromReferences(false);
        ctx.getParserOptions().setOverrideResourceIdWithBundleEntryFullUrl(false);
        
        ctx.getResourceDefinition("Bundle");
        ctx.getResourceDefinition("Observation");
        ctx.getResourceDefinition("Patient");
        ctx.getResourceDefinition("Condition");
        ctx.getResourceDefinition("Procedure");
        ctx.getResourceDefinition("DiagnosticReport");
        ctx.getResourceDefinition("Composition");
        ctx.getResourceDefinition("Organization");
        ctx.getResourceDefinition("Practitioner");
        ctx.getResourceDefinition("DocumentReference");
        
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("FhirContext R4 singleton initialized successfully in {}ms", elapsedTime);
        
        return ctx;
    }
}
