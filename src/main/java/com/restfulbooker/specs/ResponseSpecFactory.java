package com.restfulbooker.specs;

import com.restfulbooker.config.ConfigReader;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.lessThan;

/**
 * Reusable response expectations. Each method takes the expected status code,
 * because Restful Booker uses different codes per endpoint (for example 201 for DELETE).
 */
public final class ResponseSpecFactory {

    private ResponseSpecFactory() {
    }

    /** Expects the status code, a JSON content type and a response time under the configured threshold. */
    public static ResponseSpecification jsonResponse(int expectedStatus) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatus)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan(ConfigReader.getResponseTimeThreshold()), TimeUnit.MILLISECONDS)
                .log(LogDetail.STATUS)
                .build();
    }

    /** Expects only the status code and the response time; for plain-text bodies such as "Created" or "Forbidden". */
    public static ResponseSpecification textResponse(int expectedStatus) {
        return new ResponseSpecBuilder()
                .expectStatusCode(expectedStatus)
                .expectResponseTime(lessThan(ConfigReader.getResponseTimeThreshold()), TimeUnit.MILLISECONDS)
                .log(LogDetail.STATUS)
                .build();
    }
}
