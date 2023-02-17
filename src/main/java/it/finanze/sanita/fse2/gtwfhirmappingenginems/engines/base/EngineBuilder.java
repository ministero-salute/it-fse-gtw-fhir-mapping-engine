package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.data.RootData;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.FhirTypeEnum;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.engine.EngineBuilderException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IEngineRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.ITransformRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.sub.EngineMap;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static ch.ahdis.matchbox.engine.CdaMappingEngine.CdaMappingEngineBuilder;
import static it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants.Logs.*;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
public class EngineBuilder {

    private static final String TITLE = "ENGINE";
    private static final String JSON_EXT = ".json";

    @Autowired
    private ITransformRepo transform;

    @Autowired
    private IEngineRepo engine;

    public Engine fromId(String id) throws OperationException, EngineBuilderException {
        log.debug("[{}][{}] Spawning new engine", TITLE, id);
        // Retrieve entity
        EngineETY engine = this.engine.findById(id);
        // Check nullity
        if (engine == null) throw new EngineBuilderException(ERR_BLD_FIND_BY_ID_ENGINE);
        // Create mapping for all available entities
        log.debug("[{}][{}] Creating entities map", TITLE, id);
        Map<String, TransformETY> entities = createEntitiesMap(engine);
        // Create files mapping
        log.debug("[{}][{}] Creating files mapping", TITLE, id);
        Map<String, String> files = createFilesMap(entities);
        // Create root mapping
        log.debug("[{}][{}] Creating root mapping", TITLE, id);
        Map<String, RootData> roots = createRootsMap(engine.getRoots(), entities);
        // Create engine
        log.debug("[{}][{}] Initializing engine", TITLE, id);
        CdaMappingEngine instance = createEngine();
        // Register resources
        log.debug("[{}][{}] Registering resources", TITLE, id);
        registerItems(id, instance, entities);
        // Return newly fresh engine
        log.debug("[{}][{}] Engine spawned", TITLE, id);
        return new Engine(id, roots, files, instance);
    }

    private Map<String, TransformETY> createEntitiesMap(EngineETY e) throws OperationException, EngineBuilderException {
        // Working var
        HashMap<String, TransformETY> out = new HashMap<>();
        // Check emptiness
        if(e.getFiles().isEmpty()) throw new EngineBuilderException(ERR_BLD_EMPTY_FILE_LIST_FHIR);
        // Retrieve items
        List<TransformETY> files = transform.findByIds(e.getFiles());
        // Check for mismatch
        if(files.size() != e.getFiles().size()) {
            throw new EngineBuilderException(
                format(ERR_BLD_SIZE_FILE_LIST_FHIR, e.getFiles().size(), files.size())
            );
        }
        // Mapping resources
        for (TransformETY f : files) {
            // Consistency check (duplicates)
            if(out.containsKey(f.getId())) {
                throw new EngineBuilderException(
                    format(ERR_BLD_DUPLICATED_ENTRY_FHIR, f.getId())
                );
            }
            out.put(f.getId(), f);
        }
        return out;
    }

    private Map<String, String> createFilesMap(Map<String, TransformETY> entities) throws EngineBuilderException {
        // Working var
        Map<String, String> out = new HashMap<>();
        // Check emptiness
        if(entities.isEmpty()) throw new EngineBuilderException(ERR_BLD_EMPTY_MAP_LIST_FHIR);
        // Retrieve items
        Collection<TransformETY> files = entities.values();
        // Mapping resources
        for (TransformETY f : files) {
            // Consistency check (duplicates)
            if(out.containsKey(f.getId())) {
                throw new EngineBuilderException(
                    format(ERR_BLD_DUPLICATED_ENTRY_FHIR, f.getId())
                );
            }
            out.put(f.getId(), f.getFormattedUri());
        }
        return out;
    }


