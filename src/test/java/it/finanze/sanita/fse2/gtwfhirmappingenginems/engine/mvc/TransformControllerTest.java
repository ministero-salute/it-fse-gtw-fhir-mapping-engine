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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.mvc;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.FhirResourceDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractEngineTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.CDA.LAB;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.LAB_ENGINE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.http.MockRequests.transform;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.http.MockRequests.transformStateless;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.apache.http.HttpStatus.SC_SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
public class TransformControllerTest extends AbstractEngineTest {

    @Autowired
    private MockMvc mvc;

    @BeforeAll
    void setup() throws IOException {
        initDb();
    }

    @BeforeEach
    void onEachSetup() {
        emulateStartUpMsEnginesAsync();
    }

    @Test
    void transformOk() throws Exception {
        // Transform returns 200 even on erroneous TX
        mvc.perform(
            transform(
                new FhirResourceDTO(
                    null,
                    LAB.read(),
                    LAB_ENGINE.transformId(),
                    LAB_ENGINE.engineId()
                )
            )
        ).andExpectAll(
            status().is2xxSuccessful(),
            jsonPath("$.errorMessage").isNotEmpty()
        );
        // We wait until the updating process has not finished.
        awaitUntilEnginesSpawned();
        awaitUntilEnginesReady();
        // Transform returns 200 even on erroneous TX
        mvc.perform(
            transform(
                new FhirResourceDTO(
                    null,
                    LAB.read(),
                    LAB_ENGINE.transformId(),
                    LAB_ENGINE.engineId()
                )
            )
        ).andExpectAll(
            status().is2xxSuccessful(),
            jsonPath("$.json").isNotEmpty(),
            jsonPath("$.errorMessage").doesNotExist()
        );
    }

    @Test
    void transformStatelessOk() throws Exception {
        // Expect service unavailable while starting up
        mvc.perform(
            transformStateless(LAB_ENGINE, LAB)
        ).andExpect(status().is(SC_SERVICE_UNAVAILABLE));
        // We wait until the updating process has not finished.
        awaitUntilEnginesSpawned();
        awaitUntilEnginesReady();
        // Now we should be able to process the request
        mvc.perform(
            transformStateless(LAB_ENGINE, LAB)
        ).andExpect(status().is2xxSuccessful());
        // Mock an error
        doThrow(TEST_RN_EX).when(engines).manager();
        // Now we should expect an error
        mvc.perform(
            transformStateless(LAB_ENGINE, LAB)
        ).andExpect(status().is5xxServerError());
    }

    @AfterAll
    void teardown() {
        resetDb();
    }

}
