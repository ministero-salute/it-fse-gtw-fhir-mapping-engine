/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CDAHelper {
	
	private CDAHelper() {
		
	}
    
    public static String extractTemplateId(final String cda) {
		try {
            log.debug("Extracting template_id from CDA");
			final Document docT = Jsoup.parse(cda);
			return docT.select("templateid").get(0).attr("root");
		} catch(final Exception ex) {
			log.error("Error while extracting template id from CDA", ex);
			throw new BusinessException("Error while extracting template id from CDA", ex);
		}
	}
}
