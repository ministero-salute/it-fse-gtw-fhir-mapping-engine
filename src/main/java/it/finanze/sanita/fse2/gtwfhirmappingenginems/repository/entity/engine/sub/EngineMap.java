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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import static java.lang.String.format;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EngineMap {

    public static final String FIELD_OID = "oid";
    public static final String FIELD_ROOT = "template_id_root";
    public static final String FIELD_URI = "uri";
    public static final String FIELD_VERSION = "version";


    @Field(FIELD_OID)
    private ObjectId oid;

    @Field(FIELD_ROOT)
    private List<String> root;

    @Field(FIELD_URI)
    private String uri;

    @Field(FIELD_VERSION)
    private String version;

    public String getFormattedUri() {
        return format("%s|%s", uri, version);
    }

}
