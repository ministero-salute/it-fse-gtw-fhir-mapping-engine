/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractEngineTest;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.CDA.LAB;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.INVALID;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.LAB_ENGINE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
public class EngineTest extends AbstractEngineTest {

    @BeforeEach
    void onEachSetup() throws IOException {
        // Restore database
        initDb();
        // Reload engines (if required)
        initEngine();
    }

    @Test
    @Disabled
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

    @Test
    void removeEngineFromMemoryIfUnavailable() {
        assertDoesNotThrow(()-> {
            // Mock an error on MongoDB (just one time)
            when(repository.enable(anyString())).thenThrow(TEST_OP_EX).thenCallRealMethod();
            // Unload engines from memory, reset start flag and refresh (sync)
            emulateStartUpMsEnginesSync();
            // Verify we got N-1 engines
            assertEquals(Engine.size() - 1, engines.manager().engines().size());
        });
    }

    @AfterAll
    void teardown() {
        resetDb();
    }

}
