package com.myproject.busticket.dto;

import com.myproject.busticket.models.Bus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusSeatsDTO {
    private int id;
    private Bus bus;
    private String seatName;
}
