package it.finanze.sanita.fse2.gtwfhirmappingenginems.engine;

public abstract class AbstractEngineTest {
    public static final String ENGINE_FULL = "/package_completo.tgz";
    public static final String ENGINE_MINIMAL = "/package_minimo.tgz";

    public static final String RES_CDA_TO_FHIR_DATATYPE_JSON = "src/test/resources/engine/full-body/src/StructureMap-CdaToFhirDataTypes.json";
    public static final String RES_FULL_HEADER_JSON = "src/test/resources/engine/full-body/src/StructureMap-FULLHEADER.json";
    public static final String RES_FULL_BODY_MAP = "src/test/resources/engine/full-body/src/StructureMap_Referto_di_Laboratorio.map";
    public static final String RES_FULL_BODY_VERSIONED_MAP = "src/test/resources/engine/full-body/src/StructureMap_Referto_di_Laboratorio-Versioning.map";
    public static final String RES_CDA_TO_FHIR_DATATYPE_VERSIONED = "3.5.1";

    public static final String DEPS_R4_SUBSTANCE_ORIGINAL_JSON = "src/test/resources/engine/full-body/override/r4/StructureDefinition-Substance-Original.json";
    public static final String DEPS_R4_SUBSTANCE_MODIFIED_JSON = "src/test/resources/engine/full-body/override/r4/StructureDefinition-Substance-Modified.json";

    public static final String INPUT_LAB_XML = "src/test/resources/engine/full-body/LAB_SME_INPUT.xml";

}
