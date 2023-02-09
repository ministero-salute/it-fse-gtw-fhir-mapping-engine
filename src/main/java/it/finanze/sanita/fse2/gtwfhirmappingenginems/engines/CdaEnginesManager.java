package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.Engine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.EngineException;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CdaEnginesManager {

    private final ConcurrentHashMap<String, Engine> engines;
    private volatile boolean ready;

    public CdaEnginesManager() {
        this.engines = new ConcurrentHashMap<>();
        this.ready = false;
    }

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

    public boolean ready() {
        return ready;
    }
}
