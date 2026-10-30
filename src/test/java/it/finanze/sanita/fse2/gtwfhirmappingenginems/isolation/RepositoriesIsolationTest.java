package it.finanze.sanita.fse2.gtwfhirmappingenginems.isolation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY.*;

import java.util.Date;
import java.util.List;

import com.mongodb.MongoException;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl.EngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl.TransformRepo;

@Tag("ISOLATION")
@ActiveProfiles("ISOLATION")
@SpringJUnitConfig({EngineRepo.class, TransformRepo.class})
class RepositoriesIsolationTest {
    @Autowired private EngineRepo engines;
    @Autowired private TransformRepo transforms;
    @MockitoBean private MongoTemplate mongo;

    @Test
    void listsEnginesAndFindsByExactId() throws Exception {
        EngineETY entity = new EngineETY();
        when(mongo.findAll(EngineETY.class)).thenReturn(List.of(entity));
        when(mongo.findOne(any(Query.class), eq(EngineETY.class))).thenReturn(entity);
        assertEquals(List.of(entity), engines.find());
        assertSame(entity, engines.findById("engine-1"));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo).findOne(query.capture(), eq(EngineETY.class));
        assertEquals(new Document(FIELD_ID, "engine-1"), query.getValue().getQueryObject());
    }

    @Test
    void missingEngineReturnsNull() throws Exception {
        assertNull(engines.findById("missing"));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 2})
    void enableUsesMatchedCountEvenWhenNothingWasModified(long matches) throws Exception {
        when(mongo.updateFirst(any(Query.class), any(Update.class), eq(EngineETY.class)))
                .thenReturn(UpdateResult.acknowledged(matches, 0L, null));
        assertEquals(matches == 1, engines.enable("engine-1"));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> update = ArgumentCaptor.forClass(Update.class);
        verify(mongo).updateFirst(query.capture(), update.capture(), eq(EngineETY.class));
        assertEquals(new Document(FIELD_ID, "engine-1"), query.getValue().getQueryObject());
        assertEquals(new Document("$set", new Document(FIELD_AVAILABLE, true)), update.getValue().getUpdateObject());
    }

    @ParameterizedTest
    @CsvSource({"0,0,0,0", "1,1,0,0", "3,0,0,0", "3,2,2,0", "3,3,2,1"})
    void cleanupKeepsAtLeastTheNewestEngine(long total, long expired, int removed, int skip) throws Exception {
        Date cutoff = new Date(123456L);
        when(mongo.count(any(Query.class), eq(EngineETY.class))).thenReturn(total, expired);
        when(mongo.remove(any(Query.class), eq(EngineETY.class))).thenReturn(DeleteResult.acknowledged(removed));
        assertEquals(removed, engines.cleanup(cutoff));
        if (removed == 0) {
            verify(mongo, never()).remove(any(Query.class), eq(EngineETY.class));
        } else {
            ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
            verify(mongo).remove(query.capture(), eq(EngineETY.class));
            assertEquals(new Document(FIELD_LAST_SYNC, new Document("$lt", cutoff)), query.getValue().getQueryObject());
            assertEquals(new Document(FIELD_LAST_SYNC, -1), query.getValue().getSortObject());
            assertEquals(skip, query.getValue().getSkip());
        }
        verify(mongo, times(total > 1 ? 2 : 1)).count(any(Query.class), eq(EngineETY.class));
    }

    @Test
    void transformationQueryContainsAllRequestedObjectIds() throws Exception {
        List<ObjectId> ids = List.of(new ObjectId(), new ObjectId());
        List<TransformETY> entities = List.of(new TransformETY());
        when(mongo.find(any(Query.class), eq(TransformETY.class))).thenReturn(entities);
        assertSame(entities, transforms.findByIds(ids));
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo).find(query.capture(), eq(TransformETY.class));
        assertEquals(new Document(TransformETY.FIELD_ID, new Document("$in", ids)), query.getValue().getQueryObject());
    }

    @Test
    void cleanupWrapsDeletionFailureAfterSuccessfulCounts() {
        MongoException failure = new MongoException("delete denied");
        when(mongo.count(any(Query.class), eq(EngineETY.class))).thenReturn(3L, 2L);
        when(mongo.remove(any(Query.class), eq(EngineETY.class))).thenThrow(failure);
        assertSame(failure, assertThrows(OperationException.class,
                () -> engines.cleanup(new Date())).getCause());
    }

    @Test
    void emptyTransformationIdsCannotSelectAllDocuments() throws Exception {
        when(mongo.find(any(Query.class), eq(TransformETY.class))).thenReturn(List.of());
        assertTrue(transforms.findByIds(List.of()).isEmpty());
        ArgumentCaptor<Query> query = ArgumentCaptor.forClass(Query.class);
        verify(mongo).find(query.capture(), eq(TransformETY.class));
        assertEquals(new Document(TransformETY.FIELD_ID, new Document("$in", List.of())), query.getValue().getQueryObject());
    }

    @Test
    void wrapsDatabaseFailuresWithoutLosingCause() {
        MongoException failure = new MongoException("offline");
        when(mongo.findAll(EngineETY.class)).thenThrow(failure);
        when(mongo.findOne(any(Query.class), eq(EngineETY.class))).thenThrow(failure);
        when(mongo.updateFirst(any(Query.class), any(Update.class), eq(EngineETY.class))).thenThrow(failure);
        when(mongo.count(any(Query.class), eq(EngineETY.class))).thenThrow(failure);
        when(mongo.find(any(Query.class), eq(TransformETY.class))).thenThrow(failure);
        assertSame(failure, assertThrows(OperationException.class, engines::find).getCause());
        assertSame(failure, assertThrows(OperationException.class, () -> engines.findById("id")).getCause());
        assertSame(failure, assertThrows(OperationException.class, () -> engines.enable("id")).getCause());
        assertSame(failure, assertThrows(OperationException.class, () -> engines.cleanup(new Date())).getCause());
        assertSame(failure, assertThrows(OperationException.class, () -> transforms.findByIds(List.of())).getCause());
    }
}
