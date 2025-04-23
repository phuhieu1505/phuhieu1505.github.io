package com.myproject.busticket.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.myproject.busticket.services.BusService;
import com.myproject.busticket.services.Bus_SeatsService;
import com.myproject.busticket.services.SeatReservationsService;
import com.myproject.busticket.enums.SeatReservationStatus;
import com.myproject.busticket.models.Bus;

@Controller
public class BusController {
    @Autowired
    private BusService busService;

    @Autowired
    private Bus_SeatsService bus_SeatsService;

    @Autowired
    private SeatReservationsService seatReservationService;

    @GetMapping("/easy-bus/new-bus")
    public String newBus(Model model) {
        model.addAttribute("bus", new Bus());
        return "new-bus";
    }

    @PostMapping("/easy-bus/new-bus")
    public String saveBus(Bus bus) {
        busService.save(bus);
        return "redirect:/easy-bus/bus-management";
    }

    @GetMapping("/easy-bus/bus-detail/{plate}")
    public String getBusDetails(@PathVariable String plate, Model model) {
        Bus bus = busService.getByBusPlate(plate);
        if (bus == null) {
            model.addAttribute("errorMessage", "Bus not found.");
            return "redirect:/easy-bus/bus-management";
        }

        List<Map<String, Object>> seatDetails = bus_SeatsService.getByBusPlate(bus).stream().map(seat -> {
            Map<String, Object> seatMap = new HashMap<>();
            seatMap.put("seatName", seat.getSeatName());
            seatMap.put("seatId", seat.getId());
            List<Map<String, Object>> reservationList = seatReservationService.getBySeat(seat).stream()
                    .map(reservation -> {
                        Map<String, Object> reservationMap = new HashMap<>();
                        reservationMap.put("customerName",
                                reservation.getBooking() != null ? reservation.getBooking().getCustomer().getName()
                                        : "");
                        reservationMap.put("customerEmail",
                                reservation.getBooking() != null ? reservation.getBooking().getCustomer().getEmail()
                                        : "");
                        reservationMap.put("trip", reservation.getTrip().getTripId());
                        reservationMap.put("status", reservation.getStatus().name());
                        return reservationMap;
                    }).collect(Collectors.toList());
            seatMap.put("reservations", reservationList);
            seatMap.put("status",
                    reservationList.stream().anyMatch(r -> r.get("status").equals(SeatReservationStatus.booked.name()))
                            ? "booked"
                            : "available");
            return seatMap;
        }).collect(Collectors.toList());

        model.addAttribute("bus", bus);
        model.addAttribute("seatDetails", seatDetails);

        return "bus-detail";
    }
}
