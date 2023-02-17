package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Fixtures;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.base.Engine.REMOVABLE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY.FIELD_ID;
import static org.awaitility.Awaitility.await;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
public abstract class AbstractEngineTest {

    protected static final OperationException TEST_OP_EX = new OperationException(
        "Test error",
        new MongoException("Test error")
    );

    @Autowired
    protected MongoTemplate mongo;

    @SpyBean
    protected IEngineRepo repository;

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

    protected void awaitUntilEnginesSpawned() {
        await().atMost(5, TimeUnit.MINUTES).until(() -> !engines.manager().isRunning());
    }

    protected void dropUselessEngine() {
        EngineETY e = mongo.findAndRemove(new Query(where(FIELD_ID).is(REMOVABLE.engineId())), EngineETY.class);
        if(e == null) throw new IllegalStateException("Useless engine already removed");
    }
    protected void resetDb() {
        mongo.dropCollection(TransformETY.class);
        mongo.dropCollection(EngineETY.class);
    }

    protected void emulateStartUpMsEnginesAsync() {
        // Emulate the microservice restart
        log.debug("Emulating engine restart conditions");
        engines.manager().reset();
        engines.manager().refresh();
    }

    protected void emulateStartUpMsEnginesSync() {
        // Emulate the microservice restart
        log.debug("Emulating engine restart conditions (sync)");
        engines.manager().reset();
        engines.manager().refreshSync();
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
