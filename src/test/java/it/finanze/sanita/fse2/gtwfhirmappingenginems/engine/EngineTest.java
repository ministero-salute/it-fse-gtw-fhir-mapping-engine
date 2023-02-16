package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.CDA.LAB;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.INVALID;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.LAB_ENGINE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
public class EngineTest extends AbstractEngineTest {

    @BeforeAll
    void setup() throws IOException {
        initDb();
        initEngine();
    }

    @Test
    void transform() {
        assertDoesNotThrow(() -> {
            Bundle bundle = engines.manager().transform(
                LAB.read(),
                LAB_ENGINE.engineId(),
                LAB_ENGINE.transformId()
            );
            assertNotNull(bundle);
        });
    }

    @Test
    void transformWithInvalidEngineId() {
        assertThrows(EngineException.class, () -> {
            engines.manager().transform(
                LAB.read(),
                INVALID.engineId(),
                LAB_ENGINE.transformId()
            );
        });
    }

    @Test
    void transformWithInvalidTransformId() {
        assertThrows(EngineException.class, () -> {
            engines.manager().transform(
                LAB.read(),
                LAB_ENGINE.engineId(),
                INVALID.transformId()
            );
        });
    }

    @Test
    void removeEngineFromMemoryIfDropped() {
        assertEquals(Engine.size(), engines.manager().engines().size());
        // Emulate gtw-garbage behaviour
        dropUselessEngine();
        // Emulate auto-refresh scheduler
        engines.manager().refreshSync();
        assertEquals(Engine.size() - 1, engines.manager().engines().size());
    }

    @AfterAll
    void teardown() {
        resetDb();
    }

}