    private Map<String, RootData> createRootsMap(List<EngineMap> e, Map<String, TransformETY> entities) throws EngineBuilderException {
        Map<String, RootData> roots = new HashMap<>();
        for (EngineMap map : e) {
            // Get object identifier
            String id = map.getOid().toHexString();
            // Save in map
            roots.putIfAbsent(id, new RootData(map.getFormattedUri(), map.getRoot()));
            // Consistency check
            if(!entities.containsKey(id)) {
                throw new EngineBuilderException(
                    format(ERR_BLD_ROOT_UNAVAILALE, map.getUri(), id)
                );
            }
        }
        return roots;
    }

    private CdaMappingEngine createEngine() throws EngineBuilderException {
        CdaMappingEngine engine;
        try {
            // Preload default packages
            engine = new CdaMappingEngineBuilder().getEngine();
        } catch (IOException | URISyntaxException e) {
            throw new EngineBuilderException(ERR_BLD_ENGINE_UNAVAILALE, e);
        }
        return engine;
    }

    private void registerItems(String id, CdaMappingEngine engine, Map<String, TransformETY> entities) throws EngineBuilderException {
        for (Entry<String, TransformETY> i : entities.entrySet()) {
            // Retrieve instance
            TransformETY value = i.getValue();
            // Retrieve type
            FhirTypeEnum type = value.getType();
            // Retrieve uri and version
            String uri = value.getUri();
            String version = value.getVersion();
            // Log me
            log.debug("[{}][{}] Registering '{}' as '{}' (v{})", TITLE, id, type, uri, version);
            // Register entity
            switch (type) {
                case Map:
                    registerMap(engine, value);
                    break;
                case Definition:
                    registerDefinition(engine, value);
                    break;
                case Valueset:
                    registerValueset(engine, value);
                    break;
                default:
                    throw new EngineBuilderException(
                        format(ERR_BLD_UKNOWN_TYPE, uri, type)
                    );
            }
        }
    }

    private void registerMap(CdaMappingEngine engine, TransformETY e) throws EngineBuilderException {
        try {
            JsonParser parser = new JsonParser();
            // Retrieve data
            String data = new String(e.getContent().getData(), UTF_8);
            // Parse map
            StructureMap map;
            if(e.getFilename().endsWith(JSON_EXT)) {
                map = (StructureMap) parser.parse(data);
            } else {
                map = engine.parseMap(data);
            }
            // Set URI and version
            map.setUrl(e.getUri());
            map.setVersion(e.getVersion());
            // Add to engine
            engine.addCanonicalResource(map);
        } catch (Exception ex) {
            throw new EngineBuilderException(
                format(ERR_BLD_REGISTER_FHIR, e.getUri(), FhirTypeEnum.Map.getName()),
                ex
            );
        }
    }

    private void registerDefinition(CdaMappingEngine engine, TransformETY e) throws EngineBuilderException {
        try {
            JsonParser parser = new JsonParser();
            // Retrieve data
            String data = new String(e.getContent().getData(), UTF_8);
            // Parse map
            StructureDefinition definition = (StructureDefinition) parser.parse(data);
            // Set URI and version
            definition.setUrl(e.getUri());
            definition.setVersion(e.getVersion());
            // Add to engine
            engine.addCanonicalResource(definition);
        } catch (Exception ex) {
            throw new EngineBuilderException(
                format(ERR_BLD_REGISTER_FHIR, e.getUri(), FhirTypeEnum.Definition.getName()),
                ex
            );
        }
    }

    private void registerValueset(CdaMappingEngine engine, TransformETY e) throws EngineBuilderException {
        try {
            JsonParser parser = new JsonParser();
            // Retrieve data
            String data = new String(e.getContent().getData(), UTF_8);
            // Parse map
            ValueSet valueset = (ValueSet) parser.parse(data);
            // Set URI and version
            valueset.setUrl(e.getUri());
            valueset.setVersion(e.getVersion());
            // Add to engine
            engine.addCanonicalResource(valueset);
        } catch (Exception ex) {
            throw new EngineBuilderException(
                format(ERR_BLD_REGISTER_FHIR, e.getUri(), FhirTypeEnum.Valueset.getName()),
                ex
            );
        }
    }

}
