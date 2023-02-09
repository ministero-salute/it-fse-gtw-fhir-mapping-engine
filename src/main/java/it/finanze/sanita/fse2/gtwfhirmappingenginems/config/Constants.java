/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.config;

/**
 * 
 *
 * Constants application.
 */
public final class Constants {

    /**
	 *	Path scan.
	 */
	public static final class ComponentScan {

		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.finanze.sanita.fse2.gtwfhirmappingenginems.config.mongo";
		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class Collections {

		public static final String TRANSFORM = "transform";
		public static final String ENGINE = "engine";

		private Collections() {

		}
	}
 
	public static final class Profile {
		public static final String TEST = "test";
		public static final String TEST_ENGINE = "test-engine";

		public static final String TEST_PREFIX = "test_";

		/**
		 * Dev profile.
		 */
		public static final String DEV = "dev";

		
		/** 
		 * Constructor.
		 */
		private Profile() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class DocumentReference {
		/**
		 * Constant that defines bundle fhir language.
		 */
		public static final String BUNDLE_FHIR_LANGUAGE = "it-IT";

		/**
		 * Constant that defines bundle fhir type.
		 */
		public static final String BUNDLE_FHIR_MIME_TYPE = "application/pdf";

		/**
		 * Constant that defines bundle fhir document reference identifier.
		 */
		public static final String BUNDLE_FHIR_DOCUMENT_REFERENCE_ID = "Document00";
		
		/** 
		 * Constructor.
		 */
		private DocumentReference() {
			//This method is intentionally left blank.
		}

	}
  
	
	/**
	 *	Constants.
	 */
	private Constants() {}

	public static final class Logs {
		// Engine
		public static final String ERR_ENG_UNAVAILABLE = "Inizializzazione engines in corso";
		public static final String ERR_ENG_NULL = "L'istanza engine richiesta è nulla";
		public static final String ERR_ENG_ROOT_MAP = "Root map inesistente per l'engine in uso ";
		// EngineBuilder
		public static final String ERR_BLD_REGISTER_FHIR = "Impossibile registrare entità %s di tipo %s (builder)";
		public static final String ERR_BLD_FIND_BY_ID_ENGINE = "Impossibile recupero entità engine per id (builder)";
		public static final String ERR_BLD_FIND_BY_ID_FHIR = "Impossibile recupero entità fhir per id (builder)";
		public static final String ERR_BLD_ROOT_UNAVAILALE = "La root map '%s' utilizza un object-id '%s' non presente tra i files (builder)";
		public static final String ERR_BLD_ENGINE_UNAVAILALE = "Impossibile inizializzare engine (builder)";
		public static final String ERR_BLD_UKNOWN_TYPE = "La risorsa '%s' utilizza un type sconosciuto '%s' (builder)";
		// Repositories
		public static final String ERR_REP_FIND_BY_ID_FHIR = "Impossibile recupero entità fhir per id";
		public static final String ERR_REP_FIND_BY_ID_ENGINE = "Impossibile recupero entità engine per id";

		private Logs() {}
	}
}
