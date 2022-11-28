/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContextDTO {
    private String facilityTypeCode;
    private List<String> eventsCode;
    private String practiceSettingCode;
    private String serviceStartTime;
    private String serviceStopTime;
//    private String referenceId;
//    private String patientId;
}
