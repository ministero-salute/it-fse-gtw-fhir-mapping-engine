/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.repository;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.OperationException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY;

import java.util.List;

public interface IEngineRepo {
    List<EngineETY> find() throws OperationException;
    EngineETY findById(String id) throws OperationException;
    boolean enable(String id) throws OperationException;
}
