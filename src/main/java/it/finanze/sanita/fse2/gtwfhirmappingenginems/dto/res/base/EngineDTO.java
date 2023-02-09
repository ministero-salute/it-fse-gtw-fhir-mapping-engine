package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

@Getter
@AllArgsConstructor
public class EngineDTO {
    private String id;
    private Map<String, String> roots;
    private Date insertion;
}
