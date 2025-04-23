package com.myproject.busticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingInfoDTO {
    @JsonProperty("cusDetail")
    private CustomerTicketDTO customer;

    @JsonProperty("tripDetail")
    private TicketInfoDTO ticketInfoDTO;

    @JsonProperty("paymentDate")
    private String paymentDate;
}
