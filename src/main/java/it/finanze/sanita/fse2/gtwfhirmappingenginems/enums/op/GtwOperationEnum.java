package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op;

public enum GtwOperationEnum {
    CREATE("create-document"),
    REPLACE("replace-document"),
    UPDATE("update-meta-document"),
    DELETE("delete-document");

    private final String id;

    GtwOperationEnum(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
