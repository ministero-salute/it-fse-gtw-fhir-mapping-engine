package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums;

public enum FhirTypeEnum {

    Map("Map"),
    Definition("Definition"),
    Valueset("Valueset");

    private final String name;

    FhirTypeEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
