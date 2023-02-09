/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *	Collection naming.
 */
@Configuration
public class CollectionNaming {

    @Autowired
    private ProfileUtility profileUtility;

    @Bean("transformBean")
    public String getStructureMapCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.TRANSFORM;
        }
        return Constants.Collections.TRANSFORM;
    }

    @Bean("engineBean")
    public String getEngineCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.Collections.ENGINE;
        }
        return Constants.Collections.ENGINE;
    }
 
}
	