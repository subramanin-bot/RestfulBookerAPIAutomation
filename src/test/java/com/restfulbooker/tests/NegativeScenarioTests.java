package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.config.ConfigReader;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.models.AuthRequest;
import com.restfulbooker.models.BookingDates;
import com.restfulbooker.models.BookingRequest;
import com.restfulbooker.specs.RequestSpecFactory;
import com.restfulbooker.specs.ResponseSpecFactory;
import com.restfulbooker.utils.TestDataFactory;
import com.restfulbooker.utils.TokenManager;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

/**
 * Restful Booker is a public demo API with permissive validation, so these tests
 * check the behaviour it actually documents: 403 "Forbidden" without a valid
 * token, 404 "Not Found" for unknown bookings, 405 for write operations on
 * unknown ids, and an error status (400/500) for incomplete or malformed bodies.
 * Where the API may legitimately answer in more than one way, the assertion
 * accepts every documented outcome instead of one fixed code.
 */
public class NegativeScenarioTests extends BaseTest {

    // ---------- 1. Invalid authentication credentials ----------

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE, FrameworkConstants.GROUP_AUTH},
            description = "Invalid credentials: no token is issued (HTTP 200 with a 'reason' is the documented behaviour)")
    public void shouldRejectInvalidCredentials() {
        Response response = authClient.login(new AuthRequest(
                "invalid-" + TestDataFactory.randomLastName(), "invalid-" + TestDataFactory.randomLastName()));

        assertAuthRejected(response);
    }

    // ---------- 2. Missing authentication ----------

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE},
            description = "PATCH without any authentication is forbidden (403) and leaves the booking unchanged")
    public void shouldForbidPartialUpdateWithoutAuthentication() {
        BookingRequest original = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(original);
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("firstname", TestDataFactory.randomFirstName());

        Response response = bookingClient.partialUpdateBookingWithoutAuth(bookingId, changes);

        response.then().spec(ResponseSpecFactory.textResponse(403));
        assertThat(response.asString(), equalTo("Forbidden"));
        Assert.assertEquals(bookingClient.getBooking(bookingId).as(BookingRequest.class), original);
    }

    // ---------- 3. Invalid or nonexistent booking id ----------

    @DataProvider(name = "unknownBookingIds")
    public Object[][] unknownBookingIds() {
        return new Object[][]{{"999999999"}, {"0"}, {"-1"}, {"not-a-number"}};
    }

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE}, dataProvider = "unknownBookingIds",
            description = "GET with an unknown or malformed booking id returns 404 Not Found")
    public void shouldReturnNotFoundForUnknownBookingId(String bookingId) {
        Response response = bookingClient.getBooking(bookingId);

        response.then().spec(ResponseSpecFactory.textResponse(404));
        assertThat(response.asString(), equalTo("Not Found"));
    }

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE},
            description = "PUT, PATCH and DELETE on a nonexistent id with a valid token are refused (404 or 405)")
    public void shouldRefuseWriteOperationsOnNonexistentBooking() {
        String token = TokenManager.getToken();
        String missingId = "999999999";
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("firstname", TestDataFactory.randomFirstName());

        // Restful Booker answers 405 here; 404 would also be a reasonable contract, so both are accepted.
        assertThat(bookingClient.updateBooking(missingId, TestDataFactory.validBooking(), token).getStatusCode(),
                anyOf(equalTo(404), equalTo(405)));
        assertThat(bookingClient.partialUpdateBooking(missingId, changes, token).getStatusCode(),
                anyOf(equalTo(404), equalTo(405)));
        assertThat(bookingClient.deleteBooking(missingId, token).getStatusCode(),
                anyOf(equalTo(404), equalTo(405)));
    }

    // ---------- 4. Invalid request payload ----------

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE},
            description = "POST /booking with malformed JSON is rejected with an error status and creates nothing")
    public void shouldRejectMalformedJsonPayload() {
        Response response = bookingClient.createBooking("{\"firstname\": \"Broken\", \"lastname\": ");

        assertThat("status", response.getStatusCode(), allOf(greaterThanOrEqualTo(400), lessThan(600)));
        assertThat(response.asString(), not(containsString("bookingid")));
    }

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE},
            description = "POST /booking with an empty JSON object is rejected with an error status")
    public void shouldRejectEmptyPayload() {
        Response response = bookingClient.createBooking(new LinkedHashMap<String, Object>());

        assertThat("status", response.getStatusCode(), allOf(greaterThanOrEqualTo(400), lessThan(600)));
        assertThat(response.asString(), not(containsString("bookingid")));
    }

    // ---------- 5. Missing mandatory booking fields ----------

    @DataProvider(name = "mandatoryFields")
    public Object[][] mandatoryFields() {
        return new Object[][]{{"firstname"}, {"lastname"}, {"totalprice"}, {"depositpaid"}, {"bookingdates"}};
    }

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE}, dataProvider = "mandatoryFields",
            description = "POST /booking without one mandatory field is rejected with an error status")
    public void shouldRejectBookingMissingMandatoryField(String missingField) {
        Map<String, Object> payload = TestDataFactory.toMap(TestDataFactory.validBooking());
        payload.remove(missingField);

        Response response = bookingClient.createBooking(payload);

        assertThat("status when '" + missingField + "' is missing", response.getStatusCode(),
                allOf(greaterThanOrEqualTo(400), lessThan(600)));
        assertThat(response.asString(), not(containsString("bookingid")));
    }

    // ---------- 6. Update without authorization ----------

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE},
            description = "PUT without authentication is forbidden (403) and leaves the booking unchanged")
    public void shouldForbidFullUpdateWithoutAuthorization() {
        BookingRequest original = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(original);

        Response response = bookingClient.updateBookingWithoutAuth(bookingId, TestDataFactory.validBooking());

        response.then().spec(ResponseSpecFactory.textResponse(403));
        assertThat(response.asString(), equalTo("Forbidden"));
        Assert.assertEquals(bookingClient.getBooking(bookingId).as(BookingRequest.class), original);
    }

    // ---------- 7. Delete without authorization ----------

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE},
            description = "DELETE without authentication is forbidden (403) and the booking still exists")
    public void shouldForbidDeleteWithoutAuthorization() {
        BookingRequest original = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(original);

        Response response = bookingClient.deleteBookingWithoutAuth(bookingId);

        response.then().spec(ResponseSpecFactory.textResponse(403));
        assertThat(response.asString(), equalTo("Forbidden"));
        Response stillThere = bookingClient.getBooking(bookingId);
        stillThere.then().spec(ResponseSpecFactory.jsonResponse(200));
        Assert.assertEquals(stillThere.as(BookingRequest.class), original);
    }

    // ---------- 8. Invalid date values ----------

    @DataProvider(name = "invalidDates")
    public Object[][] invalidDates() {
        return new Object[][]{
                {"not-a-date", "also-not-a-date"},
                {"2025-13-45", "2025-14-99"},
                {"2030-06-10", "2030-06-01"}   // checkout before checkin
        };
    }

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE}, dataProvider = "invalidDates",
            description = "POST /booking with invalid dates is either rejected (4xx/5xx) or accepted without a server crash")
    public void shouldHandleInvalidDateValuesConsistently(String checkin, String checkout) {
        BookingRequest request = TestDataFactory.validBooking();
        request.setBookingdates(new BookingDates(checkin, checkout));

        Response response = bookingClient.createBooking(request);

        if (response.getStatusCode() == 200) {
            // The demo API is permissive and may store the values; make sure the booking is cleaned up
            // and the answer is still a well-formed create response.
            int bookingId = response.jsonPath().getInt("bookingid");
            trackBooking(bookingId);
            assertThat("bookingid", bookingId > 0);
            Assert.assertNotNull(response.jsonPath().get("booking"), "booking object");
        } else {
            assertThat("status", response.getStatusCode(), allOf(greaterThanOrEqualTo(400), lessThan(600)));
        }
    }

    // ---------- 9. Unsupported HTTP method or invalid endpoint ----------

    @DataProvider(name = "unsupportedRequests")
    public Object[][] unsupportedRequests() {
        return new Object[][]{
                {Method.GET, "/this-endpoint-does-not-exist"},
                {Method.PUT, FrameworkConstants.AUTH_ENDPOINT},
                {Method.DELETE, FrameworkConstants.BOOKING_ENDPOINT},
                {Method.PATCH, FrameworkConstants.BOOKING_ENDPOINT}
        };
    }

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE}, dataProvider = "unsupportedRequests",
            description = "Unsupported method / unknown endpoint is answered with 404 or 405")
    public void shouldRejectUnsupportedMethodOrInvalidEndpoint(Method method, String path) {
        Response response = given()
                .spec(RequestSpecFactory.baseSpec())
                .when()
                .request(method, path);

        assertThat("status for " + method + " " + path, response.getStatusCode(),
                anyOf(equalTo(404), equalTo(405)));
    }

    // ---------- Extra: credentials are required, not guessable ----------

    @Test(groups = {FrameworkConstants.GROUP_NEGATIVE, FrameworkConstants.GROUP_AUTH},
            description = "A valid username with an empty password does not issue a token")
    public void shouldRejectEmptyPassword() {
        Response response = authClient.login(new AuthRequest(ConfigReader.getUsername(), ""));

        assertAuthRejected(response);
    }
}
