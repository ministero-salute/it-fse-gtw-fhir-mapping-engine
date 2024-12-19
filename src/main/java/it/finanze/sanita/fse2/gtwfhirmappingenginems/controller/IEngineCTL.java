
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller;

import io.swagger.v3.oas.annotations.Operation;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.EngRefreshResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.EngStatusResDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.RouteUtility.*;

@RequestMapping(ENGINE_MAPPER)
public interface IEngineCTL {

    @GetMapping(
        value = ENGINE_STATUS_API,
        produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    @Operation(summary = "Restituisce lo stato corrente degli engine disponibili")
    EngStatusResDTO status();

    @GetMapping(
        value = ENGINE_REFRESH_API,
        produces = { MediaType.APPLICATION_JSON_VALUE }
    )
    @Operation(summary = "Forza il refresh degli engine")
    EngRefreshResDTO run();

}
