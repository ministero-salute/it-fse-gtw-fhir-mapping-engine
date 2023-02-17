package it.finanze.sanita.fse2.gtwfhirmappingenginems.base.raw;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

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
}
