package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.ResourceType;

import java.util.List;
import java.util.Optional;

import static org.hl7.fhir.r4.model.DocumentReference.DocumentReferenceRelatesToComponent;

public class TransformUtility {
	public static void addRelatesTo(Bundle bundle, String id) {
		Optional<Bundle.BundleEntryComponent> ref = bundle.
			getEntry().
			stream().
			filter(cmp -> cmp.getResource().getResourceType().equals(ResourceType.DocumentReference)).
			findFirst();

		if(ref.isPresent()) {
			DocumentReference entry = (DocumentReference) ref.get().getResource();
			DocumentReferenceRelatesToComponent relatesTo = new DocumentReferenceRelatesToComponent();
			relatesTo.setId(id);
			entry.setRelatesTo(List.of(relatesTo));
		}
	}
}
