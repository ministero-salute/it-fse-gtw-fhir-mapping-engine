package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.CdaEnginesManager;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.ProfileUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EngineSRV implements IEngineSRV {

    @Autowired
    private ProfileUtility profile;

    @Autowired
    private CdaEnginesManager manager;

    @EventListener(ApplicationStartedEvent.class)
    public void initialize() {
        // Prevent loading engines while running tests
        if(!profile.isTestProfile()) {
            // Create instances from database
            manager.refresh();
        } else {
            log.info("Skipping engine initialisation, using test profile");
        }
    }

    @Override
    public CdaEnginesManager manager() {
        return manager;
    }
}
