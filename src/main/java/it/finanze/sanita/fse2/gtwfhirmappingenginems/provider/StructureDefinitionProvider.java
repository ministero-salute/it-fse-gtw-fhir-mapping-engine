//package it.finanze.sanita.fse2.gtwfhirmappingenginems.provider;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.Map;
//
//public class StructureDefinitionProvider {
//
//	private static StructureDefinitionProvider instance = new StructureDefinitionProvider();
//
//	private Map<String, String> structures;
//
//	public StructureDefinitionProvider() {
//		reset();
//	}
//	
//	public static StructureDefinitionProvider getInstance() {
//		return instance;
//	}
//	
//	public Map<String, String> getStructures() {
//		return structures;
//	}
//
//	public void addAll(String path) throws IOException {
//		File folder = new File(path);
//		File[] listOfFiles = folder.listFiles();
//
//		for (int i = 0; i < listOfFiles.length; i++) {
//			if (listOfFiles[i].isFile()) {
//				String fileName = listOfFiles[i].getName();
//				System.out.println("Adding: " + fileName);
//				String content = new String(Files.readAllBytes(Path.of(listOfFiles[i].getPath())));
//  	        	structures.put(fileName, content);
//			}
//		}
//	}
//	
//	public void reset() {
//		structures = new HashMap<>();
//	}
//
//}
