
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.exception;

import com.mongodb.MongoException;

/**
 * Mainly used to catch database issues, it's used to describe a data-layer failure
 * By-design MongoDB drivers uses {@link RuntimeException} which are difficult to handle
 * without a consistent compiler-enforcing policy (e.g Checked Exceptions)
 * To simplify the handling of operations issues, this class takes in the {@link MongoException}
 * and add a reasonable descriptive message to find the routine which lead to the error.
 * This exception is supposed to be generated and re-thrown as soon as a {@link MongoException} is caught.
 */
public class OperationException extends Exception {

    /**
     * Serial version uid
     */
    private static final long serialVersionUID = 6133357493429760036L;

    /**
     * Complete constructor.
     *
     * @param msg	Message to be shown.
     *              It should describe what the operation was trying to accomplish.
     * @param e		The original MongoExceptions.
     */
    public OperationException(final String msg, final MongoException e) {
        super(msg, e);
    }
}
