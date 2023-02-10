package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.base;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.Engine;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

@Getter
public class EngineDTO {
    private final String id;
    private final Map<String, String> roots;
    private final Date insertion;

    public EngineDTO(Engine engine) {
        this.id = engine.getId();
        this.roots = engine.getRoots();
        this.insertion = engine.getInsertion();
    }

}
