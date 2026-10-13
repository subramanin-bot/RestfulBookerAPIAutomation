package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.models.BookingRequest;
import com.restfulbooker.models.BookingResponse;
import com.restfulbooker.specs.ResponseSpecFactory;
import com.restfulbooker.utils.JsonSchemaValidatorUtil;
import com.restfulbooker.utils.TestDataFactory;
import com.restfulbooker.utils.TokenManager;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One test walks a booking through its whole life. Keeping the steps in a single
 * method shares the booking id safely without relying on TestNG test ordering.
 */
public class CrudLifecycleTests extends BaseTest {

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "Create -> read -> full update -> partial update -> delete -> confirm 404")
    public void shouldCompleteFullBookingLifecycle() {
        String token = TokenManager.getToken();

        // 1. Create
        BookingRequest created = TestDataFactory.validBooking();
        Response createResponse = bookingClient.createBooking(created);
        createResponse.then().spec(ResponseSpecFactory.jsonResponse(200));
        JsonSchemaValidatorUtil.validate(createResponse, FrameworkConstants.CREATE_BOOKING_SCHEMA);
        Integer bookingId = createResponse.as(BookingResponse.class).getBookingid();
        Assert.assertNotNull(bookingId, "bookingid");
        trackBooking(bookingId);

        // 2. Read
        Response readResponse = bookingClient.getBooking(bookingId);
        readResponse.then().spec(ResponseSpecFactory.jsonResponse(200));
        Assert.assertEquals(readResponse.as(BookingRequest.class), created, "after create");

        // 3. Full update (PUT)
        BookingRequest replacement = TestDataFactory.validBooking();
        Response putResponse = bookingClient.updateBooking(bookingId, replacement, token);
        putResponse.then().spec(ResponseSpecFactory.jsonResponse(200));
        Assert.assertEquals(bookingClient.getBooking(bookingId).as(BookingRequest.class), replacement, "after PUT");

        // 4. Partial update (PATCH)
        String patchedName = TestDataFactory.randomFirstName();
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("firstname", patchedName);
        Response patchResponse = bookingClient.partialUpdateBooking(bookingId, changes, token);
        patchResponse.then().spec(ResponseSpecFactory.jsonResponse(200));
        BookingRequest expectedAfterPatch = replacement.copy();
        expectedAfterPatch.setFirstname(patchedName);
        Assert.assertEquals(bookingClient.getBooking(bookingId).as(BookingRequest.class), expectedAfterPatch, "after PATCH");

        // 5. Delete
        bookingClient.deleteBooking(bookingId, token).then().spec(ResponseSpecFactory.textResponse(201));

        // 6. Confirm it is gone
        bookingClient.getBooking(bookingId).then().spec(ResponseSpecFactory.textResponse(404));
    }
}
