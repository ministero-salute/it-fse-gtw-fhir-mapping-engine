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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.base;

public enum Engine {

    LAB_ENGINE("66437156246b8f543730a6b0", "6537f44fbe0517258e7dfb94"),
    REMOVABLE("67f5492a2f52c3901206662c", "63eb627bfe71ae4bb1ee814a"),
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
