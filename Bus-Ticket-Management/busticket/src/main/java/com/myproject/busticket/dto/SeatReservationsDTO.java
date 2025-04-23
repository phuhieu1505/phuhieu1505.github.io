package com.myproject.busticket.dto;

import com.myproject.busticket.enums.SeatReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservationsDTO {
    private int id;
    private BusSeatsDTO seat;
    private BookingDTO booking;
    private TripDTO trip;
    private String status;
}
