
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

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractEngineTest;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.CDA.LAB;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.LAB_ENGINE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
class TransformTest extends AbstractEngineTest {

	@Autowired
	private ITransformerSRV service;

	@BeforeEach
    void onEachSetup() throws IOException {
        // Restore database
        initDb();
        // Reload engines (if required)
        initEngine();
    }
	
	@Test
	void transform() throws FHIRException, IOException {
		String json = service.transform(
			LAB.read(),
            LAB_ENGINE.engineId(),
            LAB_ENGINE.transformId(),
            null);

		assertNotNull(json);
	}
	
}
