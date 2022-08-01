package it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.helper.ContextHelper;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.IStructureMapRepo;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.repository.entity.StructureMapETY;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StructureMapSingleton {
	
	private static StructureMapSingleton instance;
    
    @Getter
    private static Map<String, StructureMapSingleton> mapInstance;
    
    @Getter
    private String mapName;
    
    @Getter
    private StructureMap structureMap;
    
    private static IStructureMapRepo mapRepo;

	private StructureMapSingleton(String inMapName,StructureMap inStructureMap) {
		structureMap = inStructureMap;
		mapName = inMapName;
	}

 
	public static void initialize(final IStructureMapRepo inMapRepo) {
		mapRepo = inMapRepo;
	}
	
    public static StructureMapSingleton getAndUpdateInstance(final String mapUri) {
    	String mapName = StringUtility.getNameFromUrl(mapUri);
    	if(mapInstance != null) {
			instance = mapInstance.get(mapName); 
		} else {
			mapInstance = new HashMap<>();
		}

    	synchronized(StructureMapSingleton.class) {
			if (instance == null) {
				try { 
	            	StructureMapETY mapETY = mapRepo.findMapByName(mapName);
	            	StructureMapUtilities smu5 = new StructureMapUtilities(ContextHelper.getSimpleWorkerContextR5());
	            	StructureMap structuredMap = smu5.parse(new String(mapETY.getContentStructureMap().getData(),StandardCharsets.UTF_8) , "map");
	                instance = new StructureMapSingleton(mapName, structuredMap);
	                mapInstance.put(mapName,instance);
				} catch(Exception ex) {
					log.error("Error while retrieving and structure :", ex);
					throw new BusinessException("Error while retrieving and structure :", ex);
				}
			}
		}
		return instance;
	}
   
}
