package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.DefinitionETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.MapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.ValuesetETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.model.ValueSet;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

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
    public void insertTransform(TransformETY transform) {
        insertMap(transform.getRootMap(), transform.getVersion());
        insertDefinitions(transform.getDefinitions(), transform.getVersion());
        insertValueset(transform.getValuesets(), transform.getVersion());
    }

    @Override
    public void insertMap(MapETY root, String version) {
        // Retrieve data
        String data = new String(root.getContent().getData());
        // Parse map
        StructureMap map = engine.parseMap(data);
        // Set version
        map.setUrl(root.getName());
        map.setVersion(version);
        // Add to engine
        engine.addCanonicalResource(map);
    }

    @Override
    public void insertDefinitions(List<DefinitionETY> definitions, String version) {

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        for (DefinitionETY d : definitions) {
            // Retrieve data
            String data = new String(d.getContent().getData());
            // Parse map
            StructureDefinition definition = parser.parseResource(StructureDefinition.class, data);
            // Set version
            definition.setUrl(d.getName());
            definition.setVersion(version);
            // Add to engine
            engine.addCanonicalResource(definition);
        }
    }

    @Override
    public void insertValueset(List<ValuesetETY> valuesets, String version) {

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();

        for (ValuesetETY v : valuesets) {
            // Retrieve data
            String data = new String(v.getContent().getData());
            // Parse map
            ValueSet valueset = parser.parseResource(ValueSet.class, data);
            // Set version
            valueset.setUrl(v.getName());
            valueset.setVersion(version);
            // Add to engine
            engine.addCanonicalResource(valueset);
        }
    }

    @Override
    public boolean doesRootMapExists(TransformETY transform) {
        return engine.getContext().hasResourceVersion(
            org.hl7.fhir.r5.model.StructureMap.class,
            transform.getRootMapName(),
            transform.getVersion()
        );
    }

    @Override
    public CdaMappingEngine getEngine() {
        return engine;
    }
}
