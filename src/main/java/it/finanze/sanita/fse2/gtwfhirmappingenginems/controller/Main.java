package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.StructureMap;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import ch.ahdis.matchbox.engine.CdaMappingEngine.CdaMappingEngineBuilder;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.FileUtility;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) throws FHIRException, IOException, URISyntaxException {
        String cda = new String(FileUtility.getFileFromInternalResources("cda.xml"), StandardCharsets.UTF_8);
        String uri = "http://salute.gov.it/ig/cda-fhir-maps/StructureMap/LetteraDimissioneOspedaliera";
        CdaMappingEngine engine = new CdaMappingEngineBuilder().getEngine();
        byte[] map = FileUtility.getFileFromInternalResources("LetteraDimissioneOspedaliera_v2.8.map");
        byte[] mapCdaToFhirDataType = FileUtility.getFileFromInternalResources("CdaToFhirDataTypes_v2.3-2.map");
        byte[] mapFullHeader = FileUtility.getFileFromInternalResources("FULLHEADER_v3.5.map");

        registerMap(engine, mapFullHeader, "http://salute.gov.it/ig/cda-fhir-maps/StructureMap/FULLHEADER", "3.5");
        registerMap(engine, mapCdaToFhirDataType, "http://salute.gov.it/ig/cda-fhir-maps/StructureMap/CdaToFhirDataTypes", "2.3");
        registerMap(engine, map, "http://salute.gov.it/ig/cda-fhir-maps/StructureMap/LetteraDimissioneOspedaliera", "2.8");

        ExecutorService executor = Executors.newFixedThreadPool(4); // Imposta il numero di thread desiderato
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (int i = 0; i < 200; i++) {  
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Bundle bundle = engine.transformCdaToFhir(cda, uri);
                    System.out.println(new JsonParser().composeString(bundle));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long end = System.currentTimeMillis() - start;
        System.out.println("Tempo di esecuzione: " + end + " ms");

        // Chiude l'ExecutorService
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

	private static void registerMap(CdaMappingEngine engine,byte[] mapContent,String url,String version) {
		try {

			// Retrieve data
			String data = new String(mapContent, StandardCharsets.UTF_8);
			// Parse map
			StructureMap map = engine.parseMap(data);

			// Set URI and version
			map.setUrl(url);
			map.setVersion(version);
			// Add to engine
			engine.addCanonicalResource(map);
		} catch (Exception ex) {
			System.out.println("Errore");
		}
	}



}
