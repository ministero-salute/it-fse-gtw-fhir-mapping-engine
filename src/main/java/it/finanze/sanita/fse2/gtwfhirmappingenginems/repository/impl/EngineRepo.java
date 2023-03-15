package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import com.mongodb.MongoException;
import com.mongodb.client.result.UpdateResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.*;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY.FIELD_AVAILABLE;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY.FIELD_ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class EngineRepo implements IEngineRepo {

    @Autowired
    private MongoTemplate mongo;

    @Override
    public List<EngineETY> find() throws OperationException {
        List<EngineETY> out;
        try {
            out = mongo.findAll(EngineETY.class);
        }catch (MongoException e) {
            throw new OperationException(ERR_REP_FIND_ALL_ENGINES, e);
        }
        return out;
    }

    @Override
    public EngineETY findById(String id) throws OperationException {
        EngineETY out;
        Query q = new Query(where(FIELD_ID).is(id));
        try {
            out = mongo.findOne(q, EngineETY.class);
        }catch (MongoException e) {
            throw new OperationException(ERR_REP_FIND_BY_ID_ENGINE, e);
        }
        return out;
    }

    @Override
    public boolean enable(String id) throws OperationException {
        UpdateResult res;
        Query q = new Query(where(FIELD_ID).is(id));
        Update u = new Update();
        u.set(FIELD_AVAILABLE, true);
        try {
            res = mongo.updateFirst(q, u, EngineETY.class);
        }catch (MongoException e) {
            throw new OperationException(ERR_REP_SET_AVAILABLE_ENGINE, e);
        }
        // Keep matchedCount() because if an erroneous shutdown occurs
        // and enabled is still up this returns true anyway
        return res.getMatchedCount() == 1;
    }
}
