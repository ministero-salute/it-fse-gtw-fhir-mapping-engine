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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub.EngineMap;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.NoRecordFoundException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

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
