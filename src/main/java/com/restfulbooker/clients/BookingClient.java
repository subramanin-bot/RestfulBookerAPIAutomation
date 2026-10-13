package com.restfulbooker.clients;

import com.restfulbooker.config.ConfigReader;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.specs.RequestSpecFactory;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Client for the /booking endpoints. Every method returns the raw {@link Response}
 * so the tests decide what to assert. Booking ids are accepted as {@code Object}
 * so negative tests can also pass values such as "abc".
 *
 * <p>Restful Booker accepts the token as a {@code Cookie: token=...} header.
 */
public class BookingClient {

    private static RequestSpecification spec() {
        return given().spec(RequestSpecFactory.baseSpec());
    }

    private static RequestSpecification authorised(String token) {
        return spec().header("Cookie", "token=" + token);
    }

    // POST /booking
    /** The body can be a POJO, a Map or a raw String, which lets negative tests send broken payloads. */
    public Response createBooking(Object body) {
        return spec()
                .body(body)
                .when()
                .post(FrameworkConstants.BOOKING_ENDPOINT);
    }

    // GET /booking (optionally filtered by firstname, lastname, checkin, checkout)
    public Response getBookingIds(Map<String, ?> filters) {
        return spec()
                .queryParams(filters)
                .when()
                .get(FrameworkConstants.BOOKING_ENDPOINT);
    }

    // GET /booking/{bookingid}
    public Response getBooking(Object bookingId) {
        return spec()
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .when()
                .get(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    // PUT /booking/{bookingid}
    public Response updateBooking(Object bookingId, Object body, String token) {
        return authorised(token)
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .body(body)
                .when()
                .put(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    /** PUT using HTTP Basic authentication, the other authentication style documented by the API. */
    public Response updateBookingWithBasicAuth(Object bookingId, Object body) {
        return spec()
                .header("Authorization", basicAuthHeader())
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .body(body)
                .when()
                .put(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    public Response updateBookingWithoutAuth(Object bookingId, Object body) {
        return spec()
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .body(body)
                .when()
                .put(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    // PATCH /booking/{bookingid}
    public Response partialUpdateBooking(Object bookingId, Map<String, Object> changes, String token) {
        return authorised(token)
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .body(changes)
                .when()
                .patch(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    public Response partialUpdateBookingWithoutAuth(Object bookingId, Map<String, Object> changes) {
        return spec()
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .body(changes)
                .when()
                .patch(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    // DELETE /booking/{bookingid}
    public Response deleteBooking(Object bookingId, String token) {
        return authorised(token)
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .when()
                .delete(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    public Response deleteBookingWithoutAuth(Object bookingId) {
        return spec()
                .pathParam(FrameworkConstants.BOOKING_ID_PATH_PARAM, bookingId)
                .when()
                .delete(FrameworkConstants.BOOKING_BY_ID_ENDPOINT);
    }

    private static String basicAuthHeader() {
        String credentials = ConfigReader.getUsername() + ":" + ConfigReader.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
