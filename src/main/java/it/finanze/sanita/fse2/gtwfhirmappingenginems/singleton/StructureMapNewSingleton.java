/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.MapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.StructureMapDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructuresRepo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StructureMapNewSingleton {

	private static StructureMapNewSingleton instance;

	@Getter
	private static Map<String, StructureMapNewSingleton> mapInstance;

	@Getter
	private StructureMap rootMap;

	@Getter
	private List<StructureMap> childMaps;

	private static IStructuresRepo structuresRepo;

	@Getter
	private String objectId;

	private StructureMapNewSingleton(StructureMap inRootMap,List<StructureMap> inChildMaps, String inObjectId) {
		rootMap = inRootMap;
		childMaps = inChildMaps;
		objectId = inObjectId;
	}


	public static void initialize(final IStructuresRepo inStructuresRepo) {
		if(structuresRepo==null) {
			structuresRepo = inStructuresRepo;
		}
	}

	public static StructureMapNewSingleton getAndUpdateInstance(final StructureMapDTO structuresMapDTO , final String objectId) {
		if(mapInstance != null) {
			instance = mapInstance.get(objectId); 
		} else {
			mapInstance = new HashMap<>();
		}

		synchronized(StructureMapNewSingleton.class) {
			if (instance == null) {
				try { 
					StructureMapUtilities smu5 = new StructureMapUtilities(ContextHelper.getSimpleWorkerContextR5());
					StructureMap rootMap = smu5.parse(new String(structuresMapDTO.getRootMap().getContentStructureMap().getData(),StandardCharsets.UTF_8) , "map");
					List<StructureMap> childMaps = new ArrayList<>();
					for(MapDTO map : structuresMapDTO.getChildsMap()) {
						childMaps.add(smu5.parse(new String(map.getContentStructureMap().getData(),StandardCharsets.UTF_8) , "map"));
					}
					instance = new StructureMapNewSingleton(rootMap, childMaps,objectId);
					mapInstance.put(objectId,instance);
				} catch(Exception ex) {
					log.error("Error while retrieving and structure :", ex);
					throw new BusinessException("Error while retrieving and structure :", ex);
				}
			}
		}
		return instance;
	}

}
