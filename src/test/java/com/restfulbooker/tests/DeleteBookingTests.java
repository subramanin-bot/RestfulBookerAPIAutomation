package com.restfulbooker.tests;

import com.restfulbooker.base.BaseTest;
import com.restfulbooker.constants.FrameworkConstants;
import com.restfulbooker.specs.ResponseSpecFactory;
import com.restfulbooker.utils.TestDataFactory;
import com.restfulbooker.utils.TokenManager;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DeleteBookingTests extends BaseTest {

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "DELETE /booking/{id} with a token returns 201 'Created' and the booking is then gone (404)")
    public void shouldDeleteBookingAndReturnNotFoundAfterwards() {
        // The booking is created here for this test only, so no pre-existing id is needed.
        int bookingId = createTrackedBooking(TestDataFactory.validBooking());

        Response deleteResponse = bookingClient.deleteBooking(bookingId, TokenManager.getToken());

        // Restful Booker documents 201 (not 200/204) for a successful delete.
        deleteResponse.then().spec(ResponseSpecFactory.textResponse(201));
        assertThat(deleteResponse.asString(), equalTo("Created"));

        Response getResponse = bookingClient.getBooking(bookingId);
        getResponse.then().spec(ResponseSpecFactory.textResponse(404));
        assertThat(getResponse.asString(), equalTo("Not Found"));
    }
}
