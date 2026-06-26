package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums;

import lombok.Getter;

public enum EventCodeEnum {
	
    P99("P99", "Oscuramento del documento"),
    P00("P00", "De-Oscuramento in alimentazione");
	
    public static final String OID = "urn:oid:2.16.840.1.113883.2.9.3.3.6.1.3";

    @Getter
    private final String code;
    @Getter
    private final String description;

    EventCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
   
    public static EventCodeEnum fromCode(String code) {
        for (EventCodeEnum e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}