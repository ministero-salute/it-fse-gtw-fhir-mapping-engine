/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.res.engine;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.base.LogTraceInfoDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.base.ResponseDTO;
import lombok.Getter;

@Getter
public class EngRefreshResDTO extends ResponseDTO {
    private final String message;

    public EngRefreshResDTO(LogTraceInfoDTO info, String message) {
        super(info);
        this.message = message;
    }

}
