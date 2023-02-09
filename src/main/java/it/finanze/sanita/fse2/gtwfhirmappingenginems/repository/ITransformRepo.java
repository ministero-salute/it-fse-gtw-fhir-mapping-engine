package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;

public interface ITransformRepo {
    TransformETY findById(String id) throws OperationException;
}
