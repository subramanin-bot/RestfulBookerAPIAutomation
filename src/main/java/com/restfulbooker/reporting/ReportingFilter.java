package com.restfulbooker.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Assured filter that writes one request/response summary per call to the
 * framework log and to the current Extent test.
 *
 * <p>Headers (which carry the auth token) are never logged, and passwords and
 * tokens inside JSON bodies are masked.
 */
public class ReportingFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ReportingFilter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_BODY_LENGTH = 2000;

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        Response response = ctx.next(requestSpec, responseSpec);

        String requestLine = requestSpec.getMethod() + " " + requestSpec.getURI();
        String requestBody = describeRequestBody(requestSpec.getBody());
        String responseBody = truncate(mask(response.getBody().asString()));
        String statusLine = "HTTP " + response.getStatusCode() + " in " + response.getTime() + " ms";

        LOG.info("{} -> {}", requestLine, statusLine);
        if (!requestBody.isEmpty()) {
            LOG.debug("Request body: {}", requestBody);
        }
        LOG.debug("Response body: {}", responseBody);

        ExtentReportManager.info("<b>" + requestLine + "</b> &rarr; " + statusLine);
        if (!requestBody.isEmpty()) {
            ExtentReportManager.infoBlock("Request body", requestBody);
        }
        ExtentReportManager.infoBlock("Response body", responseBody.isEmpty() ? "(empty)" : responseBody);
        return response;
    }

    private static String describeRequestBody(Object body) {
        if (body == null) {
            return "";
        }
        try {
            String text = body instanceof String ? (String) body : MAPPER.writeValueAsString(body);
            return truncate(mask(text));
        } catch (Exception e) {
            return "(request body could not be rendered)";
        }
    }

    /** Hides the values of "password" and "token" JSON fields. */
    static String mask(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("(\"(?:password|token)\"\\s*:\\s*\")[^\"]*(\")", "$1****$2");
    }

    private static String truncate(String text) {
        return text.length() <= MAX_BODY_LENGTH ? text : text.substring(0, MAX_BODY_LENGTH) + "... (truncated)";
    }
}
