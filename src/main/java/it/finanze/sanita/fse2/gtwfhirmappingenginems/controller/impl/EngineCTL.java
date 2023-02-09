package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.IEngineCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.EngRefreshResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.EngStatusResDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.base.EngineDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IEngineSRV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EngineCTL implements IEngineCTL {

    @Autowired
    private IEngineSRV srv;

    @Override
    public EngStatusResDTO status() {
        List<EngineDTO> engines = new ArrayList<>();
        srv.manager().engines().forEach((id, engine) -> engines.add(new EngineDTO(id, engine.getRoots())));
        return new EngStatusResDTO(engines);
    }

    @Override
    public EngRefreshResDTO run() {
        srv.manager().refresh();
        return new EngRefreshResDTO("Avviato");
    }
}
