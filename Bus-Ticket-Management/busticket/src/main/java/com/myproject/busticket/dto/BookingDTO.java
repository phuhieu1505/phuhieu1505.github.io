package com.myproject.busticket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private int bookingId;
    private CustomerDTO customer;
    private TripDTO trip;
    private byte numberOfSeat;
    private boolean isRoundTrip;
    private String roundTripId;
}
