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

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractCleanupEngineTest {

    @Autowired
    protected MongoTemplate mongo;

    @SpyBean
    protected IEngineRepo repository;

    @SpyBean
    protected IEngineSRV service;


    protected void resetDb() {
        mongo.dropCollection(EngineETY.class);
    }

    protected void spawnFakeEngines(int size, int daysAfterInsertion) {
        Date current = calculateDate(daysAfterInsertion);
        List<EngineETY> engines = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            EngineETY eng = new EngineETY();
            eng.setLastSync(current);
            engines.add(eng);
        }
        mongo.insertAll(engines);
    }

    private Date calculateDate(int daysAfterInsertion) {
        return Date.from(
            LocalDateTime.of(
                LocalDate.now().minusDays(daysAfterInsertion+1), LocalTime.MIN
            ).toInstant(ZoneOffset.UTC)
        );
    }
}
