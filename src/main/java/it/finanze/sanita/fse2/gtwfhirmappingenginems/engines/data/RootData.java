/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.engines.data;

import java.util.ArrayList;
import java.util.List;

public final class RootData {
    private final String uri;
    private final List<String> templates;

    public RootData(String uri, List<String> templates) {
        this.uri = uri;
        this.templates = templates;
    }

    public List<String> getTemplates() {
        return new ArrayList<>(templates);
    }

    public String getUri() {
        return uri;
    }
}
