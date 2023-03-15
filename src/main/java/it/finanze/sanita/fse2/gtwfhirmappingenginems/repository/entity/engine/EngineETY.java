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
