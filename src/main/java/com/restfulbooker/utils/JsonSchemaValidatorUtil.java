package com.restfulbooker.utils;

import com.restfulbooker.constants.FrameworkConstants;
import io.restassured.response.Response;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/** Validates a response body against one of the JSON schemas in src/main/resources/schemas. */
public final class JsonSchemaValidatorUtil {

    private JsonSchemaValidatorUtil() {
    }

    /** Throws an AssertionError that names the violated schema rule when the body does not match. */
    public static void validate(Response response, String schemaFileName) {
        response.then().assertThat()
                .body(matchesJsonSchemaInClasspath(FrameworkConstants.SCHEMA_FOLDER + schemaFileName));
    }
}
