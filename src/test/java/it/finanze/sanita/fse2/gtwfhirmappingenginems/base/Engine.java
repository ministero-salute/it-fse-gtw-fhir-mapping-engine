
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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.base;

public enum Engine {

    LAB_ENGINE("63ef627214f30c0cc5926a09", "63eb627bfe71ae4bb1ee814a"),
    REMOVABLE("63ef61eea5ed7e31188b7406", "63eb627bfe71ae4bb1ee814a"),
    INVALID("INVALID_ENGINE_ID", "INVALID_TRANSFORM_ID");

    private final String engineId;
    private final String transformId;

    Engine(String engineId, String transformId) {
        this.engineId = engineId;
        this.transformId = transformId;
    }

    public String engineId() {
        return engineId;
    }

    public String transformId() {
        return transformId;
    }

    public static int size() {
        return Engine.values().length - 1;
    }

}
