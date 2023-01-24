package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.DefinitionETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.MapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.ValuesetETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class EngineSRV implements IEngineSRV {

    private CdaMappingEngine engine;

    @EventListener(ApplicationStartedEvent.class)
    public void initialize() {
        try {
            engine = new CdaMappingEngine.CdaMappingEngineBuilder().getEngine("/package.tgz");
        } catch(Exception ex) {
            log.error("Error while perform builder in post construct : " , ex);
            throw new BusinessException("Error while perform builder in post construct : " , ex);
        }
    }

    @Override
    public void insertTransform(TransformETY transform) throws IOException {
        insertMap(transform.getMaps(), transform.getVersion());
        insertDefinitions(transform.getDefinitions(), transform.getVersion());
        insertValueset(transform.getValuesets(), transform.getVersion());
        log.debug("{}", engine.getContext().listMapUrls());
    }

    @Override
    public void insertMap(List<MapETY> maps, String version) {
        for(MapETY m: maps) {
            // Retrieve data
            String data = new String(m.getContent().getData());
            // Parse map
            StructureMap map = engine.parseMap(data);
            // Set version
            map.setUrl(m.getName());
            map.setVersion(version);
            // Add to engine
            engine.addCanonicalResource(map);
        }
    }

    @Override
    public void insertDefinitions(List<DefinitionETY> definitions, String version) throws IOException {

        JsonParser parser = new JsonParser();

        for (DefinitionETY d : definitions) {
            // Retrieve data
            String data = new String(d.getContent().getData());
            // Parse map
            StructureDefinition definition = (StructureDefinition) parser.parse(data);
            // Set version
            definition.setUrl(d.getName());
            definition.setVersion(version);
            // Add to engine
            engine.addCanonicalResource(definition);
        }
    }

    @Override
    public void insertValueset(List<ValuesetETY> valuesets, String version) throws IOException {

        JsonParser parser = new JsonParser();

        for (ValuesetETY v : valuesets) {
            // Retrieve data
            String data = new String(v.getContent().getData());
            // Parse valueset
            ValueSet valueset = (ValueSet) parser.parse(data);
            // Set version
            valueset.setUrl(v.getName());
            valueset.setVersion(version);
            // Add to engine
            engine.addCanonicalResource(valueset);
        }
    }

    @Override
    public boolean doesRootMapExists(TransformETY transform) {
        boolean value;
        if(transform.getVersion() == null || transform.getVersion().isEmpty()) {
            log.debug("doesRootMapExists() by resource with {}", transform.getRootMapName());
            value = engine.getContext().hasResource(
                org.hl7.fhir.r5.model.StructureMap.class,
                transform.getRootMapName()
            );
        } else {
            log.debug("doesRootMapExists() by resource version with {} / {}", transform.getRootMapName(), transform.getVersion());
            value = engine.getContext().hasResourceVersion(
                org.hl7.fhir.r5.model.StructureMap.class,
                transform.getRootMapName(),
                transform.getVersion()
            );
        }
        return value;
    }

    @Override
    public CdaMappingEngine getEngine() {
        return engine;
    }
}
