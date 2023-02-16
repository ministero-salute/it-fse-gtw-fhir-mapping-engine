package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Fixtures;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public abstract class AbstractEngineTest {

    @Autowired
    protected MongoTemplate mongo;

    @Autowired
    protected IEngineSRV engines;

    protected void initDb() throws IOException {
        // Reset data
        resetDb();
        // Read fixtures
        insert(fixtures(Fixtures.ENGINES), EngineETY.class);
        insert(fixtures(Fixtures.TRANSFORM), TransformETY.class);
    }
    protected void initEngine() {
        engines.manager().refreshSync();
    }

    protected void resetDb() {
        mongo.dropCollection(TransformETY.class);
        mongo.dropCollection(EngineETY.class);
    }

    private void insert(List<Document> documents, Class<?> clazz) {
        for (Document doc : documents) {
            mongo.insert(doc, mongo.getCollectionName(clazz));
        }
    }

    private List<Document> fixtures(Fixtures f) throws IOException {
        List<Document> docs = new ArrayList<>();
        JsonNode root = new ObjectMapper().readTree(f.file());
        for (JsonNode node : root) {
            docs.add(Document.parse(node.toString()));
        }
        return docs;
    }

}
