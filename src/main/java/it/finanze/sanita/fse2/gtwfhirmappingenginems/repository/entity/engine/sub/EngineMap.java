/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
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
