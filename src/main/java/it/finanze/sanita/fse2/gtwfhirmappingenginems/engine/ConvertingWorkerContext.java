package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.hl7.fhir.common.hapi.validation.validator.VersionSpecificWorkerContextWrapper;
import org.hl7.fhir.common.hapi.validation.validator.VersionTypeConverterR4;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.ConceptMap;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r5.context.SimpleWorkerContext.IValidatorFactory;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StructureDefinition.StructureDefinitionKind;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.utils.XVerExtensionManager;
import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.utilities.OIDUtils;
import org.hl7.fhir.validation.instance.InstanceValidatorFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.exception.BusinessException;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton.StructureMapSingleton;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.singleton.ValueSetSingleton;
import it.finanze.sanita.fse2.gtwfhirmappingenginems.utility.StringUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptions for StructrueMaps transformation in 5 using
 * VersionSpecificWorkerContextWrapper
 *
 */
@Slf4j
public class ConvertingWorkerContext extends VersionSpecificWorkerContextWrapper {


	private static IValidatorFactory validatorFactory = null;

	private List<org.hl7.fhir.r5.model.StructureDefinition> myAllStructures = null;

	@Getter
	private final ValidationSupportContext myValidationSupportContext;

	private final IVersionTypeConverter myModelConverter;

	private XVerExtensionManager xverManager;
 


	public ConvertingWorkerContext(IValidationSupport myValidationSupport) {
		super(new ValidationSupportContext(myValidationSupport), new VersionTypeConverterR4());
		try {
			this.myValidationSupportContext = new ValidationSupportContext(myValidationSupport);
			this.myModelConverter = new VersionTypeConverterR4();
			if (validatorFactory==null) {
				validatorFactory = new InstanceValidatorFactory();
			}
		} catch(Exception ex) {
			log.error("Error while initialiting converting worker context :",ex);
			throw new BusinessException("Error while initialiting converting worker context :",ex);
		}
	}


	@Override
	public <T extends Resource> T fetchResourceWithException(Class<T> clazz, String theUri) throws FHIRException {
		return fetchResource(clazz, theUri);
	}


	@Override
	public List<org.hl7.fhir.r5.model.StructureDefinition> allStructures() {

		List<org.hl7.fhir.r5.model.StructureDefinition> retVal = myAllStructures;
		if (retVal == null) {
			DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport(FhirContext.forR4());
			retVal = new ArrayList<>();
			for (IBaseResource next : defaultProfileValidationSupport.fetchAllStructureDefinitions()) {
				try {
					Resource converted = myModelConverter.toCanonical(next);
					retVal.add((org.hl7.fhir.r5.model.StructureDefinition) converted);
				} catch (FHIRException e) {
					throw new InternalErrorException(e);
				}
			}
			myAllStructures = retVal;
		}

		return retVal;
	}


	@Override
	public org.hl7.fhir.r5.model.StructureDefinition fetchTypeDefinition(String typeName) { 
		return null;
	}

	@Override
	public List<String> getTypeNames() {
		List<String> result = new ArrayList<>();
		for (org.hl7.fhir.r5.model.StructureDefinition sd : getStructures()) {
			if (sd.getKind() != StructureDefinitionKind.LOGICAL && sd.getDerivation() == org.hl7.fhir.r5.model.StructureDefinition.TypeDerivationRule.SPECIALIZATION)
				result.add(sd.getName());
		}
		Collections.sort(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends org.hl7.fhir.r4.model.Resource> T fetchResourceAsR4(Class<T> clazz, String uri) {
		return (T) doFetchResource(clazz, uri);
	}

	public StructureMap fixMap(@ResourceParam StructureMap theResource) {
		if (theResource!=null) {
			// don't know why a # is prefixed to the contained it
			for (org.hl7.fhir.r5.model.Resource r : theResource.getContained()) {
				if (r instanceof org.hl7.fhir.r5.model.ConceptMap && ((org.hl7.fhir.r5.model.ConceptMap) r).getId().startsWith("#")) {
					r.setId(((org.hl7.fhir.r5.model.ConceptMap) r).getId().substring(1));
				}
			}
		}
		return theResource;
	}

	@Override
	public org.hl7.fhir.r5.model.StructureMap getTransform(final String url) {
		return (StructureMap) doFetchResource(org.hl7.fhir.r5.model.StructureMap.class, url);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Resource> T fetchResource(Class<T> clazz, String uri) { 
		if (StringUtility.isNullOrEmpty(uri)) {
			return null;
		} 
		return (T) doFetchResource(clazz, uri);
	}

	@SuppressWarnings("unchecked")
	private <T extends IBaseResource> IBaseResource doFetchResource(@Nullable Class<T> theClass, String theUri) {
		IBaseResource out = null;
		IBaseResource myNoMatch = null;

		if (theClass == null || Resource.class.equals(theClass)) {
			Supplier<IBaseResource>[] fetchers = new Supplier[] { () -> doFetchResource(ValueSet.class, theUri),
					() -> doFetchResource(CodeSystem.class, theUri), () -> doFetchResource(StructureDefinition.class, theUri),
					() -> doFetchResource(Questionnaire.class, theUri),() -> doFetchResource(ConceptMap.class, theUri) };
			return Arrays.stream(fetchers).map(t -> t.get()).filter(t -> t != myNoMatch).findFirst().orElse(myNoMatch);
		}
		String resourceType = "UNKNOWN";
		if (theClass != null) {
			resourceType = theClass.getSimpleName(); 
		}
		switch (resourceType) {

		case "ValueSet":
			ValueSetSingleton valuesetSingleton = ValueSetSingleton.getAndUpdateInstance(theUri);
			out = valuesetSingleton.getValueSet();
			break; 
		case "StructureDefinition": {
			for (org.hl7.fhir.r5.model.StructureDefinition sd:myAllStructures) {
				if (sd.getUrl().equalsIgnoreCase(theUri)) {
					return (T)sd;
				}
			}
			for (org.hl7.fhir.r5.model.StructureDefinition sd:myAllStructures) {
				String name = StringUtility.getNameFromUrl(sd.getUrl());
				if (name.equalsIgnoreCase(theUri)) {
					return (T)sd;
				}
			}
			break;
		} 
		case "StructureMap":
			StructureMapSingleton structuredMapSingleton = StructureMapSingleton.getAndUpdateInstance(theUri);
			out = structuredMapSingleton.getStructureMap();
			break;
		default:
			log.warn("Can't fetch resource type: {}", resourceType);
			break;
		}
		return out;
	}


	@Override
	public String oid2Uri(String oid) {
		return OIDUtils.getUriForOid(oid);
	}

	@Override
	public IResourceValidator newValidator() throws FHIRException {
		return validatorFactory.makeValidator(this, xverManager);
	}
}
