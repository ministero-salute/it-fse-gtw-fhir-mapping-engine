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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.FhirTypeEnum;
import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

import static java.lang.String.format;

@Data
@Document(collection = "#{@transformBean}")
public class TransformETY {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_URI = "uri";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_FILENAME = "filename";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TEMPLATE_ID_ROOT = "template_id_root";
    public static final String FIELD_LAST_UPDATE = "last_update_date";
    public static final String FIELD_DELETED = "deleted";
    public static final String FIELD_LAST_SYNC = "last_sync";

    @Id
    private String id;

    @Field(FIELD_URI)
    private String uri;

    @Field(FIELD_VERSION)
    private String version;

    @Field(FIELD_TEMPLATE_ID_ROOT)
    private List<String> templateIdRoot;

    @Field(FIELD_FILENAME)
    private String filename;

    @Field(FIELD_CONTENT)
    private Binary content;

    @Field(FIELD_TYPE)
    private FhirTypeEnum type;

    @Field(FIELD_LAST_UPDATE)
    private Date lastUpdate;

    @Field(FIELD_DELETED)
    private boolean deleted;

    @Field(FIELD_LAST_SYNC)
    private Date lastSync;

    public String getFormattedUri() {
        return format("%s|%s", uri, version);
    }

}
