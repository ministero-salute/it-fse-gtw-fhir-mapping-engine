/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
