package com.myproject.busticket.dto;

import com.myproject.busticket.enums.TripStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripDTO {
    private int tripId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private float price;
    private TripStatus status;

    private BusDTO bus;
    private DriverDTO driver;
    private ControllerDTO controller;
    private StaffDTO staff;
    private RouteDTO route;
}
