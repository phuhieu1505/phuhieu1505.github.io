package com.myproject.busticket.dto;

public class CustomerBookingDTO {
    private String email;
    private long numberOfBookings;

    public CustomerBookingDTO(String email, long numberOfBookings) {
        this.email = email;
        this.numberOfBookings = numberOfBookings;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getNumberOfBookings() {
        return numberOfBookings;
    }

    public void setNumberOfBookings(long numberOfBookings) {
        this.numberOfBookings = numberOfBookings;
    }
}