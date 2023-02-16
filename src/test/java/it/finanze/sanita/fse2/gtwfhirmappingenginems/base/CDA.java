package it.finanze.sanita.fse2.gtwfhirmappingenginems.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Paths.get;

public enum CDA {
    BASE_PATH(get("src", "test", "resources", "cda"), null, null),
    LAB(get(BASE_PATH.toString(), "lab.xml"), "63ee30fa3a73a20a61e848bb", "63eb627bfe71ae4bb1ee814a");

    private final Path path;
    private final String engineId;
    private final String transformId;

    CDA(Path path, String engineId, String transformId) {
        this.path = path;
        this.engineId = engineId;
        this.transformId = transformId;
    }

    public File file() {
        return path.toFile();
    }

    public String read() throws IOException {
        return new String(Files.readAllBytes(path), UTF_8);
    }

    public String engineId() {
        return engineId;
    }

    public String transformId() {
        return transformId;
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
