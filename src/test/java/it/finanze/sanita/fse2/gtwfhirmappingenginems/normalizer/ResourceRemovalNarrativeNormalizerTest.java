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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.normalizer;

import static org.junit.jupiter.api.Assertions.*;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.formats.JsonParser;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.junit.jupiter.api.Test;

class ResourceRemovalNarrativeNormalizerTest {

    @Test
    void noOutcomesLeavesInputUnchanged() {

        Bundle bundle = new Bundle();
        NormalizationResult result = new ResourceRemovalNarrativeNormalizer().normalize(bundle);

        assertSame(bundle, result.bundle());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void appendsAllIssuesPreservesNarrativeAndEscapesMessages() throws Exception {

        // The fixture includes two outcomes with two issues each, including repeated and markup-like messages.
        Bundle bundle;
        try (var input = getClass().getResourceAsStream("/bundles/laboratory-report-with-removal-outcomes.json")) {
            assertNotNull(input);
            bundle = (Bundle) new JsonParser().parse(input);
        }

        Bundle before = bundle.copy();
        Composition composition = (Composition) bundle.getEntryFirstRep().getResource();
        XhtmlNode original = composition.getText().getDiv();
        int originalChildCount = original.getChildNodes().size();

        NormalizationResult result = new ResourceRemovalNarrativeNormalizer().normalize(bundle);

        // Only the fixture's two outcomes disappear; all four issues are returned as diagnostics.
        assertSame(bundle, result.bundle());
        assertEquals(before.getEntry().size() - 2, bundle.getEntry().size());
        assertTrue(bundle.getEntry().stream().noneMatch(entry -> entry.getResource() instanceof OperationOutcome));
        assertEquals(4, result.issues().size());

        // Append one report block without mutating the old tree or rewriting its existing children.
        assertEquals(originalChildCount, original.getChildNodes().size());
        assertEquals(originalChildCount + 1, composition.getText().getDiv().getChildNodes().size());
        for (int index = 0; index < originalChildCount; index++) {
            assertTrue(original.getChildNodes().get(index).equalsDeep(composition.getText().getDiv().getChildNodes().get(index)));
        }
        assertEquals(original.getAttributes(), composition.getText().getDiv().getAttributes());

        // Clinical resources and Composition fields/sections must not change: only Composition.text may differ.
        for (int index = 1; index < bundle.getEntry().size(); index++) {
            assertTrue(before.getEntry().get(index).equalsDeep(bundle.getEntry().get(index)));
        }

        Composition expected = (Composition) before.getEntryFirstRep().getResource();
        expected.setText(composition.getText());
        assertTrue(expected.equalsDeep(composition));
        assertEquals(Narrative.NarrativeStatus.ADDITIONAL, composition.getText().getStatus());

        // Preserve both diagnostic and details content, including source paths and coding information.
        String text = composition.getText().getDiv().allText();
        assertTrue(text.contains("Risorse soppresse"));
        assertTrue(text.contains("Value < 5 & <script>alert(1)</script>"));
        assertTrue(text.contains("Informazioni aggiuntive"));
        assertTrue(text.contains("urn:test | missing | Dato mancante"));
        assertTrue(text.contains("ClinicalDocument/author"));
        assertTrue(text.contains("source.xml:42"));
        assertTrue(text.contains("Secondo problema"));

        // Check actual serialized XHTML escaping and that a FHIR JSON round trip retains the report.
        String json = new JsonParser().composeString(bundle);
        assertTrue(json.contains("&lt;script&gt;"));
        assertFalse(json.contains("<script>"));

        Bundle reparsed = (Bundle) new JsonParser().parse(json);
        assertEquals(text, ((Composition) reparsed.getEntryFirstRep().getResource()).getText().getDiv().allText());

        // Once outcomes have been removed, another run must not append the report again.
        assertTrue(new ResourceRemovalNarrativeNormalizer().normalize(bundle).issues().isEmpty());
        assertEquals(text, composition.getText().getDiv().allText());
    }

    @Test
    void createsNarrativeOrAppendsToPlainTextDiv() throws Exception {
        // Neither missing text nor an unformatted XHTML text node requires a rich narrative fixture.
        for (boolean hasNarrative : new boolean[] {false, true}) {
            Bundle bundle = new Bundle();
            Composition composition = new Composition();

            if (hasNarrative) {
                composition.getText().setDiv(new XhtmlParser().parseFragment(
                    "<div xmlns=\"http://www.w3.org/1999/xhtml\">Testo semplice</div>"));
            }

            bundle.addEntry().setResource(composition);
            bundle.addEntry().setResource(outcome());

            new ResourceRemovalNarrativeNormalizer().normalize(bundle);

            assertEquals(1, bundle.getEntry().size());
            assertTrue(composition.getText().getDiv().allText().contains("Risorse soppresse"));

            if (hasNarrative) {
                // Existing plain text stays as the first child, without being wrapped or replaced.
                assertEquals("Testo semplice", composition.getText().getDiv().getChildNodes().get(0).getContent());
            }
        }
    }

    @Test
    void missingCompositionRetainsOutcomes() {
        // Best effort: no destination means no report and, importantly, no deletion of diagnostics.
        Bundle missing = new Bundle();
        OperationOutcome outcome = outcome();
        missing.addEntry().setResource(outcome);

        assertTrue(new ResourceRemovalNarrativeNormalizer().normalize(missing).issues().isEmpty());
        assertSame(outcome, missing.getEntryFirstRep().getResource());
    }

    @Test
    void leavesExistingMarkupValidationToDownstreamChecks() {
        // Existing forbidden markup is neither repaired nor rejected by this optional reporting step.
        Bundle bundle = new Bundle();
        Composition composition = new Composition();
        XhtmlNode div = new XhtmlNode(NodeType.Element, "div");
        div.addTag("script").addText("alert(1)");

        composition.getText().setDiv(div);
        bundle.addEntry().setResource(composition);
        bundle.addEntry().setResource(outcome());

        NormalizationResult result = new ResourceRemovalNarrativeNormalizer().normalize(bundle);

        assertEquals(1, result.issues().size());
        assertEquals(1, bundle.getEntry().size());
        assertTrue(div.getChildNodes().get(0).equalsDeep(composition.getText().getDiv().getChildNodes().get(0)));
        assertEquals(1, div.getChildNodes().size());
        assertTrue(composition.getText().getDiv().allText().contains("Risorse soppresse"));
    }

    private OperationOutcome outcome() {
        OperationOutcome outcome = new OperationOutcome();
        outcome.addIssue().setSeverity(OperationOutcome.IssueSeverity.WARNING)
            .setCode(OperationOutcome.IssueType.REQUIRED).setDiagnostics("Organization non creata");

        return outcome;
    }
}
