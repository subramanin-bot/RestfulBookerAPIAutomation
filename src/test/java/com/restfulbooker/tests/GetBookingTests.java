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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * These tests only read, so they share one booking that is created before the
 * class runs and deleted afterwards. Its id is generated at run time.
 */
public class GetBookingTests extends BaseTest {

    private BookingRequest sharedBooking;
    private int sharedBookingId;

    @BeforeClass(alwaysRun = true)
    public void createSharedBooking() {
        sharedBooking = TestDataFactory.validBooking();
        var response = bookingClient.createBooking(sharedBooking);
        response.then().statusCode(200);
        sharedBookingId = response.jsonPath().getInt("bookingid");
    }

    @AfterClass(alwaysRun = true)
    public void deleteSharedBooking() {
        if (sharedBookingId > 0) {
            bookingClient.deleteBooking(sharedBookingId, TokenManager.getToken());
        }
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "GET /booking/{id} returns 200, JSON, schema-valid body with every created field")
    public void shouldRetrieveExistingBooking() {
        Response response = bookingClient.getBooking(sharedBookingId);

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        JsonSchemaValidatorUtil.validate(response, FrameworkConstants.BOOKING_SCHEMA);

        BookingRequest actual = response.as(BookingRequest.class);
        Assert.assertEquals(actual.getFirstname(), sharedBooking.getFirstname(), "firstname");
        Assert.assertEquals(actual.getLastname(), sharedBooking.getLastname(), "lastname");
        Assert.assertEquals(actual.getTotalprice(), sharedBooking.getTotalprice(), "totalprice");
        Assert.assertEquals(actual.getDepositpaid(), sharedBooking.getDepositpaid(), "depositpaid");
        Assert.assertEquals(actual.getBookingdates(), sharedBooking.getBookingdates(), "bookingdates");
        Assert.assertEquals(actual.getAdditionalneeds(), sharedBooking.getAdditionalneeds(), "additionalneeds");
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "The id used in the path is the id listed by the name filter for the same booking")
    public void shouldMatchRetrievedBookingIdWithCreatedId() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("firstname", sharedBooking.getFirstname());
        filters.put("lastname", sharedBooking.getLastname());

        Response listResponse = bookingClient.getBookingIds(filters);

        listResponse.then().spec(ResponseSpecFactory.jsonResponse(200));
        listResponse.then().body("bookingid", hasItem(sharedBookingId));

        // The single-booking response has no id field, so the path id plus the matching content proves it is the same booking.
        BookingRequest byId = bookingClient.getBooking(sharedBookingId).as(BookingRequest.class);
        Assert.assertEquals(byId, sharedBooking);
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "GET /booking?firstname=&lastname= (query parameters) finds the created booking")
    public void shouldFilterBookingsByFirstnameAndLastname() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("firstname", sharedBooking.getFirstname());
        filters.put("lastname", sharedBooking.getLastname());

        Response response = bookingClient.getBookingIds(filters);

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        response.then().body("bookingid", hasItem(sharedBookingId));
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "GET /booking?checkin=&checkout= (date query parameters) finds the created booking")
    public void shouldFilterBookingsByDates() {
        LocalDate checkin = LocalDate.parse(sharedBooking.getBookingdates().getCheckin());
        LocalDate checkout = LocalDate.parse(sharedBooking.getBookingdates().getCheckout());

        // checkin is "on or after" and checkout is "on or before" per the API docs; the margins keep the check unambiguous.
        // The name filters narrow the result to our own booking on the shared public API.
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("firstname", sharedBooking.getFirstname());
        filters.put("lastname", sharedBooking.getLastname());
        filters.put("checkin", checkin.minusDays(1).toString());
        filters.put("checkout", checkout.plusDays(1).toString());

        Response response = bookingClient.getBookingIds(filters);

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        response.then().body("bookingid", hasItem(sharedBookingId));
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "GET /booking with a name that does not exist returns an empty list")
    public void shouldReturnEmptyListForUnknownName() {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("lastname", "NoSuchLastName" + TestDataFactory.randomLastName());

        Response response = bookingClient.getBookingIds(filters);

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        response.then().body("$", hasSize(0));
    }

    @Test(groups = {FrameworkConstants.GROUP_CRUD, FrameworkConstants.GROUP_POSITIVE},
            description = "GET /booking without filters returns a non-empty list of objects with a positive bookingid")
    public void shouldListAllBookingIds() {
        Response response = bookingClient.getBookingIds(new LinkedHashMap<>());

        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        response.then().body("$", not(empty()));
        assertThat("first entry has a positive id", response.jsonPath().getInt("[0].bookingid") > 0);
    }
}
