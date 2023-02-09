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
		private Logs() {}
	}
}
