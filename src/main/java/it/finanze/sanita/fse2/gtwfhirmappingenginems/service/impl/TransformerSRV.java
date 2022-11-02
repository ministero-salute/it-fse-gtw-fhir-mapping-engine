/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.service.impl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engine.Trasformer;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.ITransformerSRV;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton.StructureMapNewSingleton;
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


	@PostConstruct
	void postConstruct() {
		ValueSetSingleton.initialize(structuresRepo);
		StructureMapNewSingleton.initialize(structuresRepo);
	}


	@Override
	public String transform(final String cda, final String objectId) {
		String bundle = "";
		try {
			StructureMapDTO mapsDTO = structuresRepo.findMapsById(objectId);
			if(mapsDTO!=null) {
				StructureMapNewSingleton singleton = StructureMapNewSingleton.getAndUpdateInstance(mapsDTO,objectId);
//				StructureMapSingleton singleton = StructureMapSingleton.getAndUpdateInstance(mapETY.getNameStructureMap(),objectId);
				bundle = Trasformer.transform(new ByteArrayInputStream(cda.getBytes(StandardCharsets.UTF_8)), singleton.getRootMap(),
						objectId);
			} else { 
				throw new BusinessException("Nessuna structured map trovata con object id : " + objectId);
			}
		} catch(Exception ex) {
			log.error("Error while perform transform of clinical document : " , ex);
			throw new BusinessException("Error while perform transform of clinical document : " , ex);
		}
		return bundle;
	}

}
