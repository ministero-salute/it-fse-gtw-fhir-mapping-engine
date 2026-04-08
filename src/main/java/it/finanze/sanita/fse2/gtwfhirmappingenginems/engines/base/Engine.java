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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.base;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.data.RootData;
import lombok.Getter;

@Getter
public final class Engine {
    private final String id;
    private final ConcurrentHashMap<String, RootData> roots;
    private final ConcurrentHashMap<String, String> files;
    private final BlockingQueue<CdaMappingEngine> pool;
    private final Date insertion;

    public Engine(
        String id,
        Map<String, RootData> roots,
        Map<String, String> files,
        List<CdaMappingEngine> instances  // <-- lista invece di singola istanza
    ) {
        this.id = id;
        this.roots = new ConcurrentHashMap<>(roots);
        this.files = new ConcurrentHashMap<>(files);
        this.insertion = new Date();
        this.pool = new ArrayBlockingQueue<>(instances.size());
        this.pool.addAll(instances);
    }

    public CdaMappingEngine acquire() throws InterruptedException {
        return pool.take();
    }

    public void release(CdaMappingEngine instance) {
        pool.offer(instance);
    }
}