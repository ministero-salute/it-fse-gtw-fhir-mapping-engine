
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub.EngineMap;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Document(collection = "#{@engineBean}")
@Data
@NoArgsConstructor
public class EngineETY {

    public static final int MIN_ENGINE_AVAILABLE = 1;

    public static final String FIELD_ID = "_id";
    public static final String FIELD_ROOTS = "roots";
    public static final String FIELD_FILES = "files";
    public static final String FIELD_LAST_SYNC = "last_sync";
    public static final String FIELD_EXPIRED = "expired";
    public static final String FIELD_AVAILABLE = "available";

    @Id
    private String id;
    @Field(FIELD_ROOTS)
    private List<EngineMap> roots;
    @Field(FIELD_FILES)
    private List<ObjectId> files;
    @Field(FIELD_LAST_SYNC)
    private Date lastSync;
    @Field(FIELD_AVAILABLE)
    private boolean available;

}
