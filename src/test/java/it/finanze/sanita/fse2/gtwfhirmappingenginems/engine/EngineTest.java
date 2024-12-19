
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractEngineTest;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
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
