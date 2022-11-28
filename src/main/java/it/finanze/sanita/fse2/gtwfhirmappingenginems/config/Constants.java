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
		 * Base path.
		 */
		public static final String BASE = "it.finanze.sanita.fse2.gtwfhirmappingenginems";


		/**
		 * Configuration path.
		 */
		public static final String CONFIG = "it.finanze.sanita.fse2.gtwfhirmappingenginems.config";
		
		/**
		 * Configuration mongo path.
		 */
		public static final String CONFIG_MONGO = "it.finanze.sanita.fse2.gtwfhirmappingenginems.config.mongo";
		
		/**
		 * Configuration mongo repository path.
		 */
		public static final String REPOSITORY_MONGO = "it.finanze.sanita.fse2.gtwfhirmappingenginems.repository";

		
		private ComponentScan() {
			//This method is intentionally left blank.
		}

	}
	
	public static final class Collections {

		public static final String STRUCTURE_DEFINITIONS = "structure_definition";
		
		public static final String STRUCTURE_MAP = "structure_map";
		
		public static final String VALUESET = "valueset";

		 
		private Collections() {

		}
	}
 
	public static final class Profile {
		public static final String TEST = "test";

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
	private Constants() {

	}

}
