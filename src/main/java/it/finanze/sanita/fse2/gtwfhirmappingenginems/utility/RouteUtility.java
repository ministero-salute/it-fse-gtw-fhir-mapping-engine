package it.finanze.sanita.fse2.gtwfhirmappingenginems.utility;

public final class RouteUtility {

    public static final String API_VERSION = "v1";
    public static final String API_ENGINE = "engine";
    public static final String API_DOCUMENTS = "documents";
    public static final String API_TRANSFORM = "transform";
    public static final String API_STATELESS = "stateless";

    public static final String API_FILE_VAR = "file";
    public static final String API_ENGINE_ID_VAR = "engineId";
    public static final String API_ENGINE_ID_PATH_VAR = "{" + API_ENGINE_ID_VAR + "}" ;
    public static final String API_OBJECT_ID_VAR = "objectId";
    public static final String API_OBJECT_ID_PATH_VAR = "{" + API_OBJECT_ID_VAR + "}" ;

    public static final String DOCUMENTS_MAPPER = "/" + API_VERSION + "/" + API_DOCUMENTS;

    public static final String ENGINE_MAPPER = "/" + API_VERSION + "/" + API_ENGINE;

    public static final String API_TRANSFORM_BY_OBJ = "/" + API_TRANSFORM;
    public static final String API_TRANSFORM_STATELESS_BY_OBJ = API_TRANSFORM_BY_OBJ + "/" + API_STATELESS + "/" + API_ENGINE_ID_PATH_VAR + "/" + API_OBJECT_ID_PATH_VAR;

    public static final String ENGINE_TAG = "Servizio gestione engines";
    public static final String ENGINE_STATUS_API = "/status";
    public static final String ENGINE_REFRESH_API = "/refresh";

}
