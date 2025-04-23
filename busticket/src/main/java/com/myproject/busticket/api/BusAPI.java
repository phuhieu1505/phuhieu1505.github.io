package com.myproject.busticket.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.enums.SeatReservationStatus;
import com.myproject.busticket.enums.SeatType;
import com.myproject.busticket.models.Bus;
import com.myproject.busticket.models.Bus_Seats;
import com.myproject.busticket.services.BusService;
import com.myproject.busticket.services.Bus_SeatsService;
import com.myproject.busticket.services.SeatReservationsService;
import com.myproject.busticket.services.TripService;

@RestController
@RequestMapping("/api/bus")
public class BusAPI {
    @Autowired
    private BusService busService;

    @Autowired
    private Bus_SeatsService bus_SeatsService;

    @Autowired
    private SeatReservationsService seatReservationService;

    @Autowired
    private TripService tripService;

    @GetMapping("/buses")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBuses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bus> busPages;

        if (searchValue != null && !searchValue.isEmpty()) {
            busPages = busService.searchBusesByPlateAndSeatType(pageable, searchValue);
        } else {
            busPages = busService.getAll(pageable);
        }

        List<Bus> buses = busPages.getContent().stream()
                .map(bus -> new Bus(bus.getPlate(), bus.getSeatType(), bus.getNumberOfSeat()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("buses", buses);
        response.put("currentPage", page);
        response.put("totalPages", busPages.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/new-bus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBus(@RequestBody Map<String, Object> busRequest) {
        Map<String, Object> response = new HashMap<>();
        String plate = (String) busRequest.get("plate");
        String seatTypeStr = (String) busRequest.get("seatType");
        String numberOfSeatStr = (String) busRequest.get("numberOfSeat");
        List<String> seatNames = (List<String>) busRequest.get("seatNames");
        int numberOfSeat;
        try {
            numberOfSeat = Integer.parseInt(numberOfSeatStr);
        } catch (NumberFormatException e) {
            response.put("message", "Invalid number of seats format.");
            return ResponseEntity.badRequest().body(response);
        }
        if (plate == null || plate.isEmpty() || seatTypeStr == null || seatTypeStr.isEmpty() || numberOfSeat <= 0
                || seatNames == null || seatNames.size() != numberOfSeat) {
            response.put("message", "Invalid input data.");
            return ResponseEntity.badRequest().body(response);
        }
        Bus existingBus = busService.getByBusPlate(plate);
        if (existingBus != null) {
            response.put("message", "Bus with this plate already exists.");
            return ResponseEntity.badRequest().body(response);
        }
        SeatType seatType;
        try {
            seatType = parseSeatType(seatTypeStr);
        } catch (IllegalArgumentException e) {
            response.put("message", "Invalid seat type.");
            return ResponseEntity.badRequest().body(response);
        }
        Bus newBus = new Bus();
        newBus.setPlate(plate);
        newBus.setSeatType(seatType);
        newBus.setNumberOfSeat(numberOfSeat);
        busService.save(newBus);

        for (String seatName : seatNames) {
            Bus_Seats seat = new Bus_Seats();
            seat.setBus(newBus);
            seat.setSeatName(seatName);
            bus_SeatsService.save(seat);
        }

        response.put("message", "Bus saved successfully.");
        return ResponseEntity.ok(response);
    }

    private SeatType parseSeatType(String seatType) {
        try {
            return SeatType.valueOf(seatType.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid seat type.");
        }
    }

    @GetMapping("/bus-detail/{plate}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBusDetails(@PathVariable String plate) {
        Bus bus = busService.getByBusPlate(plate);
        if (bus == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bus not found."));
        }

        List<Map<String, Object>> seatDetails = bus_SeatsService.getByBusPlate(bus).stream().map(seat -> {
            Map<String, Object> seatMap = new HashMap<>();
            seatMap.put("seatName", seat.getSeatName());
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

        Map<String, Object> response = Map.of(
                "busPlate", bus.getPlate(),
                "seatType", bus.getSeatType().name(),
                "numberOfSeats", bus.getNumberOfSeat(),
                "seats", seatDetails);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-bus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBus(@RequestBody Map<String, Object> busRequest) {
        Map<String, Object> response = new HashMap<>();
        String plate = (String) busRequest.get("plate");
        Bus existingBus = busService.getByBusPlate(plate);
        if (existingBus == null) {
            response.put("message", "Bus not found.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!tripService.findByBus(existingBus).isEmpty()) {
            response.put("message", "Bus has been assigned to a trip(s) and cannot be deleted.");
            return ResponseEntity.badRequest().body(response);
        }

        // proceed deleting if no trips are assigned
        List<Bus_Seats> seats = bus_SeatsService.getByBusPlate(existingBus);
        if (!seats.isEmpty()) {
            bus_SeatsService.deleteAll(seats);
        }

        // Delete bus
        busService.delete(existingBus);
        response.put("message", "Bus deleted successfully.");
        return ResponseEntity.ok(response);
    }
}
