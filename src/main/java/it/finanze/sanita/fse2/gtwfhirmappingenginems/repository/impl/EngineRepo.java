/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import com.mongodb.MongoException;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.*;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY.*;
import static org.springframework.data.domain.Sort.Direction.DESC;
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

    @Override
    public int cleanup(Date at) throws OperationException {

        // Cases:
        // [1] If one engine available, do nothing
        // [2] If multiple engines but all expired, leave the most recent
        // [3] If no engine expired, do nothing
        // [4] If some engines expired and some aren't, remove the expired ones

        int engines = 0;

        // Retrieve all expired engines, sort by last_sync
        Query q = new Query(
            where(FIELD_LAST_SYNC).lt(at)
        ).with(Sort.by(DESC, FIELD_LAST_SYNC));

        try {
            DeleteResult res;
            // Verify how many engines we got
            // We always want to keep at least one engine
            long size = mongo.count(new Query(), EngineETY.class);
            // If we have more than one engine
            if(size > MIN_ENGINE_AVAILABLE) {
                // Count the expired engines
                long expired = mongo.count(q, EngineETY.class);
                // Do not perform queries if no engine expired
                if(expired > 0) {
                    if(expired == size) {
                        // If all engines are expired, remove everything but keep the most recent
                        res = mongo.remove(q.skip(MIN_ENGINE_AVAILABLE), EngineETY.class);
                    } else {
                        // Otherwise remove only the expired ones
                        res = mongo.remove(q, EngineETY.class);
                    }
                    // Save the delete count
                    engines = (int) res.getDeletedCount();
                }
            }
        } catch (MongoException e) {
            throw new OperationException(ERR_REP_CLEANUP_ENGINE, e);
        }

        return engines;
    }
}
