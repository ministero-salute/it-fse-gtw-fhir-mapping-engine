package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base;

import lombok.Data;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class MapETY {
    public static final String FIELD_NAME = "name_map";
    public static final String FIELD_FILENAME = "filename_map";
    public static final String FIELD_CONTENT = "content_map";

    @Field(FIELD_NAME)
    private String name;

    @Field(FIELD_FILENAME)
    private String filename;

    @Field(FIELD_CONTENT)
    private Binary content;
}
