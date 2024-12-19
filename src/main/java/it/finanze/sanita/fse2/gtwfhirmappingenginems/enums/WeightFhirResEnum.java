
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.enums;

import lombok.Getter;

public enum WeightFhirResEnum {
 
	CDA_AUTH("ClinicalDocument/authenticator",19.00f),
	CDA_AUTH_REP_ORG("ClinicalDocument/authenticator/representedOrganization",4.00f),
	CDA_AUTH_REP_ORG_PARTOF("ClinicalDocument/authenticator/representedOrganization/asOrganizationPartOf",1.00f),
	CDA_AUTH_REP_ORG_PARTOF_WHOLEORG("ClinicalDocument/authenticator/representedOrganization/asOrganizationPartOf/wholeOrganization",1.00f),
	CDA_AUTHOR("ClinicalDocument/author",20.00f),
	CDA_AUTHOR_REP_ORG("ClinicalDocument/author/representedOrganization",13.00f),
	CDA_AUTHOR_REP_ORG_PARTOF("ClinicalDocument/author/representedOrganization/asOrganizationPartOf",3.00f),
	CDA_AUTHOR_REP_ORG_PARTOF_WHOLEORG("ClinicalDocument/author/representedOrganization/asOrganizationPartOf/wholeOrganization",3.00f),
	CDA_INFORMANT("ClinicalDocument/informant",11.00f),
	CDA_INFORMANT_REP_ORG("ClinicalDocument/informant/representedOrganization",5.00f),
	CDA_INFORMANT_REP_ORG_PARTOF("ClinicalDocument/informant/representedOrganization/asOrganizationPartOf",2.00f),
	CDA_INFORMANT_REP_ORG_PARTOF_WHOLEORG("ClinicalDocument/informant/representedOrganization/asOrganizationPartOf/wholeOrganization",2.00f),
	CDA_COMP_OF_ENC_ENCOUNTER_LOCATION_HEALTH("ClinicalDocument/componentOf/EncompassingEncounter/location/healthCareFacility",8.00f),
	CDA_COMP_OF_ENC_ENCOUNTER_LOCATION_HEALTH_SERVPROVORG("ClinicalDocument/componentOf/EncompassingEncounter/location/healthCareFacility/serviceProviderOrganization",6.00f),
	CDA_COMP_OF_ENC_ENCOUNTER_LOCATION_HEALTH_SERVPROVORG_PARTOF("ClinicalDocument/componentOf/EncompassingEncounter/location/healthCareFacility/serviceProviderOrganization/asOrganizationPartOf",2.00f),
	CDA_COMP_OF_ENC_ENCOUNTER_RESP_PARTY("ClinicalDocument/componentOf/EncompassingEncounter/responsibleParty",7.00f),
	CDA_COMP_OF_ENC_ENCOUNTER_RESP_PARTY_REP_ORG("ClinicalDocument/componentOf/EncompassingEncounter/responsibleParty/representedOrganization",5.00f),
	CDA_COMP_OF_ENC_ENCOUNTER_RESP_PARTY_REP_ORG_PARTOF("ClinicalDocument/componentOf/EncompassingEncounter/responsibleParty/representedOrganization/asOrganizationPartOf",2.00f),
	CDA_COMP_OF_ENC_ENCOUNTER_RESP_PARTY_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/componentOf/EncompassingEncounter/responsibleParty/representedOrganization/asOrganizationPartOf/wholeOrganization",2.00f),
	CDA_CUSTODIAN("ClinicalDocument/custodian",14.00f),
	CDA_DATA_ENTERER("ClinicalDocument/dataEnterer",8.00f),
	CDA_DATA_ENTERER_REP_ORG("ClinicalDocument/dataEnterer/representedOrganization",5.00f),
	CDA_DATA_ENTERER_REP_ORG_PARTOF("ClinicalDocument/dataEnterer/representedOrganization/asOrganizationPartOf",2.00f),
	CDA_DATA_ENTERER_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/dataEnterer/representedOrganization/asOrganizationPartOf/wholeOrganization",2.00f),
	CDA_DOC_OF_PERF_REP_ORG_PARTOF("ClinicalDocument/documentationOf/performer/representedOrganization/asOrganizationPartOf",5.00f),
	CDA_DOC_OF_SERV_EVENT_PERF_REP_ORG("ClinicalDocument/documentationOf/serviceEvent/performer/representedOrganization",5.00f),
	CDA_DOC_OF_SERV_EVENT_PERF_REP_ORG_PARTOF("ClinicalDocument/documentationOf/serviceEvent/performer/representedOrganization/asOrganizationPartOf",2.00f),
	CDA_DOC_OF_SERV_EVENT_PERF_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/documentationOf/serviceEvent/performer/representedOrganization/asOrganizationPartOf/wholeOrganization",2.00f),
	CDA_INF_REC("ClinicalDocument/informationRecipient",7.00f),
	CDA_INF_REC_RECEIVED_ORG("ClinicalDocument/informationRecipient/receivedOrganization",2.00f),
	CDA_INF_REC_RECEIVED_ORG_PARTOF("ClinicalDocument/informationRecipient/receivedOrganization/asOrganizationPartOf",1.00f),
	CDA_INF_REC_RECEIVED_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/informationRecipient/receivedOrganization/asOrganizationPartOf/wholeOrganization",1.00f),
	CDA_LEGAL_AUTH("ClinicalDocument/legalAuthenticator",21.00f),
	CDA_LEGAL_AUTH_REP_ORG("ClinicalDocument/legalAuthenticator/representedOrganization",13.00f),
	CDA_LEGAL_AUTH_REP_ORG_PARTOF("ClinicalDocument/legalAuthenticator/representedOrganization/asOrganizationPartOf",3.00f),
	CDA_LEGAL_AUTH_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/legalAuthenticator/representedOrganization/asOrganizationPartOf/wholeOrganization",3.00f),
	CDA_PARTICIPANT("ClinicalDocument/participant",31.00f),
	CDA_PARTICIPANT_REP_ORG("ClinicalDocument/participant/representedOrganization",0.50f),
	CDA_PARTICIPANT_REP_ORG_PARTOF("ClinicalDocument/participant/representedOrganization/asOrganizationPartOf",0.50f),
	CDA_PARTICIPANT_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/participant/representedOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_PARTICIPANT_REP_ORG_PARTOF_WHOLE_ORG_SCOPING_ORG("ClinicalDocument/participant/scopingOrganization",6.00f),
	CDA_PARTICIPANT_REP_ORG_PARTOF_WHOLE_ORG_SCOPING_ORG_PARTOF("ClinicalDocument/participant/scopingOrganization/asOrganizationPartOf",0.50f),
	CDA_PARTICIPANT_REP_ORG_PARTOF_WHOLE_ORG_SCOPING_ORG_PARTOF_WHOLEORG("ClinicalDocument/participant/scopingOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_REC_TARGET_PAT_ROLE_PAT_GUARD_GUARD_ORG("ClinicalDocument/recordTarget/PatientRole/patient/guardian/guardianOrganization",0.50f),
	CDA_REC_TARGET_PAT_ROLE_PAT_GUARD_GUARD_ORG_PARTOF("ClinicalDocument/recordTarget/PatientRole/patient/guardian/guardianOrganization/asOrganizationPartOf",0.50f),
	CDA_REC_TARGET_PAT_ROLE_PAT_GUARD_GUARD_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/recordTarget/PatientRole/patient/guardian/guardianOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_REC_TARGET_PAT_ROLE_PAT_PROV_ORG("ClinicalDocument/recordTarget/PatientRole/patient/providerOrganization",0.50f),
	CDA_REC_TARGET_PAT_ROLE_PAT_PROV_ORG_PARTOF("ClinicalDocument/recordTarget/PatientRole/patient/providerOrganization/asOrganizationPartOf",0.50f),
	CDA_REC_TARGET_PAT_ROLE_PAT_PROV_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/recordTarget/PatientRole/patient/providerOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_BODY_ACT_AUTHOR("ClinicalDocument/body/act/author",6.00f),
	CDA_BODY_ACT_AUTHOR_REP_ORG("ClinicalDocument/body/act/author/representedOrganization",0.50f),
	CDA_BODY_ACT_AUTHOR_REP_ORG_PARTOF("ClinicalDocument/body/act/author/representedOrganization/asOrganizationPartOf",0.50f),
	CDA_BODY_ACT_AUTHOR_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/body/act/author/representedOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_BODY_ACT_PARTICIPANT("ClinicalDocument/body/act/participant",6.00f),
	CDA_BODY_ACT_PERFOMER("ClinicalDocument/body/act/performer",6.00f),
	CDA_BODY_ACT_REP_ORG("ClinicalDocument/body/act/representedOrganization",0.50f),
	CDA_BODY_ACT_REP_ORG_PARTOF("ClinicalDocument/body/act/representedOrganization/asOrganizationPartOf",0.50f),
	CDA_BODY_ACT_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/body/act/representedOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_BODY_ANAMNESI_AUTHOR("clinicalDocument/body/Anamnesi/author",0.50f),
	CDA_BODY_CONSULENZA_OBS_PERFOMER("ClinicalDocument/body/Consulenza/Observation/performer",6.00f),
	CDA_BODY_CONSULENZA_OBS_SERV_REQ_PARTICIPANT("ClinicalDocument/body/Consulenza/Observation/ServiceRequest/participant",0.50f),
	CDA_BODY_ENCOUNTER_PART("ClinicalDocument/body/encounterParticipant",0.50f),
	CDA_BODY_ENCOUNTER_PART_SCOP_ORG("ClinicalDocument/body/encounterParticipant/scopingOrganization",0.50f),
	CDA_BODY_ENCOUNTER_PART_SCOP_ORG_PARTOF("ClinicalDocument/body/encounterParticipant/scopingOrganization/asOrganizationPartOf",0.50f),
	CDA_BODY_ENCOUNTER_PART_SCOP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/body/encounterParticipant/scopingOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_BODY_ENTRY_OBS_PERFOMER("ClinicalDocument/body/entry/observation/performer",8.00f),
	CDA_BODY_ESAME_OBIETTIVO_AUTHOR("clinicalDocument/body/EsameObiettivo/author",0.50f),
	CDA_BODY_ESAMI_DUR_RIC_OBS_PERFOMER("ClinicalDocument/body/EsamiDuranteRicovero/Observation/performer",6.00f),
	CDA_BODY_ESAMI_DUR_RIC_OBS_PERFOMER_SERV_REQ_PARTICIPANT("ClinicalDocument/body/EsamiDuranteRicovero/Observation/ServiceRequest/participant",0.50f),
	CDA_BODY_INQUADRAMENTO_CLINICO_AUTHOR("clinicalDocument/body/InquadramentoClinicoIniziale/author",0.50f),
	CDA_BODY_MAN_ORG("ClinicalDocument/body/manufacturerOrganization",0.50f),
	CDA_BODY_OBS_AUTHOR("ClinicalDocument/body/observation/author",0.50f),
	CDA_BODY_OBS_AUTHOR_REP_ORG("ClinicalDocument/body/observation/author/representedOrganization",0.50f),
	CDA_BODY_OBS_AUTHOR_REP_ORG_PARTOF("ClinicalDocument/body/observation/author/representedOrganization/asOrganizationPartOf",0.50f),
	CDA_BODY_OBS_AUTHOR_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/body/observation/author/representedOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_BODY_OBS_PARTICIPANT("ClinicalDocument/body/Observation/participant",0.50f),
	CDA_BODY_OBS_PERFOMER("ClinicalDocument/body/Observation/performer",6.00f),
	CDA_BODY_OBS_PERFOMER_REP_ORG("ClinicalDocument/body/Observation/performer/representedOrganization",0.50f),
	CDA_BODY_OBS_PERFOMER_REP_ORG_PARTOF("ClinicalDocument/body/Observation/performer/representedOrganization/asOrganizationPartOf",0.50f),
	CDA_BODY_OBS_PERFOMER_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/body/Observation/performer/representedOrganization/wholeOrganization",0.50f),
	CDA_BODY_ORG_AUTHOR("ClinicalDocument/body/organizer/author",0.50f),
	CDA_BODY_ORG_AUTHOR_REP_ORG("ClinicalDocument/body/organizer/author/representedOrganization",0.50f),
	CDA_BODY_ORG_AUTHOR_REP_ORG_PARTOF("ClinicalDocument/body/organizer/author/representedOrganization/asOrganizationPartOf",0.50f),
	CDA_BODY_ORG_AUTHOR_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/body/organizer/author/representedOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_BODY_ORG_PARTICIPANT("ClinicalDocument/body/organizer/participant",0.50f),
	CDA_BODY_ORG_PERFOMER("ClinicalDocument/body/organizer/performer",6.00f), 
	CDA_BODY_ORG_PERFOMER_REP_ORG("ClinicalDocument/body/organizer/performer/representedOrganization",0.50f),
	CDA_BODY_ORG_PERFOMER_REP_ORG_PARTOF("ClinicalDocument/body/organizer/performer/representedOrganization/asOrganizationPartOf",0.50f),
	CDA_BODY_ORG_PERFOMER_REP_ORG_PARTOF_WHOLE_ORG("ClinicalDocument/body/organizer/performer/representedOrganization/asOrganizationPartOf/wholeOrganization",0.50f),
	CDA_BODY_PARTICIPANT("ClinicalDocument/body/participant",6.00f),
	CDA_BODY_PROBLEMIAPERTI_AUTHOR("clinicalDocument/body/ProblemiAperti/author",0.50f),
	CDA_BODY_TERAPIA_CONS_PARTICIPANT("ClinicalDocument/body/TERAPIA_FARMACOLOGICA_CONSIGLIATA/participant/",0.50f),
	CDA_BODY_TERAPIA_ING_AUTHOR("ClinicalDocument/body/TerapiaFarmacologicaIngresso/author",0.50f),
	CDA_BODY_TERAPIA_RIC_ADMIN_PART("ClinicalDocument/body/TerapiaFarmacologicaRicovero/MedAdministration/participant",0.50f),
	CDA_BODY_TERAPIA_RIC_ADMIN_PERFOMER("ClinicalDocument/body/TerapiaFarmacologicaRicovero/MedAdministration/performer",0.50f),
	CDA_BODY_VACC_PARTICIPANT("ClinicalDocument/body/vaccinazione/participant",0.50f);

	 
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