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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.impl;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.controller.IConverterCTL;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.service.IConverterSRV;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementazione del Controller di Conversione Bundle Fhir.
 */
@RestController
public class ConverterCTL implements IConverterCTL {

    private final IConverterSRV converterService;

    public ConverterCTL(IConverterSRV converterService) {
        this.converterService = converterService;
    }

    @Override
    public ResponseEntity<String> convertDocumentToTransaction(String documentBundleJson) {
            String transactionBundleJson =
                    converterService.convertDocumentToTransactionJson(documentBundleJson);
            return ResponseEntity.ok(transactionBundleJson);
    }

}
