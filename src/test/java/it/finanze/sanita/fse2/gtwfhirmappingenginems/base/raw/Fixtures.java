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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.base.raw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.engine.EngineETY.FIELD_LAST_SYNC;

public enum Fixtures {
    BASE_PATH(Paths.get("src", "test", "resources", "fixtures")),
    ENGINES(Paths.get(BASE_PATH.toString(), "engines.json")),
    TRANSFORM(Paths.get(BASE_PATH.toString(), "transform.json"));

    private final Path path;

    Fixtures(Path path) {
        this.path = path;
    }

    public File file() {
        return path.toFile();
    }

    @Override
    public String toString() {
        return path.toString();
    }

    public List<Document> asDocuments() throws IOException {
        List<Document> docs = new ArrayList<>();
        JsonNode root = new ObjectMapper().readTree(this.file());
        for (JsonNode node : root) {
            docs.add(Document.parse(node.toString()));
        }
        return docs;
    }

    public List<Document> asFreshDocuments() throws IOException {
        List<Document> docs = new ArrayList<>();
        JsonNode root = new ObjectMapper().readTree(this.file());
        for (JsonNode node : root) docs.add(Document.parse(node.toString()));
        for (Document doc : docs) doc.replace(FIELD_LAST_SYNC, new Date());
        return docs;
    }
}
