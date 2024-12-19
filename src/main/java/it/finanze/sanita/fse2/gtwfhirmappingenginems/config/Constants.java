
/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright (C) 2023 Ministero della Salute
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
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
		public static final String ENGINE = "engines";

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
		public static final String DOCKER = "docker";

		
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
		public static final String ERR_ENG_ROOT_MAP = "Root map inesistente per engine in uso";
		public static final String ERR_ENG_ROOT_URI = "La root map richiesta esiste ma URI è nullo";
		// EngineBuilder
		public static final String ERR_BLD_REGISTER_FHIR = "Impossibile registrare entità %s di tipo %s (builder)";
		public static final String ERR_BLD_FIND_BY_ID_ENGINE = "Impossibile recupero entità engine per id (builder)";
		public static final String ERR_BLD_SIZE_FILE_LIST_FHIR = "Differenze tra file richiesti (%d) e file ottenuti (%d) (builder)";
		public static final String ERR_BLD_DUPLICATED_ENTRY_FHIR = "Risorsa duplicata tra i file richiesti: %s (builder)";
		public static final String ERR_BLD_EMPTY_FILE_LIST_FHIR = "Impossibile recupero entità fhir per id, lista files vuota (builder)";
		public static final String ERR_BLD_EMPTY_MAP_LIST_FHIR = "Impossibile recupero entità fhir per uri, mappa files vuota (builder)";
		public static final String ERR_BLD_ROOT_UNAVAILALE = "La root map '%s' utilizza un object-id '%s' non presente tra i files (builder)";
		public static final String ERR_BLD_ENGINE_UNAVAILALE = "Impossibile inizializzare engine (builder)";
		public static final String ERR_BLD_UKNOWN_TYPE = "La risorsa '%s' utilizza un type sconosciuto '%s' (builder)";
		// Repositories
		public static final String ERR_REP_FIND_BY_ID_FHIR = "Impossibile recupero entità fhir per id";
		public static final String ERR_REP_FIND_BY_ID_ENGINE = "Impossibile recupero entità engine per id";
		public static final String ERR_REP_FIND_ALL_ENGINES = "Impossibile recupero di tutte le entità engine";
		public static final String ERR_REP_SET_AVAILABLE_ENGINE = "Impossibile impostare engine come disponibile";
		public static final String ERR_REP_CLEANUP_ENGINE = "Impossibile rimuovere gli engine obsoleti";

		// Scheduler
		public static final String ERR_SCH_RUNNING = "Il processo di aggiornamento risulta già avviato";
		public static final String DTO_RUN_TASK_QUEUED = "Processo avviato, verifica i logs";
		private Logs() {}
	}
}
