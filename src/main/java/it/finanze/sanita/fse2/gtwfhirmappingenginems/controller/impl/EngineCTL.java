
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
