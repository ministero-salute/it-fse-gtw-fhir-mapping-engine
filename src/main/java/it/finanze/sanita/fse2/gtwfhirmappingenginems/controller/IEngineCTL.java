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
