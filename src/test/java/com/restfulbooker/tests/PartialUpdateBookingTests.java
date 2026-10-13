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

import java.util.LinkedHashMap;
import java.util.Map;

public class PartialUpdateBookingTests extends BaseTest {

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "PATCH /booking/{id} with only firstname changes that field and leaves the rest untouched")
    public void shouldUpdateOnlyFirstname() {
        BookingRequest original = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(original);

        String newFirstname = TestDataFactory.randomFirstName();
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("firstname", newFirstname);

        BookingRequest expected = original.copy();
        expected.setFirstname(newFirstname);

        Response response = bookingClient.partialUpdateBooking(bookingId, changes, TokenManager.getToken());

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        JsonSchemaValidatorUtil.validate(response, FrameworkConstants.BOOKING_SCHEMA);
        Assert.assertEquals(response.as(BookingRequest.class), expected, "PATCH response");
        Assert.assertEquals(bookingClient.getBooking(bookingId).as(BookingRequest.class), expected, "state after GET");
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "PATCH /booking/{id} with firstname and totalprice changes both and leaves the rest untouched")
    public void shouldUpdateFirstnameAndTotalprice() {
        BookingRequest original = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(original);

        String newFirstname = TestDataFactory.randomFirstName();
        int newPrice = original.getTotalprice() + 1;
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("firstname", newFirstname);
        changes.put("totalprice", newPrice);

        BookingRequest expected = original.copy();
        expected.setFirstname(newFirstname);
        expected.setTotalprice(newPrice);

        Response response = bookingClient.partialUpdateBooking(bookingId, changes, TokenManager.getToken());

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        Assert.assertEquals(response.as(BookingRequest.class), expected, "PATCH response");

        // Unchanged fields must keep their original values.
        BookingRequest afterGet = bookingClient.getBooking(bookingId).as(BookingRequest.class);
        Assert.assertEquals(afterGet.getLastname(), original.getLastname(), "lastname should be unchanged");
        Assert.assertEquals(afterGet.getDepositpaid(), original.getDepositpaid(), "depositpaid should be unchanged");
        Assert.assertEquals(afterGet.getBookingdates(), original.getBookingdates(), "bookingdates should be unchanged");
        Assert.assertEquals(afterGet.getAdditionalneeds(), original.getAdditionalneeds(), "additionalneeds should be unchanged");
        Assert.assertEquals(afterGet, expected, "state after GET");
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "PATCH /booking/{id} with only totalprice changes the price and nothing else")
    public void shouldUpdateOnlyTotalprice() {
        BookingRequest original = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(original);

        int newPrice = original.getTotalprice() + 250;
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("totalprice", newPrice);

        BookingRequest expected = original.copy();
        expected.setTotalprice(newPrice);

        Response response = bookingClient.partialUpdateBooking(bookingId, changes, TokenManager.getToken());

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        Assert.assertEquals(response.as(BookingRequest.class), expected, "PATCH response");
        Assert.assertEquals(bookingClient.getBooking(bookingId).as(BookingRequest.class), expected, "state after GET");
    }
}
