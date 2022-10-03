//package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity;
//
//import java.util.Date;
//import java.util.List;
//
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.data.mongodb.core.mapping.Field;
//
//import it.finanze.sanita.fse2.ms.gtw.rulesmanager.repository.entity.structures.StructureDefinitionETY;
//import it.finanze.sanita.fse2.ms.gtw.rulesmanager.repository.entity.structures.StructureMapETY;
//import it.finanze.sanita.fse2.ms.gtw.rulesmanager.repository.entity.structures.StructureValuesetETY;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Document(collection = "#{@structuresBean}")
//@Data
//@NoArgsConstructor
//public class StructuresETY {
//
//    public static final String FIELD_ID = "_id";
//
//    public static final String FIELD_VALUESET = "valueset";
//    public static final String FIELD_DEFINITION = "definition";
//
//    @Id
//    private String id;
//
//    @Field(name = "last_update_date")
//    private Date lastUpdate;
//
//    @Field(name = "map")
//    private List<StructureMapETY> map;
//
//    @Field(name = "valueset")
//    private List<StructureValuesetETY> valueset;
//
//    @Field(name = "definition")
//    private List<StructureDefinitionETY> definition;
//
//}
