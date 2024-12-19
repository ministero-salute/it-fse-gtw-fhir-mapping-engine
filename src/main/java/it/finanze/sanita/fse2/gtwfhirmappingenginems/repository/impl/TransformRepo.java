
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.impl;

import com.mongodb.MongoException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.ITransformRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.ERR_REP_FIND_BY_ID_FHIR;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
public class TransformRepo implements ITransformRepo {

    @Autowired
    private MongoTemplate mongo;

    @Override
    public List<TransformETY> findByIds(List<ObjectId> ids) throws OperationException {
        List<TransformETY> out;
        Query q = new Query(where(TransformETY.FIELD_ID).in(ids));
        try {
            out = mongo.find(q, TransformETY.class);
        }catch (MongoException e) {
            throw new OperationException(ERR_REP_FIND_BY_ID_FHIR, e);
        }
        return out;
    }
}
