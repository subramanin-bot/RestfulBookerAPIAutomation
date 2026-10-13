package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.models.BookingRequest;
import com.restfulbooker.specs.ResponseSpecFactory;
import com.restfulbooker.utils.JsonSchemaValidatorUtil;
import com.restfulbooker.utils.TestDataFactory;
import com.restfulbooker.utils.TokenManager;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateBookingTests extends BaseTest {

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "PUT /booking/{id} with a token replaces every field and a later GET confirms it")
    public void shouldUpdateAllFieldsUsingToken() {
        int bookingId = createTrackedBooking(TestDataFactory.validBooking());
        BookingRequest replacement = TestDataFactory.validBooking();

        Response response = bookingClient.updateBooking(bookingId, replacement, TokenManager.getToken());

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        JsonSchemaValidatorUtil.validate(response, FrameworkConstants.BOOKING_SCHEMA);
        assertAllFields(response.as(BookingRequest.class), replacement);

        BookingRequest afterGet = bookingClient.getBooking(bookingId).as(BookingRequest.class);
        assertAllFields(afterGet, replacement);
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "PUT /booking/{id} also works with HTTP Basic authentication")
    public void shouldUpdateAllFieldsUsingBasicAuth() {
        int bookingId = createTrackedBooking(TestDataFactory.validBooking());
        BookingRequest replacement = TestDataFactory.validBooking();

        Response response = bookingClient.updateBookingWithBasicAuth(bookingId, replacement);

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        assertAllFields(response.as(BookingRequest.class), replacement);
        assertAllFields(bookingClient.getBooking(bookingId).as(BookingRequest.class), replacement);
    }

    private void assertAllFields(BookingRequest actual, BookingRequest expected) {
        Assert.assertEquals(actual.getFirstname(), expected.getFirstname(), "firstname");
        Assert.assertEquals(actual.getLastname(), expected.getLastname(), "lastname");
        Assert.assertEquals(actual.getTotalprice(), expected.getTotalprice(), "totalprice");
        Assert.assertEquals(actual.getDepositpaid(), expected.getDepositpaid(), "depositpaid");
        Assert.assertEquals(actual.getBookingdates(), expected.getBookingdates(), "bookingdates");
        Assert.assertEquals(actual.getAdditionalneeds(), expected.getAdditionalneeds(), "additionalneeds");
    }
}
