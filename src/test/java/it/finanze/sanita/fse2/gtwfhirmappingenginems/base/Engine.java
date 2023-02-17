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
