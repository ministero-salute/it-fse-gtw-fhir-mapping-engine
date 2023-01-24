package it.finanze.sanita.fse2.gtwfhirmappingenginems.service;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.DefinitionETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.MapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.base.ValuesetETY;

import java.io.IOException;
import java.util.List;

public interface IEngineSRV {
    void insertTransform(TransformETY transform) throws IOException;
    void insertMap(List<MapETY> root, String version);
    void insertDefinitions(List<DefinitionETY> definitions, String version) throws IOException;
    void insertValueset(List<ValuesetETY> valuesets, String version) throws IOException;
    boolean doesRootMapExists(TransformETY transform);
    CdaMappingEngine getEngine();
}
