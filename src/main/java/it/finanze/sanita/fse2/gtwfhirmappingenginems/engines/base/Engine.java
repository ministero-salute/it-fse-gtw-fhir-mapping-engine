package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import lombok.Getter;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Getter
public final class Engine {

    private final String id;
    private final Map<String, String> roots;
    private final CdaMappingEngine instance;
    private final Date insertion;

    public Engine(String id, Map<String, String> roots, CdaMappingEngine instance) {
        this.id = id;
        // Guarantees read-only operations
        this.roots = Collections.unmodifiableMap(roots);
        this.instance = instance;
        this.insertion = new Date();
    }
}
