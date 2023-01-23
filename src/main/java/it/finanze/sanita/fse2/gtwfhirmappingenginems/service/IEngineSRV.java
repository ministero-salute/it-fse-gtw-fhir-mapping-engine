package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.DefinitionETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.MapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.ValuesetETY;

import java.util.List;

public interface IEngineSRV {
    void insertTransform(TransformETY transform);
    void insertMap(MapETY root, String version);
    void insertDefinitions(List<DefinitionETY> definitions, String version);
    void insertValueset(List<ValuesetETY> valuesets, String version);
    boolean doesRootMapExists(TransformETY transform);
    CdaMappingEngine getEngine();
}
