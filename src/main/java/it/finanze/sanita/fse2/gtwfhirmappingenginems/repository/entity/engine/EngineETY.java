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
