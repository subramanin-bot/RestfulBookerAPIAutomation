package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.config.ConfigReader;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.models.AuthRequest;
import com.restfulbooker.models.AuthResponse;
import com.restfulbooker.models.BookingRequest;
import com.restfulbooker.specs.ResponseSpecFactory;
import com.restfulbooker.utils.JsonSchemaValidatorUtil;
import com.restfulbooker.utils.TestDataFactory;
import com.restfulbooker.utils.TokenManager;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class AuthTests extends BaseTest {

    @Test(groups = {FrameworkConstants.GROUP_AUTH, FrameworkConstants.GROUP_POSITIVE},
            description = "POST /auth with valid credentials returns 200, JSON, a non-empty token and matches the schema")
    public void shouldGenerateTokenWithValidCredentials() {
        Response response = authClient.login(ConfigReader.getUsername(), ConfigReader.getPassword());

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        JsonSchemaValidatorUtil.validate(response, FrameworkConstants.AUTH_SCHEMA);

        AuthResponse body = response.as(AuthResponse.class);
        assertThat("token", body.getToken(), not(emptyOrNullString()));
    }

    @Test(groups = {FrameworkConstants.GROUP_AUTH, FrameworkConstants.GROUP_NEGATIVE},
            description = "POST /auth with a wrong password does not issue a token")
    public void shouldRejectInvalidPassword() {
        Response response = authClient.login(
                new AuthRequest(ConfigReader.getUsername(), "wrong-" + TestDataFactory.randomLastName()));

        assertAuthRejected(response);
    }

    @Test(groups = {FrameworkConstants.GROUP_AUTH, FrameworkConstants.GROUP_NEGATIVE},
            description = "POST /auth with an unknown username does not issue a token")
    public void shouldRejectInvalidUsername() {
        Response response = authClient.login(
                new AuthRequest("nobody-" + TestDataFactory.randomLastName(), ConfigReader.getPassword()));

        assertAuthRejected(response);
    }

    @Test(groups = {FrameworkConstants.GROUP_AUTH, FrameworkConstants.GROUP_NEGATIVE},
            description = "POST /auth with an empty body does not issue a token")
    public void shouldRejectMissingCredentials() {
        Response response = authClient.login(new AuthRequest());

        assertAuthRejected(response);
    }

    @Test(groups = {FrameworkConstants.GROUP_AUTH, FrameworkConstants.GROUP_NEGATIVE},
            description = "A protected operation (PUT) with an invalid token is forbidden and changes nothing")
    public void shouldForbidProtectedOperationWithInvalidToken() {
        BookingRequest original = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(original);

        Response response = bookingClient.updateBooking(bookingId, TestDataFactory.validBooking(), "invalid-token-value");

        response.then().spec(ResponseSpecFactory.textResponse(403));
        assertThat(response.asString(), equalTo("Forbidden"));
        Assert.assertEquals(bookingClient.getBooking(bookingId).as(BookingRequest.class), original,
                "The booking must be unchanged after a forbidden update");
    }

    @Test(groups = {FrameworkConstants.GROUP_AUTH, FrameworkConstants.GROUP_POSITIVE},
            description = "The token cached by TokenManager is accepted by a protected operation")
    public void shouldAcceptCachedTokenOnProtectedOperation() {
        int bookingId = createTrackedBooking(TestDataFactory.validBooking());

        Response response = bookingClient.updateBooking(bookingId, TestDataFactory.validBooking(), TokenManager.getToken());

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
    }
}
