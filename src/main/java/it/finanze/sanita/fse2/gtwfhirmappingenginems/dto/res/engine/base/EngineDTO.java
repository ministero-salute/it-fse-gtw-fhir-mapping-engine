/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.base;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base.Engine;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class EngineDTO {
    private final String id;
    private final List<EngineRootDTO> roots;
    private final List<EngineFileDTO> files;
    private final Date insertion;

    public EngineDTO(Engine engine) {
        this.id = engine.getId();
        this.roots = engine.getRoots().entrySet().stream().map(EngineRootDTO::new).collect(Collectors.toList());
        this.files = engine.getFiles().entrySet().stream().map(EngineFileDTO::new).collect(Collectors.toList());
        this.insertion = engine.getInsertion();
    }

}
