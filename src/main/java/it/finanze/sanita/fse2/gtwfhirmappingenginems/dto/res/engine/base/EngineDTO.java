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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.base;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.Engine;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class EngineDTO {
    private final String id;
    private final List<EngineRootDTO> roots;
    private final List<EngineFileDTO> files;
    private final Date insertion;

    public EngineDTO(Engine engine) {
        this.id = engine.getId();
        this.roots = engine.getRoots().entrySet().stream().map(EngineRootDTO::new).collect(Collectors.toList());
        this.files = engine.getFiles().entrySet().stream().map(EngineFileDTO::new).collect(Collectors.toList());
        this.insertion = engine.getInsertion();
    }

}
