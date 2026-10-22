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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.NoRecordFoundException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EngineSRV implements IEngineSRV {

    @Autowired
    private ProfileUtility profile;

    @Autowired
    private CdaEnginesManager manager;

    @Autowired 
    private IEngineRepo engineRepo;

    @EventListener(ApplicationStartedEvent.class)
    public void initialize() {
        // Prevent loading engines while running tests
        if(!profile.isTestProfile()) {
            // Create instances from database
            manager.refresh();
        } else {
            log.info("Skipping engine initialisation, using test profile");
        }
    }

    @Override
    public CdaEnginesManager manager() {
        return manager;
    }

    @Override
    public Pair<String, String> getStructureObjectID(final List<String> templateIds) {

        EngineETY latest;

        try {
            latest = engineRepo.getLatestEngine();
        } catch (Exception ex) {
            throw new BusinessException("Impossibile recuperare la structure-map nell'engine associato", ex);
        }

        if (latest == null) throw new NoRecordFoundException("Nessun engine disponibile");

        Optional<EngineMap> map = latest.getRoots().stream()
                .filter(r -> templateIds.stream().anyMatch(id -> r.getRoot().contains(id)))
                .findFirst();

        if (!map.isPresent()) {
            throw new NoRecordFoundException(
                String.format("Nessuna mappa con ids %s è stata trovata nell'engine %s", templateIds, latest.getId())
            );
        }

        return Pair.of(latest.getId(), map.get().getOid().toString());
    }
}
