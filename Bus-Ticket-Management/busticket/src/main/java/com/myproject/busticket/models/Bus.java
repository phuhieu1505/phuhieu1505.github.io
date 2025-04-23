package com.myproject.busticket.models;

import com.myproject.busticket.enums.SeatType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bus")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bus {
    @Id
    @Column(name = "bus_id", nullable = false, length = 12)
    private String plate;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, columnDefinition = "ENUM('economy', 'limousine') DEFAULT 'economy'")
    private SeatType seatType;

    @Column(name = "number_of_seat")
    private int numberOfSeat;

    @Override
    public String toString() {
        return "Bus{" +
                "plate='" + plate + '\'' +
                ", type=" + seatType +
                ", numberOfSeat=" + numberOfSeat +
                '}';
    }
}
