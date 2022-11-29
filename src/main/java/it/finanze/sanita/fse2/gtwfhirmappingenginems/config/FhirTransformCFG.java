/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.TransformALGEnum;
import lombok.Data;


@Data
@Component
public class FhirTransformCFG {

	@Value("${fhir.transform.alg}")
	private TransformALGEnum algToRemoveDuplicate;
	 
}
