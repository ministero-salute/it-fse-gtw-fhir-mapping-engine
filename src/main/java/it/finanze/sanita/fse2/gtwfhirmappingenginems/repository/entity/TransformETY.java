package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.DefinitionETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.MapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.ValuesetETY;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "#{@transformBean}")
public class TransformETY {

    public static final String FIELD_ID = "_id";
    public static final String FIELD_MAPS = "maps";
    public static final String FIELD_DEFINITIONS = "definitions";
    public static final String FIELD_VALUESETS = "valuesets";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_TEMPLATE_ID_ROOT = "template_id_root";
    public static final String FIELD_LAST_UPDATE = "last_update_date";
    public static final String FIELD_ROOT_MAP = "root_map";
    public static final String FIELD_DELETED = "deleted";
    public static final String FIELD_LAST_SYNC = "last_sync";


    @Id
    private String id;

    @Field(FIELD_TEMPLATE_ID_ROOT)
    private String templateIdRoot;

    @Field(FIELD_DEFINITIONS)
    private List<DefinitionETY> definitions;

    @Field(FIELD_MAPS)
    private List<MapETY> maps;

    @Field(FIELD_VALUESETS)
    private List<ValuesetETY> valuesets;

    @Field(FIELD_VERSION)
    private String version;

    @Field(FIELD_ROOT_MAP)
    private String rootMapName;

    @Field(FIELD_DELETED)
    private boolean deleted;

    @Field(FIELD_LAST_UPDATE)
    private Date lastUpdate;

    public MapETY getRootMap() {
        return maps.get(0);
    }

}
