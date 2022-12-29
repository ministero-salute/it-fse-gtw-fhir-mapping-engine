/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public final class StringUtility {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private StringUtility() {
		// Constructor intentionally empty.
	}
	     
	public static String generateUUID() {
	    return UUID.randomUUID().toString();
	}
}
