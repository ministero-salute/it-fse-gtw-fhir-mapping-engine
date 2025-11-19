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

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractCleanupEngineTest;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConfigSRV;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
public class EngineCleanupTest extends AbstractCleanupEngineTest {

    private static final int DAYS_AFTER_INSERTION = 5;

    @MockitoBean
    private IConfigSRV config;

    @BeforeEach
    void onEachSetup() {
        // Restore database
        resetDb();
    }

    @Test
    @DisplayName("Do not remove any engine (none available)")
    void emptyEngines() throws OperationException {
        when(config.getRetentionDay()).thenReturn(DAYS_AFTER_INSERTION);
        assertTrue(repository.find().isEmpty(), "engines should not be inserted before testing the empty deletion");
        assertDoesNotThrow(() -> service.manager().cleanup());
        assertTrue(repository.find().isEmpty());
    }

    @Test
    @DisplayName("Do not remove any engine (only one available)")
    void noEngines() throws OperationException {
        when(config.getRetentionDay()).thenReturn(DAYS_AFTER_INSERTION);
        final int size = 1;
        spawnFakeEngines(size, DAYS_AFTER_INSERTION);
        assertFalse(repository.find().isEmpty(), "engines should be inserted before testing the deletion");
        assertDoesNotThrow(() -> service.manager().cleanup());
        assertEquals(size, repository.find().size());
    }

    @Test
    @DisplayName("Do not remove any engine (none expired)")
    void noEnginesExpired() throws OperationException {
        when(config.getRetentionDay()).thenReturn(DAYS_AFTER_INSERTION);
        final int size = 5;
        spawnFakeEngines(size, DAYS_AFTER_INSERTION - 2);
        assertFalse(repository.find().isEmpty(), "engines should be inserted before testing the deletion");
        assertDoesNotThrow(() -> service.manager().cleanup());
        assertEquals(size, repository.find().size());
    }

    @Test
    @DisplayName("Remove expired engines (not unexpired one)")
    void multipleEngines() throws OperationException {
        when(config.getRetentionDay()).thenReturn(DAYS_AFTER_INSERTION);

        final int unexpired = 5;
        final int expired = 10;

        spawnFakeEngines(unexpired, DAYS_AFTER_INSERTION - 2);
        spawnFakeEngines(expired, DAYS_AFTER_INSERTION);

        assertFalse(repository.find().isEmpty(), "engines should be inserted before testing the deletion");
        assertDoesNotThrow(() -> service.manager().cleanup());
        assertEquals(unexpired,  repository.find().size());
    }

    @AfterAll
    void teardown() {
        resetDb();
    }

}
