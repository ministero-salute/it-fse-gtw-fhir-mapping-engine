package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.TransformETY;
import org.bson.types.ObjectId;

import java.util.List;

public interface ITransformRepo {
    List<TransformETY> findByIds(List<ObjectId> ids) throws OperationException;
}
