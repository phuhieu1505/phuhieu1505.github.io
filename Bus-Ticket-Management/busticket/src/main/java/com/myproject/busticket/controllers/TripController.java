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

import com.myproject.busticket.dto.TripDTO;
import com.myproject.busticket.models.Bus;
import com.myproject.busticket.models.Bus_Seats;
import com.myproject.busticket.models.SeatReservations;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.services.BusService;
import com.myproject.busticket.services.Bus_SeatsService;
import com.myproject.busticket.services.SeatReservationsService;
import com.myproject.busticket.services.TripService;

@Controller
public class TripController {
    @Autowired
    private TripService tripService;

    @Autowired
    private Bus_SeatsService bus_SeatsService;

    @Autowired
    private BusService busService;

    @Autowired
    private SeatReservationsService seatReservationService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/api/trip/{tripId}")
    public TripDTO getById(@PathVariable int tripId) {
        TripDTO trip = tripService.findById(tripId);
        return trip;
    }

    @GetMapping("/api/trips")
    public List<Trip> getAllTrips() {
        return tripService.findAll();
    }

    @GetMapping("/easy-bus/trip-detail/{tripId}")
    public String getTripDetails(@PathVariable int tripId, Model model) {
        Trip trip = tripService.findTripById(tripId);
        if (trip == null) {
            model.addAttribute("errorMessage", "Trip not found.");
            return "redirect:/easy-bus/trip-management";
        }

        Bus existingBus = busService.getByBusPlate(trip.getBus().getPlate());
        if (existingBus == null) {
            model.addAttribute("errorMessage", "Bus not found.");
            return "redirect:/easy-bus/trip-management";
        }

        List<Bus_Seats> seats = bus_SeatsService.getByBusPlate(existingBus);
        if (seats.isEmpty()) {
            model.addAttribute("errorMessage", "No seats found for this bus.");
            return "redirect:/easy-bus/trip-management";
        }

        List<Map<String, Object>> seatDetails = seats.stream()
                .map(seat -> {
                    Map<String, Object> seatMap = new HashMap<>();
                    SeatReservations reservation = seatReservationService.getReservationBySeatAndTrip(seat, trip);
                    seatMap.put("seatName", seat.getSeatName());
                    seatMap.put("status", reservation.getStatus().toString().toLowerCase());
                    if (reservation != null && reservation.getBooking() != null) {
                        seatMap.put("customerName", reservation.getBooking().getCustomer().getName());
                        seatMap.put("customerEmail", reservation.getBooking().getCustomer().getEmail());
                        seatMap.put("customerPhone", reservation.getBooking().getCustomer().getPhone());
                    } else {
                        seatMap.put("customerName", "");
                        seatMap.put("customerEmail", "");
                        seatMap.put("customerPhone", "");
                    }
                    return seatMap;
                })
                .collect(Collectors.toList());

        model.addAttribute("trip", trip);
        model.addAttribute("seatDetails", seatDetails);
        return "trip-detail";
    }

    @GetMapping("/easy-bus/new-trip")
    public String newTrip(Model model) {
        model.addAttribute("trip", new Trip());
        return "new-trip";
    }

    @PostMapping("/easy-bus/new-trip")
    public String saveTrip(Trip trip) {
        tripService.save(trip);
        return "redirect:/easy-bus/trip-management";
    }

    @GetMapping("/easy-bus/update-trip/{tripId}")
    public String updateTrip(@PathVariable int tripId, Model model) {
        Trip trip = tripService.findTripById(tripId);
        if (trip == null) {
            model.addAttribute("errorMessage", "Trip not found.");
            return "redirect:/easy-bus/trip-management";
        }
        model.addAttribute("trip", trip);
        return "update-trip";
    }

    @PostMapping("/easy-bus/update-trip/{tripId}")
    public String updateTrip(@PathVariable int tripId, Trip trip) {
        Trip existingTrip = tripService.findTripById(tripId);
        if (existingTrip == null) {
            return "redirect:/easy-bus/trip-management";
        }
        existingTrip.setDepartureTime(trip.getDepartureTime());
        existingTrip.setArrivalTime(trip.getArrivalTime());
        existingTrip.setPrice(trip.getPrice());
        existingTrip.setBus(trip.getBus());
        tripService.save(existingTrip);
        return "redirect:/easy-bus/trip-management";
    }
}