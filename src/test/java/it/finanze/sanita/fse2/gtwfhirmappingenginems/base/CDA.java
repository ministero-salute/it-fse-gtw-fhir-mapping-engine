
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Paths.get;

public enum CDA {
    BASE_PATH(get("src", "test", "resources", "cda")),
    LAB(get(BASE_PATH.toString(), "LAB.xml"));

    private final Path path;

    CDA(Path path) {
        this.path = path;
    }

    public File file() {
        return path.toFile();
    }

    public byte[] bytes() throws IOException {
        return Files.readAllBytes(path);
    }

    public String read() throws IOException {
        return new String(bytes(), UTF_8);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
