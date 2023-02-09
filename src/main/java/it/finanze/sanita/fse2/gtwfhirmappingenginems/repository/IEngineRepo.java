package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;

public interface IEngineRepo {
    EngineETY findById(String id) throws OperationException;
}
