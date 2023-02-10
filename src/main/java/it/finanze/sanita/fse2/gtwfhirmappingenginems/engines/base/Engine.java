package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import lombok.Getter;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class Engine {

    private final String id;
    private final ConcurrentHashMap<String, String> roots;
    private final ConcurrentHashMap<String, String> files;

    private final CdaMappingEngine instance;
    private final Date insertion;

    public Engine(
        String id,
        Map<String, String> roots,
        Map<String, String> files,
        CdaMappingEngine instance
    ) {
        this.id = id;
        this.roots = new ConcurrentHashMap<>(roots);
        this.files = new ConcurrentHashMap<>(files);
        this.instance = instance;
        this.insertion = new Date();
    }
}
