package com.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response body of POST /booking: the generated id and the stored booking. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingResponse {

    private Integer bookingid;
    private BookingRequest booking;

    public BookingResponse() {
    }

    public Integer getBookingid() {
        return bookingid;
    }

    public void setBookingid(Integer bookingid) {
        this.bookingid = bookingid;
    }

    public BookingRequest getBooking() {
        return booking;
    }

    public void setBooking(BookingRequest booking) {
        this.booking = booking;
    }

    @Override
    public String toString() {
        return "BookingResponse{bookingid=" + bookingid + ", booking=" + booking + "}";
    }
}
