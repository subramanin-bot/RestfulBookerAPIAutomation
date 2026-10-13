package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * A booking payload. It is used as the request body for POST and PUT, and also
 * as the model for the booking returned by GET, PUT and PATCH.
 *
 * <p>Wrapper types are used so a field can be left {@code null}; null fields
 * are omitted from the JSON, which lets tests send incomplete payloads.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingRequest {

    private String firstname;
    private String lastname;
    private Integer totalprice;
    private Boolean depositpaid;
    private BookingDates bookingdates;
    private String additionalneeds;

    public BookingRequest() {
    }

    public BookingRequest(String firstname, String lastname, Integer totalprice, Boolean depositpaid,
                          BookingDates bookingdates, String additionalneeds) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.totalprice = totalprice;
        this.depositpaid = depositpaid;
        this.bookingdates = bookingdates;
        this.additionalneeds = additionalneeds;
    }

    /** Returns an independent copy, handy for building the expected state after a partial update. */
    public BookingRequest copy() {
        BookingDates datesCopy = bookingdates == null
                ? null
                : new BookingDates(bookingdates.getCheckin(), bookingdates.getCheckout());
        return new BookingRequest(firstname, lastname, totalprice, depositpaid, datesCopy, additionalneeds);
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getTotalprice() {
        return totalprice;
    }

    public void setTotalprice(Integer totalprice) {
        this.totalprice = totalprice;
    }

    public Boolean getDepositpaid() {
        return depositpaid;
    }

    public void setDepositpaid(Boolean depositpaid) {
        this.depositpaid = depositpaid;
    }

    public BookingDates getBookingdates() {
        return bookingdates;
    }

    public void setBookingdates(BookingDates bookingdates) {
        this.bookingdates = bookingdates;
    }

    public String getAdditionalneeds() {
        return additionalneeds;
    }

    public void setAdditionalneeds(String additionalneeds) {
        this.additionalneeds = additionalneeds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookingRequest)) {
            return false;
        }
        BookingRequest that = (BookingRequest) o;
        return Objects.equals(firstname, that.firstname)
                && Objects.equals(lastname, that.lastname)
                && Objects.equals(totalprice, that.totalprice)
                && Objects.equals(depositpaid, that.depositpaid)
                && Objects.equals(bookingdates, that.bookingdates)
                && Objects.equals(additionalneeds, that.additionalneeds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }

    @Override
    public String toString() {
        return "BookingRequest{firstname='" + firstname + "', lastname='" + lastname
                + "', totalprice=" + totalprice + ", depositpaid=" + depositpaid
                + ", bookingdates=" + bookingdates + ", additionalneeds='" + additionalneeds + "'}";
    }
}
