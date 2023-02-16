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

    public String read() throws IOException {
        return new String(Files.readAllBytes(path), UTF_8);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
