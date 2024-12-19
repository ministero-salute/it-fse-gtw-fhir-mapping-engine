
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
