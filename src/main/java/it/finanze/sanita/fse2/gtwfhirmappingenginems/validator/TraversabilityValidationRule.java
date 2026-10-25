/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * Copyright (C) 2023 Ministero della Salute
 */
package it.finanze.sanita.fse2.gtwfhirmappingenginems.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.BundleUtil;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.dto.ValidationIssueDTO;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.enums.ValidationType;

/**
 * Builds a directed resource-reference graph and traverses it depth-first
 * from its first DocumentReference node.
 */
@Component
public class TraversabilityValidationRule implements ValidationRule {

	// Graph identifiers use the default parser's resource/fullUrl resolution.
	private static final FhirContext CONTEXT = FhirContext.forR4();

	@Override
	public ValidationType type() {
		return ValidationType.TRAVERSABILITY;
	}

	@Override
	public List<ValidationIssueDTO> validate(ValidationContext context) {
		return findNotTraversed(context.json()).stream()
				.map(resource -> new ValidationIssueDTO("ERROR", "NOTFOUND", List.of(resource),
						"Resource or reference is not reachable from the first DocumentReference"))
				.toList();
	}

	private static List<String> findNotTraversed(String json) {
		Bundle bundle = CONTEXT.newJsonParser().parseResource(Bundle.class, json);
		List<IBaseResource> resources = BundleUtil.toListOfResources(CONTEXT, bundle).stream().distinct().toList();
		List<Node> nodes = resources.stream().map(resource -> new Node(id(resource.getIdElement()))).toList();

		List<Edge> edges = new ArrayList<>();
		for (int i = 0; i < resources.size(); i++) {
			IBaseResource resource = resources.get(i);
			Node source = nodes.get(i);

			CONTEXT.newTerser().getAllResourceReferences(resource).forEach(info -> {
				String targetId = id(info.getResourceReference().getReferenceElement());
				if (targetId != null && !targetId.isEmpty()) {
					edges.add(new Edge(source, new Node(targetId), resource.fhirType() + "." + info.getName()));
				}
			});
		}

		// ID-less resources cannot be graph nodes, but their outgoing edges remain reportable.
		Graph graph = new Graph(nodes.stream().filter(node -> node.id != null).toList(), edges);
		Node start = graph.nodes.stream().filter(node -> node.id.contains("DocumentReference")).findFirst().orElse(null);

		traverse(start, null, graph);

		return graph.notTraversed();
	}

	private static String id(IIdType id) {
		if (id == null) return null;

		// Compare type/ID only: server bases and history versions do not identify new nodes.
		StringBuilder result = new StringBuilder();
		if (id.getResourceType() != null) result.append(id.getResourceType());
		if (id.getIdPart() != null) result.append('/').append(id.getIdPart());

		return result.isEmpty() ? null : result.toString();
	}

	private static void traverse(Node current, Edge from, Graph graph) {
		// Mark the incoming edge even when its target has no corresponding Bundle node.
		graph.markEdge(from);
		if (current == null || current.traversed) return;

		graph.nodes.stream().filter(node -> node.equals(current)).findFirst()
				.ifPresent(node -> node.traversed = true);

		// Snapshot pending edges before recursion, preserving their order and duplicates.
		List<Edge> edgesToVisit = graph.edges.stream()
				.filter(edge -> edge.source.equals(current) && !edge.traversed).toList();

		for (Edge edge : edgesToVisit) {
			traverse(edge.target, edge, graph);
		}
	}

	private static final class Node {
		private final String id;
		private boolean traversed;

		private Node(String id) {
			this.id = id;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) return true;
			return other instanceof Node node && id != null && id.equals(node.id);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}

	private static final class Edge {
		private final Node source;
		private final Node target;
		private final String path;
		private boolean traversed;

		private Edge(Node source, Node target, String path) {
			this.source = source;
			this.target = target;
			this.path = path;
		}

		@Override
		public String toString() {
			return (source.id == null ? "" : source.id) + "^" + target.id + "^" + path;
		}
	}

	private static final class Graph {
		private final List<Node> nodes;
		private final List<Edge> edges;

		private Graph(List<Node> nodes, List<Edge> edges) {
			this.nodes = nodes;
			this.edges = edges;
		}

		private void markEdge(Edge from) {
			if (from == null) return;

			// Equal source/target/path triples may occur more than once; consume one at a time.
			edges.stream().filter(edge -> edge.source.equals(from.source)
					&& edge.target.equals(from.target) && edge.path.equals(from.path) && !edge.traversed)
					.findFirst().ifPresent(edge -> edge.traversed = true);
		}

		private List<String> notTraversed() {
			List<Edge> remainingEdges = edges.stream().filter(edge -> !edge.traversed).toList();
			List<String> result = new ArrayList<>();
			remainingEdges.forEach(edge -> result.add(edge.toString()));

			// Avoid reporting an isolated node again if an untraversed edge already describes it.
			nodes.stream().filter(node -> !node.traversed)
					.filter(node -> remainingEdges.stream().noneMatch(edge -> edge.source.equals(node) || edge.target.equals(node)))
					.forEach(node -> result.add(node.id));

			return result;
		}
	}
}
