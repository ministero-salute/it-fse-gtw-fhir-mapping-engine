/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.provider;

import java.util.HashMap;
import java.util.Map;

public class ValueSetProvider {

	private static ValueSetProvider instance = new ValueSetProvider();

	private Map<String, String> valueSet;

	public ValueSetProvider() {
		valueSet = new HashMap<>();
	}
	
	public static ValueSetProvider getInstance() {
		return instance;
	}
	
	public Map<String, String> getValueSet() {
		return valueSet;
	}
}
