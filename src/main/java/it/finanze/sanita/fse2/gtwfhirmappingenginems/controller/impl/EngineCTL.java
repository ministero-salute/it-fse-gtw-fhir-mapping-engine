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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.IEngineCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.EngRefreshResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.EngStatusResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.base.EngineDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineSchedulerException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.DTO_RUN_TASK_QUEUED;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.ERR_SCH_RUNNING;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.ENGINE_TAG;

@Tag(name = ENGINE_TAG)
@RestController
public class EngineCTL extends AbstractCTL implements IEngineCTL {

    @Autowired
    private IEngineSRV srv;

    @Override
    public EngStatusResDTO status() {
        List<EngineDTO> engines = new ArrayList<>();
        srv.manager().engines().forEach((id, engine) -> engines.add(new EngineDTO(engine)));
        return new EngStatusResDTO(engines);
    }

    @Override
    public EngRefreshResDTO run() {
        // Safety check
        if(srv.manager().isRunning()) throw new EngineSchedulerException(ERR_SCH_RUNNING);
        // Spawn process
        srv.manager().refresh();
        // Return response
        return new EngRefreshResDTO(getLogTraceInfo(), DTO_RUN_TASK_QUEUED);
    }
}
