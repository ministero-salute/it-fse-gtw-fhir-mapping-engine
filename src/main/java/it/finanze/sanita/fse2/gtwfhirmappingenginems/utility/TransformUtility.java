package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

import org.hl7.fhir.r4.model.*;

import java.util.Arrays;
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

	public static void sortByComposition(Bundle doc) {

		// Find Composition from bundle
		Optional<Bundle.BundleEntryComponent> composition = doc.getEntry()
				.stream()
				.filter(entry -> ResourceType.Composition.equals(entry.getResource().getResourceType()))
				.findFirst();

		// Integrity check
		if (composition.isEmpty()) {
			throw new IllegalStateException("Cannot sort Bundle due to missing Composition");
		}

		// Place composition at the first position of entries
		doc.getEntry().remove(composition.get());
		doc.getEntry().add(0, composition.get());
	}

	public static final List<Class<?>> IMMUTABLE_RESOURCES = Arrays.asList(
			Patient.class,
			Practitioner.class,
			Organization.class,
			Location.class
	);

	public static void prepareForDelete(Bundle bundle, DocumentReference documentReference) {
		bundle.setType(Bundle.BundleType.TRANSACTION);
		bundle.getEntry().add(getBundleEntryComponent(documentReference));
		bundle.getEntry().removeIf(TransformUtility::isImmutable);
		bundle.getEntry().forEach(TransformUtility::setDeletionRequest);
	}

	private static void setDeletionRequest(Bundle.BundleEntryComponent entry) {
		Bundle.BundleEntryRequestComponent request = new Bundle.BundleEntryRequestComponent();
		request.setMethod(Bundle.HTTPVerb.DELETE);
		request.setUrl(getUrl(entry));
		entry.setRequest(request);
		entry.setFullUrl(null);
		entry.setSearch(null);
	}

	private static String getUrl(Bundle.BundleEntryComponent entry) {
		IdType idType = entry.getResource().getIdElement();
		return idType.getResourceType() + "/" + idType.getIdPart();
	}


	private static Bundle.BundleEntryComponent getBundleEntryComponent(DocumentReference documentReference) {
		Bundle.BundleEntryComponent component = new Bundle.BundleEntryComponent();
		component.setResource(documentReference);
		return component;
	}

	private static boolean isImmutable(Bundle.BundleEntryComponent bundleEntryComponent) {
		return isImmutable(bundleEntryComponent.getResource());
	}

	private static boolean isImmutable(Resource resource) {
		return IMMUTABLE_RESOURCES
				.stream()
				.anyMatch(immutableResource -> resource.getClass() == immutableResource);
	}
}
