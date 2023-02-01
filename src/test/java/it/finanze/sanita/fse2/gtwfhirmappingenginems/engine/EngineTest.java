package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static ch.ahdis.matchbox.engine.CdaMappingEngine.CdaMappingEngineBuilder;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST_ENGINE;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles({TEST_ENGINE, TEST})
@TestInstance(PER_CLASS)
public class EngineTest extends AbstractEngineTest {

    private JsonParser parser;

    @BeforeAll
    public void setup() {
        parser = new JsonParser();
    }

    @Test
    public void testFullBodyEmpty() throws IOException, URISyntaxException {
        // Load engine
        CdaMappingEngine engine = new CdaMappingEngineBuilder().getEngine();
        log.info("{}", engine.getContext().listMapUrls());
        // Run
        setupFullBody(engine);
    }

    @Test
    public void testFullBodyMinimal() throws IOException, URISyntaxException {
        // Load engine
        CdaMappingEngine engine = new CdaMappingEngineBuilder().getEngine(ENGINE_MINIMAL);
        log.info("{}", engine.getContext().listMapUrls());
        // Run
        setupFullBody(engine);
    }

    @Test
    public void overrideDepsResource() throws IOException, URISyntaxException {
        // Load engine
        CdaMappingEngine engine = new CdaMappingEngineBuilder().getEngine();

        log.info("[DEFS] {}", engine.getContext().listStructures().stream().map(s -> s.getName()).collect(Collectors.toList()));

        StructureDefinition res0 = (StructureDefinition) parser.parse(new FileInputStream(DEPS_R4_SUBSTANCE_MODIFIED_JSON));
        // Add
        engine.addCanonicalResource(res0);

        log.info("[DEFS] {}", engine.getContext().listStructures().stream().map(s -> s.getName()).collect(Collectors.toList()));

    }

    @Test
    public void loadSameUrlSameVersion() throws IOException, URISyntaxException {
        // Load engine
        CdaMappingEngine engine = new CdaMappingEngineBuilder().getEngine(ENGINE_FULL);
        // Adds a canonical resource to the loaded packages,
        // please note that it will replace a resource with
        // the same canonical url
        StructureMap res0 = (StructureMap) parser.parse(new FileInputStream(RES_CDA_TO_FHIR_DATATYPE_JSON));
        StructureMap res1 = (StructureMap) parser.parse(new FileInputStream(RES_CDA_TO_FHIR_DATATYPE_JSON));

        log.info("Look for: {}", res0.getName());
        log.info("Before / {}", engine.getContext().listMapUrls());
        // Add
        engine.addCanonicalResource(res0);
        engine.addCanonicalResource(res1);

        log.info("After / {}", engine.getContext().listMapUrls());
    }

    @Test
    public void loadSameUrlDifferentVersion() throws IOException, URISyntaxException {
        // Load engine
        CdaMappingEngine engine = new CdaMappingEngineBuilder().getEngine(ENGINE_FULL);
        // Adds a canonical resource to the loaded packages,
        // please note that it will replace a resource with
        // the same canonical url
        StructureMap res0 = (StructureMap) parser.parse(new FileInputStream(RES_CDA_TO_FHIR_DATATYPE_JSON));
        StructureMap res1 = (StructureMap) parser.parse(new FileInputStream(RES_CDA_TO_FHIR_DATATYPE_JSON));
        // There is also no expectation that versions can be placed in a lexicographical sequence.
        res1.setVersion("2.X");
        log.info("Look for: {}", res0.getName());
        log.info("Before / {}", engine.getContext().listMapUrls());
        // Add
        engine.addCanonicalResource(res0);
        engine.addCanonicalResource(res1);

        log.info("After / {}", engine.getContext().listMapUrls());
    }

    private void setupFullBody(CdaMappingEngine engine) throws IOException {
        // Log resources
        // log.info("[MAPS] {}", engine.getContext().listMapUrls());
        // log.info("[DEFS] {}", engine.getContext().listStructures());
        // Load deps
        StructureMap res0 = (StructureMap) parser.parse(new FileInputStream(RES_CDA_TO_FHIR_DATATYPE_JSON));
        StructureMap res1 = (StructureMap) parser.parse(new FileInputStream(RES_FULL_HEADER_JSON));
        // Load map
        Path p = Paths.get(RES_FULL_BODY_MAP);
        StructureMap map = engine.parseMap(Files.readString(p));
        log.info("Map: {} / {} / {}", map.getName(), map.getVersion(), map.getUrl());
        // Add resources
        engine.addCanonicalResource(res0);
        engine.addCanonicalResource(res1);
        engine.addCanonicalResource(map);
        // Log resources
        // log.info("[MAPS] {}", engine.getContext().listMapUrls());
        // log.info("[DEFS] {}", engine.getContext().listStructures());
        // Load XML
        p = Paths.get(INPUT_LAB_XML);
        // Transform
        Bundle b = engine.transformCdaToFhir(Files.readString(p), map.getUrl());
        // Print
        log.info("{}", parser.composeString(b));
    }

}
