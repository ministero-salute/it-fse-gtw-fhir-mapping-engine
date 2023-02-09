package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.ITransformRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EngineBuilder {

    @Autowired
    private ITransformRepo transform;

    @Autowired
    private IEngineRepo engine;

    public Engine fromId(String id) {
        return null;
    }

}
