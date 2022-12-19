/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums;

import lombok.Getter;

public enum WeightFhirResEnum {
 
	CDA_AUTHOR("ClinicalDocument/author" , 2.0f),
	CDA_CUSTODIAN("ClinicalDocument/custodian", 1.9f),
	CDA_LEGAL_AUTH("ClinicalDocument/legalAuthenticator", 1.8f),
	CDA_DATAENTERER("ClinicalDocument/dataEnterer", 1.7f),
	CDA_AUTHENTICATOR("ClinicalDocument/authenticator", 1.6f),
	CDA_INFORMANT("ClinicalDocument/informant", 1.5f),
	CDA_INF_REC("ClinicalDocument/informationRecipient", 1.4f),
	CDA_PATICIPANT("ClinicalDocument/paticipant", 1.3f),
	CDA_PARTICIPANT("ClinicalDocument/participant", 1.3f),
	CDA_DOCOF_SE_PERFORMER("ClinicalDocument/documentationOf/serviceEvent/performer", 0.9f),
	CDA_TGT_ROLE_GUARDIANORG("ClinicalDocument/recordTarget/PatientRole/patient/guardian/guardianOrganization", 0.8f),
	CDA_TGT_ROLE_PROVIDERORG("ClinicalDocument/recordTarget/PatientRole/patient/providerOrganization", 0.7f),
	CDA_RESPONSIBLEPARTY("ClinicalDocument/componentOf/EncompassingEncounter/responsibleParty", 0.6f),
	CDA_HEALTHCARE_FACILITY("ClinicalDocument/componentOf/EncompassingEncounter/location/healthCareFacility", 0.5f),
	CDA_ORGPARTOF("ClinicalDocument/documentationOf/performer/representedOrganization/asOrganizationPartOf", 0.4f),
	CDA_MAN_ORGANIZATION("ClinicalDocument/body/vaccinazione/manufacturerOrganization", 0.3f),
	CDA_VACC_PARTICIPANT("ClinicalDocument/body/vaccinazione/participant", 1.3f),
	CDA_INQUADRAMENTO_CLINICO_AUTHOR("clinicalDocument/body/InquadramentoClinicoIniziale/author", 1.9f),
	CDA_ANAMENSI_AUTHOR("clinicalDocument/body/Anamnesi/author", 1.9f),
	CDA_ESAME_OBIETTIVO_AUTHOR("clinicalDocument/body/EsameObiettivo/author", 1.9f),
	CDA_TER_FARMACOLOGICA_AUTHOR("ClinicalDocument/body/TerapiaFarmacologicaIngresso/author", 1.9f),
	CDA_PROBLEMI_APERTI_AUTHOR("clinicalDocument/body/ProblemiAperti/author", 1.9f),
	CDA_OBSERVATION_PERFOMER("ClinicalDocument/body/entry/observation/performer", 0.9f);
	
	
	@Getter
	private String name;
		
	@Getter
	private float weight;

	private WeightFhirResEnum(String inName, float inWeight) {
		name = inName;
		weight = inWeight;
	}
	
	public static WeightFhirResEnum fromValue(final String value) {
		WeightFhirResEnum output = null;
        for (final WeightFhirResEnum valueEnum : WeightFhirResEnum.values()) {
            if (valueEnum.getName().equalsIgnoreCase(value)) {
                output = valueEnum;
                break;
            }

        }

        return output;
    }

}