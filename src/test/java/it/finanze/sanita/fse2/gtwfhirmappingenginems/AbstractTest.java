/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
public abstract class AbstractTest {

	@Autowired
	protected MongoTemplate mongo;

    @BeforeEach
    void init() {
        mongo.dropCollection(TransformETY.class);
    }
}
