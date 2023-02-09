package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineException;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.EngineCFG.ENGINE_EXECUTOR;

@Component
public class CdaEnginesManager {

    private final ConcurrentHashMap<String, Engine> engines;
    private volatile boolean ready;

    public CdaEnginesManager() {
        this.engines = new ConcurrentHashMap<>();
        this.ready = false;
    }

    @Async(ENGINE_EXECUTOR)
    public void refresh() {
        // Set flag
        ready = true;
    }

    public Bundle transform(String cda, String engineId, String objectId) throws IOException {
        if(!ready) throw new EngineException("Engines initialization not finished yet");
        Engine obj = engines.get(engineId);
        if (obj == null) throw new EngineException("Engine instance is null");
        String uri = obj.getRoots().get(objectId);
        if (uri == null) throw new EngineException("Engine is available but root map was not found");
        return obj.getInstance().transformCdaToFhir(cda, uri);
    }
}
