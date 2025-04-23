package com.myproject.busticket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoDTO {
    private int tripId;
    private byte numberOfSeat;
    private List<String> seatNumbers;
    private double price;
}
