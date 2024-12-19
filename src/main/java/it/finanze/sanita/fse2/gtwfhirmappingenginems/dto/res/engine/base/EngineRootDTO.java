
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.base;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.data.RootData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map.Entry;

@Getter
@AllArgsConstructor
public class EngineRootDTO {
    private final String id;
    private final String uri;
    private final List<String> templates;

    public EngineRootDTO(Entry<String, RootData> pair) {
        this.id = pair.getKey();
        this.uri = pair.getValue().getUri();
        this.templates = pair.getValue().getTemplates();
    }

}
