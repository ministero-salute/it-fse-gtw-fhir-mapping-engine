/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine.base;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.data.RootData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map.Entry;

@Getter
@AllArgsConstructor
public class EngineRootDTO {
    private final String id;
    private final String uri;
    private final List<String> templates;

    public EngineRootDTO(Entry<String, RootData> pair) {
        this.id = pair.getKey();
        this.uri = pair.getValue().getUri();
        this.templates = pair.getValue().getTemplates();
    }

}
