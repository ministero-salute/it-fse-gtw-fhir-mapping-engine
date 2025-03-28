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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.base;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConfigSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.REMOVABLE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.raw.Fixtures.ENGINES;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.raw.Fixtures.TRANSFORM;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY.FIELD_ID;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
public abstract class AbstractEngineTest {

    protected static final OperationException TEST_OP_EX = new OperationException(
        "Test error",
        new MongoException("Test error")
    );

    protected static final RuntimeException TEST_RN_EX = new RuntimeException(
        "Test error"
    );

    @Autowired
    protected MongoTemplate mongo;

    @SpyBean
    protected IEngineRepo repository;

    @SpyBean
    protected IEngineSRV engines;

    @MockBean
    private IConfigSRV config;

    protected void initDb() throws IOException {
        // Reset data
        resetDb();
        // Read fixtures
        insert(ENGINES.asFreshDocuments(), EngineETY.class);
        insert(TRANSFORM.asDocuments(), TransformETY.class);
        // Mock gtw-config
        when(config.getRetentionDay()).thenReturn(5);
    }
    protected void initEngine() {
        // Run refresh
        engines.manager().refreshSync();
    }

    protected void awaitUntilEnginesSpawned() {
        await().atMost(5, TimeUnit.MINUTES).until(() -> !engines.manager().isRunning());
    }

    protected void awaitUntilEnginesReady() {
        await().atMost(1, TimeUnit.MINUTES).until(() -> engines.manager().isReady());
    }

    protected void dropUselessEngine() {
        EngineETY e = mongo.findAndRemove(new Query(where(FIELD_ID).is(REMOVABLE.engineId())), EngineETY.class);
        if(e == null) throw new IllegalStateException("Useless engine already removed");
    }
    protected void resetDb() {
        mongo.dropCollection(TransformETY.class);
        mongo.dropCollection(EngineETY.class);
    }

    protected void emulateStartUpMsEnginesAsync() {
        // Emulate the microservice restart
        log.debug("Emulating engine restart conditions");
        engines.manager().reset();
        engines.manager().refresh();
    }

    protected void emulateStartUpMsEnginesSync() {
        // Emulate the microservice restart
        log.debug("Emulating engine restart conditions (sync)");
        engines.manager().reset();
        engines.manager().refreshSync();
    }

    private void insert(List<Document> documents, Class<?> clazz) {
        for (Document doc : documents) {
            mongo.insert(doc, mongo.getCollectionName(clazz));
        }
    }
}
