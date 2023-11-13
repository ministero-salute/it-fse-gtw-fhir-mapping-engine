package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.op;

public enum GtwPostOperationEnum {
    CREATE,
    REPLACE;

    public GtwOperationEnum toGeneric() {
        return GtwOperationEnum.valueOf(name());
    }
}
