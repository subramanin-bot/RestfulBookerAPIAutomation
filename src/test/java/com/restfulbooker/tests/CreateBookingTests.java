package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.models.BookingRequest;
import com.restfulbooker.models.BookingResponse;
import com.restfulbooker.specs.ResponseSpecFactory;
import com.restfulbooker.utils.JsonSchemaValidatorUtil;
import com.restfulbooker.utils.TestDataFactory;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class CreateBookingTests extends BaseTest {

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "POST /booking with valid data returns 200, JSON, a positive id, the schema-valid body and echoed fields")
    public void shouldCreateBookingWithValidData() {
        BookingRequest request = TestDataFactory.validBooking();

        Response response = bookingClient.createBooking(request);

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        JsonSchemaValidatorUtil.validate(response, FrameworkConstants.CREATE_BOOKING_SCHEMA);

        BookingResponse body = response.as(BookingResponse.class);
        Assert.assertNotNull(body.getBookingid(), "bookingid");
        trackBooking(body.getBookingid());
        assertThat("bookingid", body.getBookingid(), greaterThan(0));

        BookingRequest created = body.getBooking();
        Assert.assertNotNull(created, "booking");
        Assert.assertEquals(created.getFirstname(), request.getFirstname(), "firstname");
        Assert.assertEquals(created.getLastname(), request.getLastname(), "lastname");
        Assert.assertEquals(created.getTotalprice(), request.getTotalprice(), "totalprice");
        Assert.assertEquals(created.getDepositpaid(), request.getDepositpaid(), "depositpaid");
        Assert.assertEquals(created.getBookingdates(), request.getBookingdates(), "bookingdates");
        Assert.assertEquals(created.getAdditionalneeds(), request.getAdditionalneeds(), "additionalneeds");
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "Two bookings created one after the other receive different ids")
    public void shouldGenerateDifferentIdsForSeparateBookings() {
        int firstId = createTrackedBooking(TestDataFactory.validBooking());
        int secondId = createTrackedBooking(TestDataFactory.validBooking());

        Assert.assertNotEquals(firstId, secondId, "Each booking must get its own id");
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "A booking created with POST can be read back using the generated id")
    public void shouldReadBackBookingUsingGeneratedId() {
        BookingRequest request = TestDataFactory.validBooking();
        int bookingId = createTrackedBooking(request);

        BookingRequest stored = bookingClient.getBooking(bookingId).as(BookingRequest.class);

        Assert.assertEquals(stored, request, "Stored booking should equal what was sent");
    }
}
