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

    LAB_ENGINE("68777b7f3972d22d71c1135e", "6877657258e2f14cfcb3f9f8"),
    REMOVABLE("6877a5c64afe567ca0ac23aa", "6877657258e2f14cfcb3f9f8"),
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
