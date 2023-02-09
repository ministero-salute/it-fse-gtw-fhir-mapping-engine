package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.ERR_REP_FIND_BY_ID_ENGINE;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class EngineRepo implements IEngineRepo {

    @Autowired
    private MongoTemplate mongo;

    @Override
    public EngineETY findById(String id) throws OperationException {
        EngineETY out;
        Query q = new Query(where(EngineETY.FIELD_ID).is(id));
        try {
            out = mongo.findOne(q, EngineETY.class);
        }catch (MongoException e) {
            throw new OperationException(ERR_REP_FIND_BY_ID_ENGINE, e);
        }
        return out;
    }
}
