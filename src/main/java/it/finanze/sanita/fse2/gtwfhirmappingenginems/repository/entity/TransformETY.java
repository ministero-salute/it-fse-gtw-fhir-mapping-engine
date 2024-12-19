
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
