package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.base.AbstractDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.base.EngineDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class EngStatusResDTO extends AbstractDTO {
    private final List<EngineDTO> engines;

    public EngStatusResDTO(List<EngineDTO> engines) {
        this.engines = engines;
    }
}
