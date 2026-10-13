package com.restfulbooker.base;

import com.restfulbooker.clients.AuthClient;
import com.restfulbooker.clients.BookingClient;
import com.restfulbooker.models.BookingResponse;
import com.restfulbooker.models.BookingRequest;
import com.restfulbooker.reporting.ExtentReportManager;
import com.restfulbooker.specs.ResponseSpecFactory;
import com.restfulbooker.utils.TokenManager;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

/**
 * Parent of every test class. It owns the API clients, starts and finishes the
 * Extent report entry for each test, and deletes any booking a test created
 * so tests stay independent of each other and leave no data behind.
 */
public abstract class BaseTest {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    protected final AuthClient authClient = new AuthClient();
    protected final BookingClient bookingClient = new BookingClient();

    /** Ids of bookings created by the current test; removed in {@link #afterMethod}. */
    private final List<Integer> createdBookingIds = new ArrayList<>();

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        ExtentReportManager.initReports();
        LOG.info("Test suite started");
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        Test annotation = method.getAnnotation(Test.class);
        String description = annotation == null ? "" : annotation.description();
        ExtentReportManager.startTest(method.getDeclaringClass().getSimpleName() + " - " + method.getName(), description);
        createdBookingIds.clear();
        LOG.info("Starting test: {}", method.getName());
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        deleteTrackedBookings();

        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            ExtentReportManager.info("Parameters: " + Arrays.toString(parameters));
        }
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                ExtentReportManager.pass("Test passed");
                break;
            case ITestResult.FAILURE:
                ExtentReportManager.fail(result.getThrowable());
                break;
            default:
                ExtentReportManager.skip(result.getThrowable());
                break;
        }
        LOG.info("Finished test: {}", result.getMethod().getMethodName());
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ExtentReportManager.flush();
        LOG.info("Test suite finished");
    }

    /** Creates a booking, checks it was accepted and registers it for automatic cleanup. */
    protected int createTrackedBooking(BookingRequest request) {
        Response response = bookingClient.createBooking(request);
        response.then().spec(ResponseSpecFactory.jsonResponse(200));
        Integer bookingId = response.as(BookingResponse.class).getBookingid();
        Assert.assertNotNull(bookingId, "Create booking response did not contain a bookingid");
        trackBooking(bookingId);
        return bookingId;
    }

    /** Registers a booking that a test created itself so it is deleted after the test. */
    protected void trackBooking(int bookingId) {
        createdBookingIds.add(bookingId);
    }

    /**
     * Asserts that the API refused the credentials. Restful Booker answers a
     * bad login with HTTP 200 and a {"reason":"Bad credentials"} body, so a
     * non-200 error status is accepted too.
     */
    protected void assertAuthRejected(Response response) {
        assertThat("A token must not be issued", response.asString(), not(containsString("\"token\"")));
        int status = response.getStatusCode();
        if (status == 200) {
            Assert.assertEquals(response.jsonPath().getString("reason"), "Bad credentials",
                    "HTTP 200 for a failed login must carry the documented reason");
        } else {
            assertThat("Unexpected status for rejected credentials", status,
                    allOf(greaterThanOrEqualTo(400), lessThan(600)));
        }
    }

    private void deleteTrackedBookings() {
        for (Integer bookingId : createdBookingIds) {
            try {
                // Bookings already deleted by the test itself simply return an error here, which is fine.
                int status = bookingClient.deleteBooking(bookingId, TokenManager.getToken()).getStatusCode();
                LOG.debug("Cleanup of booking {} returned HTTP {}", bookingId, status);
            } catch (RuntimeException e) {
                LOG.warn("Cleanup of booking {} failed: {}", bookingId, e.getMessage());
            }
        }
        createdBookingIds.clear();
    }
}
