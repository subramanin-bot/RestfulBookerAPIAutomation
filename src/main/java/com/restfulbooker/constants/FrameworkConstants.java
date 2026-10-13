package com.restfulbooker.constants;

/**
 * Constants shared across the framework: endpoints, schema names, TestNG group
 * names and report locations.
 */
public final class FrameworkConstants {

    private FrameworkConstants() {
    }

    // Endpoints
    public static final String AUTH_ENDPOINT = "/auth";
    public static final String BOOKING_ENDPOINT = "/booking";
    public static final String BOOKING_BY_ID_ENDPOINT = "/booking/{bookingid}";
    public static final String BOOKING_ID_PATH_PARAM = "bookingid";

    // JSON schema files (relative to src/main/resources)
    public static final String SCHEMA_FOLDER = "schemas/";
    public static final String AUTH_SCHEMA = "auth-response-schema.json";
    public static final String BOOKING_SCHEMA = "booking-response-schema.json";
    public static final String CREATE_BOOKING_SCHEMA = "booking-id-response-schema.json";

    // TestNG groups (must be compile-time constants to be used in annotations)
    public static final String GROUP_AUTH = "auth";
    public static final String GROUP_POSITIVE = "positive";
    public static final String GROUP_NEGATIVE = "negative";
    public static final String GROUP_CRUD = "crud";

    // Reporting
    public static final String REPORT_DIRECTORY = "reports";
    public static final String REPORT_FILE = REPORT_DIRECTORY + "/ExtentReport.html";
    public static final String SUMMARY_FILE = REPORT_DIRECTORY + "/test-execution-summary.txt";
}
