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

import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base.AbstractEngineTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.http.MockRequests.engines;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.http.MockRequests.refresh;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Profile.TEST;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles(TEST)
@TestInstance(PER_CLASS)
public class EngineControllerTest extends AbstractEngineTest {

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
    void statusOk() throws Exception {
        // Returns empty while loading engines
        mvc.perform(engines()).andExpectAll(
            status().is2xxSuccessful(),
            jsonPath("$.engines").isArray(),
            jsonPath("$.engines").isEmpty()
        );
        // We wait until the updating process has not finished.
        awaitUntilEnginesSpawned();
        // Returns content after loading engines
        mvc.perform(engines()).andExpectAll(
            status().is2xxSuccessful(),
            jsonPath("$.engines").isArray(),
            jsonPath("$.engines").isNotEmpty()
        );
        // We wait until the updating process has not finished.
        awaitUntilEnginesSpawned();
        // Returns content after loading engines (expect one less engine)
        mvc.perform(engines()).andExpectAll(
            status().is2xxSuccessful(),
            jsonPath("$.engines").isArray(),
            jsonPath("$.engines", hasSize(Engine.size() - 1))
        );
    }

    @Test
    void refreshOk() throws Exception {
        // Returns error while loading engines
        mvc.perform(refresh()).andExpect(status().isLocked());
        // We wait until the updating process has not finished.
        awaitUntilEnginesSpawned();
        // It should work
        mvc.perform(refresh()).andExpect(status().is2xxSuccessful());
    }

    @AfterAll
    void teardown() {
        resetDb();
    }

}
