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
package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.DataQualityIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TraversabilityValidationRule implements ValidationRule {

	private final FhirContext fhirContext;

	@Override
	public ValidationType type() {
		return ValidationType.TRAVERSABILITY;
	}

	@Override
	public List<DataQualityIssueDTO> validate(ValidationContext context) {

		if (context == null || context.bundle() == null) {
			return List.of(issue("Bundle", "No bundle available to validate"));
		}

		Map<String, Resource> resources = index(context.bundle());
		List<Resource> roots = resources.values().stream().filter(DocumentReference.class::isInstance).toList();

		if (roots.isEmpty()) return List.of(issue("Bundle", "No DocumentReference root found"));

		Set<Resource> visited = new HashSet<>();
		ArrayDeque<Resource> pending = new ArrayDeque<>(roots);

		while (!pending.isEmpty()) {
			Resource resource = pending.removeFirst();
			if (!visited.add(resource)) continue;

			fhirContext.newTerser().getAllResourceReferences(resource).forEach(info -> {
				String reference = info.getResourceReference().getReferenceElement().getValue();
				Resource target = resources.get(reference);
				if (target != null && !visited.contains(target)) pending.addLast(target);
			});
		}

		List<DataQualityIssueDTO> issues = new ArrayList<>();
		if (context.bundle().getEntry() != null) {
			for (Bundle.BundleEntryComponent entry : context.bundle().getEntry()) {

				if (entry.hasResource() && !visited.contains(entry.getResource())) {
					String reference = entry.hasFullUrl()
							? entry.getFullUrl()
							: entry.getResource().getIdElement().toUnqualifiedVersionless().getValue();
					issues.add(issue(reference, "Resource is not reachable from a DocumentReference"));
				}

			}
		}

		return issues;

	}

	private Map<String, Resource> index(Bundle bundle) {

		Map<String, Resource> resources = new HashMap<>();

		if (bundle.getEntry() != null) {

			for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
				if (!entry.hasResource()) continue;
				Resource resource = entry.getResource();
				if (entry.hasFullUrl()) resources.put(entry.getFullUrl(), resource);
				if (resource.hasIdElement()) {
					resources.put(resource.getIdElement().getValue(), resource);
					resources.put(resource.fhirType() + "/" + resource.getIdElement().getIdPart(), resource);
				}
			}
		}

		return resources;
	}

	private DataQualityIssueDTO issue(String path, String message) {
		return new DataQualityIssueDTO(IssueSeverity.ERROR.toCode().toUpperCase(),
				IssueType.NOTFOUND.toCode().toUpperCase(), List.of(path), message);
	}

}
