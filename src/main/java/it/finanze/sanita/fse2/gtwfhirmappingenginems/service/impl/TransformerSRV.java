/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import static java.util.Arrays.stream;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.config.Constants;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.Trasformer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.CDAHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton.StructureMapSingleton;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton.ValueSetSingleton;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransformerSRV implements ITransformerSRV {

	/**
	 * Serial version uid.
	 */
	private static final long serialVersionUID = 527508018653373276L;

	@Autowired
	private IStructuresRepo structuresRepo;

	@Autowired
	private Environment environment;

	@PostConstruct
	void postConstruct() {
		ValueSetSingleton.initialize(structuresRepo);
		StructureMapSingleton.initialize(structuresRepo);
	}


	@Override
	public String transform(final String cda) {
		String bundle = "";
		try {
			if(isDevProfile() && cda.contains("http://hl7.org/fhir/tutorial")) {
				StructureMapDTO mapETY = structuresRepo.findMapByName("step01");
				StructureMapSingleton singleton = StructureMapSingleton.getAndUpdateInstance(mapETY.getNameStructureMap());
				bundle = Trasformer.transform(new ByteArrayInputStream(cda.getBytes(StandardCharsets.UTF_8)), singleton.getStructureMap());
			} else {
				String templateIdRoot = CDAHelper.extractTemplateId(cda);
				StructureMapDTO mapETY = structuresRepo.findMapByTemplateIdRoot(templateIdRoot);
				if(mapETY!=null) {
					StructureMapSingleton singleton = StructureMapSingleton.getAndUpdateInstance(mapETY.getNameStructureMap());
					bundle = Trasformer.transform(new ByteArrayInputStream(cda.getBytes(StandardCharsets.UTF_8)), singleton.getStructureMap());
				} else { 
					throw new BusinessException("Nessuna structured map trovata con il template id root : " + templateIdRoot);
				}
			}

		} catch(Exception ex) {
			log.error("Error while perform transform of clinical document : " , ex);
			throw new BusinessException("Error while perform transform of clinical document : " , ex);
		}
		return bundle;
	}

	private boolean isDevProfile() {
		// Get profiles
		String[] profiles = environment.getActiveProfiles();
		// Verify if exists the test profile
		Optional<String> exists = stream(profiles).filter(i -> i.equals(Constants.Profile.DEV)).findFirst();
		// Return
		return exists.isPresent();
	}


}
