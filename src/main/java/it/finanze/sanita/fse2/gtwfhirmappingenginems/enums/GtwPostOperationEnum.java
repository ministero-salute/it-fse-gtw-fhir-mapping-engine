package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums;

public enum GtwPostOperationEnum {
    CREATE,
    REPLACE;

    public GtwOperationEnum toGeneric() {
        return GtwOperationEnum.valueOf(name());
    }
}
