package com.restfulbooker.specs;

import com.restfulbooker.config.ConfigReader;
import com.restfulbooker.reporting.ReportingFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Builds the common request specification: base URI, JSON Content-Type and
 * Accept headers, timeouts and logging.
 *
 * <p>REST Assured's own logging is limited to the method and URI so tokens and
 * passwords are never printed; {@link ReportingFilter} adds a masked
 * request/response summary to the log and the HTML report.
 */
public final class RequestSpecFactory {

    private RequestSpecFactory() {
    }

    /** A new specification for every call, so tests never share mutable state. */
    public static RequestSpecification baseSpec() {
        RestAssuredConfig config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", ConfigReader.getConnectionTimeout())
                        .setParam("http.socket.timeout", ConfigReader.getResponseTimeout()));

        return new RequestSpecBuilder()
                .setBaseUri(ConfigReader.getBaseUrl())
                .setContentType(ContentType.JSON)
                // Plain string on purpose: ContentType.JSON would expand to "application/json, application/javascript, ..."
                // and Restful Booker answers that multi-value Accept header with 418 "I'm a Teapot".
                .setAccept("application/json")
                .setConfig(config)
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .addFilter(new ReportingFilter())
                .build();
    }
}
