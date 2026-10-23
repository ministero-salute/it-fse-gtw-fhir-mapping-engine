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

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;
import org.springframework.stereotype.Component;

import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds a narrative report of resources removed during transformation.
 * Appends OperationOutcome issue information to Composition.text before removing
 * all OperationOutcome entries. Leaves the Bundle unchanged when reporting fails.
 */
@Component
@Slf4j
public class ResourceRemovalNarrativeNormalizer {

    /**
     * Returns the normalized Bundle and diagnostics about earlier resource removals.
     * Modifies and returns the same Bundle instance on successful reporting.
     */
    public NormalizationResult normalize(Bundle bundle) {
        List<OperationOutcome> outcomes = bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(OperationOutcome.class::isInstance)
            .map(OperationOutcome.class::cast)
            .toList();

        if (outcomes.isEmpty()) {
            return new NormalizationResult(bundle, List.of());
        }

        Composition composition = bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(Composition.class::isInstance)
            .map(Composition.class::cast)
            .findFirst().orElse(null);

        if (composition == null) {
            log.warn("Skipping resource removal report: Composition not found; OperationOutcomes retained");
            return new NormalizationResult(bundle, List.of());
        }

        Narrative narrative;
        List<DataQualityIssueDTO> issues = new ArrayList<>();

        try {
            narrative = composition.hasText() ? composition.getText().copy() : new Narrative();
            XhtmlNode div = narrative.getDiv();

            if (div == null) {
                div = new XhtmlNode(NodeType.Element, "div");
                div.setAttribute("xmlns", XhtmlParser.XHTML_NS);
                narrative.setDiv(div);
            }

            XhtmlNode report = div.div();
            report.h2().addText("Risorse soppresse");
            XhtmlNode list = report.ul();

            // Add an entry for each removed resource to the narrative
            for (OperationOutcome outcome : outcomes) {
                XhtmlNode item = list.li();
                item.para().addText(outcome.hasId()
                    ? outcome.fhirType() + ": " + outcome.getIdElement().getIdPart() : outcome.fhirType());

                if (outcome.hasText() && outcome.getText().hasDiv()) {
                    addField(item, "Descrizione", outcome.getText().getDiv().allText());
                }

                for (OperationOutcomeIssueComponent issue : outcome.getIssue()) {
                    XhtmlNode detail = item.div();
                    addField(detail, "Severità", issue.hasSeverity() ? issue.getSeverity().toCode() : null);
                    addField(detail, "Codice", issue.hasCode() ? issue.getCode().toCode() : null);

                    List<String> messages = new ArrayList<>();
                    if (issue.hasDiagnostics()) {
                        messages.add(issue.getDiagnostics());
                        addField(detail, "Diagnostica", issue.getDiagnostics());
                    }

                    if (issue.hasDetails()) {
                        if (issue.getDetails().hasText()) {
                            messages.add(issue.getDetails().getText());
                            addField(detail, "Dettagli", issue.getDetails().getText());
                        }

                        issue.getDetails().getCoding().forEach(coding -> {
                            List<String> values = new ArrayList<>();
                            if (coding.hasSystem()) values.add(coding.getSystem());
                            if (coding.hasCode()) values.add(coding.getCode());
                            if (coding.hasDisplay()) values.add(coding.getDisplay());

                            String value = String.join(" | ", values);
                            addField(detail, "Codifica", value);
                            if (!value.isEmpty()) messages.add(value);
                        });
                    }

                    List<String> paths = new ArrayList<>();
                    issue.getExpression().forEach(path -> {
                        paths.add(path.getValue());
                        addField(detail, "Percorso", path.getValue());
                    });

                    issue.getLocation().forEach(path -> {
                        paths.add(path.getValue());
                        addField(detail, "Posizione", path.getValue());
                    });

                    issues.add(new DataQualityIssueDTO(
                        issue.hasSeverity() ? issue.getSeverity().name() : null,
                        issue.hasCode() ? issue.getCode().name() : null,
                        paths, String.join("\n", messages)));
                }
            }

            narrative.setStatus(NarrativeStatus.ADDITIONAL);
        } catch (Exception ex) {
            log.warn("Skipping resource removal report: narrative could not be extended; OperationOutcomes retained", ex);
            return new NormalizationResult(bundle, List.of());
        }

        composition.setText(narrative);

        // Remove all OperationOutcome resources only if the narrative alteration is successful
        bundle.getEntry().removeIf(entry -> entry.getResource() instanceof OperationOutcome);

        return new NormalizationResult(bundle, issues);
    }

    private void addField(XhtmlNode parent, String label, String value) {
        if (value != null && !value.isBlank()) {
            parent.para().addText(label + ": " + value);
        }
    }

}
