package com.myproject.busticket.dto;

import com.myproject.busticket.enums.SeatType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusDTO {
    private String plate;
    private int numberOfSeat;
    private SeatType seatType;
}
