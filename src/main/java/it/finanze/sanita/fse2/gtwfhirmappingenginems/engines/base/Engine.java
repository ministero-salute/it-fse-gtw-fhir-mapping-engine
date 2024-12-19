
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.data.RootData;
import lombok.Getter;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class Engine {

    private final String id;
    private final ConcurrentHashMap<String, RootData> roots;
    private final ConcurrentHashMap<String, String> files;

    private final CdaMappingEngine instance;
    private final Date insertion;

    public Engine(
        String id,
        Map<String, RootData> roots,
        Map<String, String> files,
        CdaMappingEngine instance
    ) {
        this.id = id;
        this.roots = new ConcurrentHashMap<>(roots);
        this.files = new ConcurrentHashMap<>(files);
        this.instance = instance;
        this.insertion = new Date();
    }
}
