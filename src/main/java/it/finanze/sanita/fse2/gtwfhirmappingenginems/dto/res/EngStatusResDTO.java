package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.AbstractDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.base.EngineDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class EngStatusResDTO extends AbstractDTO {
    private final List<EngineDTO> engines;

    public EngStatusResDTO(List<EngineDTO> engines) {
        this.engines = engines;
    }
}
