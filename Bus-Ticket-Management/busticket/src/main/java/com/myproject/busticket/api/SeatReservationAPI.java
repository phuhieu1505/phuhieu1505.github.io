package com.myproject.busticket.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.enums.SeatReservationStatus;
import com.myproject.busticket.models.Bus_Seats;
import com.myproject.busticket.services.Bus_SeatsService;
import com.myproject.busticket.services.SeatReservationsService;

@RestController
@RequestMapping("/api/seat-reservation")
public class SeatReservationAPI {
    @Autowired
    private Bus_SeatsService bus_SeatsService;

    @Autowired
    private SeatReservationsService seatReservationService;

    @GetMapping("/{seatId}/reservations")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSeatReservations(@PathVariable Integer seatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Bus_Seats seat = bus_SeatsService.getById(seatId);

        List<Map<String, Object>> reservationList = seatReservationService.getBySeat(seat).stream()
                .filter(reservation -> reservation.getStatus() != SeatReservationStatus.open)
                .map(reservation -> {
                    Map<String, Object> reservationMap = new HashMap<>();
                    reservationMap.put("customerName",
                            reservation.getBooking() != null
                                    ? reservation.getBooking().getCustomer()
                                            .getName()
                                    : "");
                    reservationMap.put("customerEmail",
                            reservation.getBooking() != null
                                    ? reservation.getBooking().getCustomer()
                                            .getEmail()
                                    : "");
                    reservationMap.put("trip", reservation.getTrip().getTripId());
                    reservationMap.put("status", reservation.getStatus().name());
                    return reservationMap;
                }).collect(Collectors.toList());

        int start = Math.min(page * size, reservationList.size());
        int end = Math.min((page + 1) * size, reservationList.size());
        List<Map<String, Object>> paginatedList = reservationList.subList(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", paginatedList);
        response.put("totalPages", (int) Math.ceil((double) reservationList.size() / size));
        response.put("currentPage", page);

        return ResponseEntity.ok(response);
    }
}
