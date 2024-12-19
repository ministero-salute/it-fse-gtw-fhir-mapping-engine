
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
