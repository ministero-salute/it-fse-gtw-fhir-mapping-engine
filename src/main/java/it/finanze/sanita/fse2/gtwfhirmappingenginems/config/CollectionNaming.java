package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;

/**
 *	@author vincenzoingenito
 *
 *	Collection naming.
 */
@Configuration
public class CollectionNaming {

    @Autowired
    private ProfileUtility profileUtility;

    @Bean("structureDefinitionBean")
    public String getStructureDefinitionCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.STRUCTURE_DEFINITIONS;
        }
        return Constants.ComponentScan.Collections.STRUCTURE_DEFINITIONS;
    }
    
    @Bean("structureMapBean")
    public String getStructureMapCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.STRUCTURE_MAP;
        }
        return Constants.ComponentScan.Collections.STRUCTURE_MAP;
    }
    
    @Bean("valuesetBean")
    public String getValueSetCollection() {
        if (profileUtility.isTestProfile()) {
            return Constants.Profile.TEST_PREFIX + Constants.ComponentScan.Collections.VALUESET;
        }
        return Constants.ComponentScan.Collections.VALUESET;
    }
    
    
 
}
