package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.EngineBuilder;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineBuilderException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineInitException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.*;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.EngineCFG.ENGINE_EXECUTOR;

@Slf4j
@Component
public class CdaEnginesManager {

    private final IEngineRepo repository;
    private final EngineBuilder builder;
    private final ConcurrentHashMap<String, Engine> engines;
    private volatile boolean ready;
    private volatile boolean running;

    public CdaEnginesManager(
        @Autowired IEngineRepo repository,
        @Autowired EngineBuilder builder
    ) {
        this.repository = repository;
        this.builder = builder;
        this.engines = new ConcurrentHashMap<>();
        this.ready = false;
    }

    @Scheduled(cron = "${engine.scheduler.invoke}")
    @SchedulerLock(name = "invokeGTWEngineScheduler")
    @Async(ENGINE_EXECUTOR)
    public void refresh() {
        // Set running flag
        running = true;
        log.info("Beginning engine refreshing process");
        // Start process
        register(lists());
        // Set flag (start-up only)
        if(!ready) ready = true;
        log.info("Finishing engine refreshing process");
        // Reset running flag
        running = false;
    }

    public Bundle transform(String cda, String engineId, String objectId) throws IOException {
        if(!ready) throw new EngineInitException(ERR_ENG_UNAVAILABLE);
        Engine obj = engines.get(engineId);
        if (obj == null) throw new EngineException(ERR_ENG_NULL);
        String uri = obj.getRoots().get(objectId);
        if (uri == null) throw new EngineException(ERR_ENG_ROOT_MAP);
        return obj.getInstance().transformCdaToFhir(cda, uri);
    }

    public ConcurrentHashMap<String, Engine> engines() {
        return engines;
    }

    private List<EngineETY> lists() {
        List<EngineETY> list = new ArrayList<>();
        try {
            // Retrieve available engines
            list = repository.find();
        } catch (OperationException e) {
            log.error("Unable to retrieve all available engines", e);
        }
        return list;
    }
    private void register(List<EngineETY> list) {
        for (EngineETY e : list) {
            String id = e.getId();
            // Check if instance exists
            if(!engines.containsKey(id)) {
                // Spawn engine
                Optional<Engine> engine = create(id);
                // Consistency check
                if(engine.isPresent()) {
                    // Update
                    engines.put(id, engine.get());
                    // Mark as available, if unsuccessful unload from memory
                    if (!available(id)) {
                        log.debug("Removing engine {} because couldn't set as available", id);
                        engines.remove(id);
                    }
                }
            } else {
                log.debug("Skipping engine {} because it already exists in-memory", id);
            }
        }
    }
    private Optional<Engine> create(String id) {
        Engine e = null;
        try {
            e = builder.fromId(id);
        } catch (OperationException | EngineBuilderException ex) {
            log.error("Unable to instance engine", ex);
            log.error("Skipping {} ...", id);
        }
        return Optional.ofNullable(e);
    }
    private boolean available(String id) {
        boolean b = false;
        try {
            b = repository.enable(id);
        } catch (OperationException e) {
            log.error("Unable to mark engine '{}' as enabled", id);
        }
        return b;
    }
    public boolean isRunning() {
        return running;
    }
}
