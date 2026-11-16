package it.finanze.sanita.fse2.gtwfhirmappingenginems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FhirResourceNoEngineDTO {

    private DocumentReferenceDTO documentReferenceDTO;
	
	private String cda;

}
