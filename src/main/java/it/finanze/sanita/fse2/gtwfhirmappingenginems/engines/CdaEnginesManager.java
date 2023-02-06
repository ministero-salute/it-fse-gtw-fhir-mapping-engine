package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.ex.EngineInitializationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.ex.EngineUnavailableException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CdaEnginesManager {

    private final ConcurrentHashMap<String, CdaMappingEngine> engines;
    private volatile String latest;
    private volatile boolean ready;

    public CdaEnginesManager() {
        this.engines = new ConcurrentHashMap<>();
    }

    public void initialize() {
        // Set flag
        ready = true;
    }

    public CdaMappingEngine latest() {
        if(!ready) throw new EngineInitializationException("Engines initialization not finished yet");
        if (latest == null) throw new EngineUnavailableException("No engine is currently available");
        CdaMappingEngine obj = engines.get(latest);
        if (obj == null) throw new EngineUnavailableException("Engine seems available but instance is null");
        return obj;
    }

    public boolean ready() {
        return ready;
    }
}
