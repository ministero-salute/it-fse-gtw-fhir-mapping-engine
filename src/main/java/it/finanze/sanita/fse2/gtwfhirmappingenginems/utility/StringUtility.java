/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * 
 * Copyright 2023 Ministero della Salute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import java.security.MessageDigest;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtility {

	private static final String SHA_ALGORITHM = "SHA-256";
	
	
	     
	public static String generateUUID() {
	    return UUID.randomUUID().toString();
	}

	/**
	 * Returns {@code true} if the String passed as parameter is null or empty.
	 * 
	 * @param str	String to validate.
	 * @return		{@code true} if the String passed as parameter is null or empty.
	 */
	public static boolean isNullOrEmpty(final String str) {
		return str == null || str.isEmpty();
	}
	
	/**
	 * Returns the encoded String of the SHA-256 algorithm represented in base 64.
	 * 
	 * @param objectToEncode String to encode.
	 * @return String Encoded.
	 */
	public static String encodeSHA256(final byte[] objectToEncode) {
		try {
			final MessageDigest digest = MessageDigest.getInstance(SHA_ALGORITHM);
			return Hex.encodeHexString(digest.digest(objectToEncode));
		} catch (final Exception e) {
			throw new BusinessException(e);
		}
	}
	
}
