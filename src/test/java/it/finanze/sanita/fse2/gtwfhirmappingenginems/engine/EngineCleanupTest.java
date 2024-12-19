
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

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractCleanupEngineTest;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConfigSRV;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
public class EngineCleanupTest extends AbstractCleanupEngineTest {

    private static final int DAYS_AFTER_INSERTION = 5;

    @MockBean
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
