package com.myproject.busticket.dto;

import com.myproject.busticket.enums.TicketType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailDTO {
    private int id;
    private BillDTO bill;
    private TripDTO trip;
    private int numberOfTicket;
    private float fee;
    private TicketType ticketType;
}
