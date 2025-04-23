package com.myproject.busticket.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
import com.myproject.busticket.enums.TripStatus;
import com.myproject.busticket.models.Bill;
import com.myproject.busticket.models.Bill_Detail;
import com.myproject.busticket.models.Booking;
import com.myproject.busticket.models.Bus;
import com.myproject.busticket.models.Bus_Seats;
import com.myproject.busticket.models.Controller;
import com.myproject.busticket.models.Driver;
import com.myproject.busticket.models.Route;
import com.myproject.busticket.models.SeatReservations;
import com.myproject.busticket.models.Staff;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.services.BillDetailService;
import com.myproject.busticket.services.BillService;
import com.myproject.busticket.services.BookingService;
import com.myproject.busticket.services.BusService;
import com.myproject.busticket.services.Bus_SeatsService;
import com.myproject.busticket.services.ControllerService;
import com.myproject.busticket.services.DriverService;
import com.myproject.busticket.services.RouteService;
import com.myproject.busticket.services.SeatReservationsService;
import com.myproject.busticket.services.StaffService;
import com.myproject.busticket.services.TripService;

@RestController
@RequestMapping("/api/trip")
public class TripAPI {
    @Autowired
    private TripService tripService;

    @Autowired
    private BusService busService;

    @Autowired
    private Bus_SeatsService bus_SeatsService;

    @Autowired
    private SeatReservationsService seatReservationService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private ControllerService controllerService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BillService billService;

    @Autowired
    private BillDetailService billDetailService;

    @GetMapping("/trips")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Trip> tripsPage;

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null && !startDate.isEmpty()) {
            startDateTime = LocalDateTime.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            endDateTime = LocalDateTime.parse(endDate);
        }

        tripsPage = tripService.searchTrips(pageable, searchValue, startDateTime, endDateTime);

        List<Trip> trips = tripsPage.getContent().stream()
                .map(trip -> new Trip(trip.getTripId(), trip.getDepartureTime(), trip.getArrivalTime(),
                        trip.getPrice(), trip.getStatus(), trip.getBus(), trip.getDriver(),
                        trip.getController(), trip.getStaff(), trip.getRoute()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("trips", trips);
        response.put("currentPage", page);
        response.put("totalPages", tripsPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trip-detail/{tripId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTripDetails(@PathVariable int tripId) {
        Trip trip = tripService.findTripById(tripId);
        if (trip == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "Trip not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Bus existingBus = busService.getByBusPlate(trip.getBus().getPlate());
        if (existingBus == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "Bus not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        List<Bus_Seats> seats = bus_SeatsService.getByBusPlate(existingBus);
        if (seats.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "No seats found for this bus.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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

        Map<String, Object> response = new HashMap<>();
        response.put("trip", trip);
        response.put("seatDetails", seatDetails);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming-trips")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUpcomingTrips(@RequestParam String entityType,
            @RequestParam String entityId) {
        LocalDateTime now = LocalDateTime.now();
        List<Trip> upcomingTrips = tripService.findUpcomingTrips(now);
        List<Trip> ongoingTrips = tripService.findOnGoingTrips(now);

        // Combine upcoming and ongoing trips
        List<Trip> allTrips = new ArrayList<>();
        allTrips.addAll(upcomingTrips);
        allTrips.addAll(ongoingTrips);

        if (allTrips.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("errorMessage", "No upcoming or ongoing trips found.");
            return ResponseEntity.ok(response);
        }

        List<Trip> filteredTrips = allTrips.stream()
                .filter(trip -> {
                    switch (entityType) {
                        case "bus":
                            return trip.getBus().getPlate().equals(entityId);
                        case "driver":
                            return trip.getDriver().getDriverId() == Integer.parseInt(entityId);
                        case "controller":
                            return trip.getController().getId() == Integer.parseInt(entityId);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> tripDetails = filteredTrips.stream()
                .map(trip -> {
                    Map<String, Object> tripMap = new HashMap<>();
                    tripMap.put("tripId", trip.getTripId());
                    tripMap.put("departureTime", trip.getDepartureTime());
                    tripMap.put("arrivalTime", trip.getArrivalTime());
                    tripMap.put("price", trip.getPrice());
                    tripMap.put("status", trip.getStatus().toString().toLowerCase());
                    tripMap.put("busPlate", trip.getBus().getPlate());
                    tripMap.put("driverName", trip.getDriver().getAccount().getFullName());
                    tripMap.put("controllerName", trip.getController().getAccount().getFullName());
                    tripMap.put("staffName", trip.getStaff().getAccount().getFullName());
                    tripMap.put("routeCode", trip.getRoute().getCode());
                    return tripMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("trips", tripDetails);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assigned-trips/{plate}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBusTrips(
            @PathVariable String plate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Bus bus = busService.getByBusPlate(plate);
        if (bus == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Bus not found."));
        }

        List<Trip> trips = tripService.findByBus(bus);
        if (trips == null || trips.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No trips found for this bus.", "trips", List.of()));
        }

        int totalElements = trips.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = Math.min(page * size, totalElements);
        int end = Math.min((page + 1) * size, totalElements);

        List<Map<String, Object>> tripDetails = trips.subList(start, end).stream()
                .map(trip -> {
                    Map<String, Object> tripMap = new HashMap<>();
                    tripMap.put("tripId", trip.getTripId());
                    tripMap.put("departureTime", trip.getDepartureTime());
                    tripMap.put("arrivalTime", trip.getArrivalTime());
                    tripMap.put("price", trip.getPrice());
                    tripMap.put("status", trip.getStatus() != null ? trip.getStatus().name() : "Unknown");
                    tripMap.put("routeCode", trip.getRoute() != null ? trip.getRoute().getCode() : "Unknown");
                    tripMap.put("driverName", trip.getDriver() != null && trip.getDriver().getAccount() != null
                            ? trip.getDriver().getAccount().getFullName()
                            : "Unknown");
                    tripMap.put("controllerName",
                            trip.getController() != null && trip.getController().getAccount() != null
                                    ? trip.getController().getAccount().getFullName()
                                    : "Unknown");
                    return tripMap;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "trips", tripDetails,
                "totalPages", totalPages,
                "totalElements", totalElements));
    }

    @PostMapping("/new-trip")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveTrip(@RequestBody Map<String, Object> tripRequest) {
        Map<String, Object> response = new HashMap<>();
        String departureTimeStr = (String) tripRequest.get("departureTime");
        if (departureTimeStr == null || departureTimeStr.isEmpty()) {
            response.put("message", "Departure time is required.");
            return ResponseEntity.badRequest().body(response);
        }

        String arrivalTimeStr = (String) tripRequest.get("arrivalTime");
        if (arrivalTimeStr == null || arrivalTimeStr.isEmpty()) {
            response.put("message", "Arrival time is required.");
            return ResponseEntity.badRequest().body(response);
        }

        if (arrivalTimeStr.equals(departureTimeStr)) {
            response.put("message", "Arrival time can't be the same as departure time.");
            return ResponseEntity.badRequest().body(response);
        } else if (LocalDateTime.parse(arrivalTimeStr).isBefore(LocalDateTime.parse(departureTimeStr))) {
            response.put("message", "Arrival time can't be before departure time.");
            return ResponseEntity.badRequest().body(response);
        }

        float price = Float.parseFloat(tripRequest.get("price").toString());
        if (price < 0) {
            response.put("message", "Price can't be negative.");
            return ResponseEntity.badRequest().body(response);
        }
        String busPlate = (String) tripRequest.get("busPlate");
        if (busPlate == null || busPlate.isEmpty()) {
            response.put("message", "Bus plate is required.");
            return ResponseEntity.badRequest().body(response);
        }

        int driverId = Integer.parseInt(tripRequest.get("driverId").toString());
        if (driverId <= 0) {
            response.put("message", "Driver ID is required.");
            return ResponseEntity.badRequest().body(response);
        }

        int controllerId = Integer.parseInt(tripRequest.get("controllerId").toString());
        if (controllerId <= 0) {
            response.put("message", "Controller ID is required.");
            return ResponseEntity.badRequest().body(response);
        }

        int staffId = Integer.parseInt(tripRequest.get("staff_id").toString());
        if (staffId <= 0) {
            response.put("message", "Staff ID is required.");
            return ResponseEntity.badRequest().body(response);
        }

        String routeCode = (String) tripRequest.get("routeCode");
        if (routeCode == null || routeCode.isEmpty()) {
            response.put("message", "Route code is required.");
            return ResponseEntity.badRequest().body(response);
        }

        LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr);
        LocalDateTime arrivalTime = LocalDateTime.parse(arrivalTimeStr);

        List<Trip> conflictingTripsByBus = tripService.findConflictingTripsByBus(busPlate, departureTime,
                arrivalTime);
        if (!conflictingTripsByBus.isEmpty()) {
            response.put("message", "Bus is already assigned to another trip at the same time.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Trip> conflictingTripsByDriver = tripService.findConflictingTripsByDriver(driverId, departureTime,
                arrivalTime);
        if (!conflictingTripsByDriver.isEmpty()) {
            response.put("message", "Driver is already assigned to another trip at the same time.");
            return ResponseEntity.badRequest().body(response);
        }

        List<Trip> conflictingTripsByController = tripService.findConflictingTripsByController(controllerId,
                departureTime, arrivalTime);
        if (!conflictingTripsByController.isEmpty()) {
            response.put("message", "Controller is already assigned to another trip at the same time.");
            return ResponseEntity.badRequest().body(response);
        }

        Bus existingBus = busService.getByBusPlate(busPlate);
        if (existingBus == null) {
            response.put("message", "Bus not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Driver driver = driverService.getDriverById(driverId);
        if (driver == null) {
            response.put("message", "Driver not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Controller controller = controllerService.getControllerById(controllerId);
        if (controller == null) {
            response.put("message", "Controller not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Staff staff = staffService.getStaffById(staffId);
        if (staff == null) {
            response.put("message", "Staff not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Route route = routeService.getRouteByCode(routeCode);
        if (route == null) {
            response.put("message", "Route not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Trip newTrip = new Trip();
        newTrip.setDepartureTime(departureTime);
        newTrip.setArrivalTime(arrivalTime);
        newTrip.setPrice(price);
        newTrip.setStatus(TripStatus.waiting);
        newTrip.setBus(existingBus);
        newTrip.setDriver(driver);
        newTrip.setController(controller);
        newTrip.setStaff(staff);
        newTrip.setRoute(route);
        tripService.save(newTrip);

        // for loop to add all the seats to the trip:
        List<Bus_Seats> seats = bus_SeatsService.getByBusPlate(existingBus);
        for (Bus_Seats seat : seats) {
            SeatReservations reservation = new SeatReservations();
            reservation.setTrip(newTrip);
            reservation.setSeat(seat);
            reservation.setStatus(SeatReservationStatus.open);
            seatReservationService.save(reservation);
        }
        response.put("message", "Trip saved successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-trip/{tripId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTrip(@PathVariable int tripId,
            @RequestBody Map<String, Object> tripRequest) {
        Map<String, Object> response = new HashMap<>();
        Trip existingTrip = tripService.findTripById(tripId);
        if (existingTrip == null) {
            response.put("message", "Trip not found.");
            return ResponseEntity.badRequest().body(response);
        }

        String departureTimeStr = (String) tripRequest.get("departureTime");
        if (departureTimeStr == null || departureTimeStr.isEmpty()) {
            response.put("message", "Departure time is required.");
            return ResponseEntity.badRequest().body(response);
        }

        String arrivalTimeStr = (String) tripRequest.get("arrivalTime");
        if (arrivalTimeStr == null || arrivalTimeStr.isEmpty()) {
            response.put("message", "Arrival time is required.");
            return ResponseEntity.badRequest().body(response);
        }

        if (arrivalTimeStr.equals(departureTimeStr)) {
            response.put("message", "Arrival time can't be the same as departure time.");
            return ResponseEntity.badRequest().body(response);
        } else if (LocalDateTime.parse(arrivalTimeStr).isBefore(LocalDateTime.parse(departureTimeStr))) {
            response.put("message", "Arrival time can't be before departure time.");
            return ResponseEntity.badRequest().body(response);
        }

        float price = Float.parseFloat(tripRequest.get("price").toString());
        if (price < 0) {
            response.put("message", "Price can't be negative.");
            return ResponseEntity.badRequest().body(response);
        }

        String busPlate = (String) tripRequest.get("busPlate");
        if (busPlate == null || busPlate.isEmpty()) {
            response.put("message", "Bus plate is required.");
            return ResponseEntity.badRequest().body(response);
        }

        int driverId = Integer.parseInt(tripRequest.get("driverId").toString());
        if (driverId <= 0) {
            response.put("message", "Driver ID is required.");
            return ResponseEntity.badRequest().body(response);
        }

        int controllerId = Integer.parseInt(tripRequest.get("controllerId").toString());
        if (controllerId <= 0) {
            response.put("message", "Controller ID is required.");
            return ResponseEntity.badRequest().body(response);
        }

        int staffId = Integer.parseInt(tripRequest.get("staff_id").toString());
        if (staffId <= 0) {
            response.put("message", "Staff ID is required.");
            return ResponseEntity.badRequest().body(response);
        }

        String routeCode = (String) tripRequest.get("routeCode");
        if (routeCode == null || routeCode.isEmpty()) {
            response.put("message", "Route code is required.");
            return ResponseEntity.badRequest().body(response);
        }

        String statusStr = (String) tripRequest.get("status");
        if (statusStr == null || statusStr.isEmpty()) {
            response.put("message", "Status is required.");
            return ResponseEntity.badRequest().body(response);
        }

        LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr);
        LocalDateTime arrivalTime = LocalDateTime.parse(arrivalTimeStr);

        Bus existingBus = busService.getByBusPlate(busPlate);
        if (existingBus == null) {
            response.put("message", "Bus not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Driver driver = driverService.getDriverById(driverId);
        if (driver == null) {
            response.put("message", "Driver not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Controller controller = controllerService.getControllerById(controllerId);
        if (controller == null) {
            response.put("message", "Controller not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Staff staff = staffService.getStaffById(staffId);
        if (staff == null) {
            response.put("message", "Staff not found.");
            return ResponseEntity.badRequest().body(response);
        }

        Route route = routeService.getRouteByCode(routeCode);
        if (route == null) {
            response.put("message", "Route not found.");
            return ResponseEntity.badRequest().body(response);
        }

        TripStatus status = TripStatus.valueOf(statusStr.toLowerCase());

        boolean isStatusChanged = !existingTrip.getStatus().equals(status);
        boolean isBusChanged = !existingTrip.getBus().equals(existingBus);
        boolean isDriverChanged = !existingTrip.getDriver().equals(driver);
        boolean isControllerChanged = !existingTrip.getController().equals(controller);
        boolean isDepartureTimeChanged = !existingTrip.getDepartureTime().equals(departureTime);
        boolean isArrivalTimeChanged = !existingTrip.getArrivalTime().equals(arrivalTime);

        if (!isStatusChanged && !isBusChanged && !isDriverChanged && !isControllerChanged && !isDepartureTimeChanged &&
                !isArrivalTimeChanged && existingTrip.getPrice() == price && existingTrip.getRoute().equals(route)) {
            response.put("message", "Nothing has been changed.");
            return ResponseEntity.badRequest().body(response);
        }

        if (isBusChanged) {
            List<Trip> conflictingTripsByBus = tripService.findConflictingTripsByBus(busPlate,
                    existingTrip.getDepartureTime(),
                    existingTrip.getArrivalTime());
            if (!conflictingTripsByBus.isEmpty()) {
                response.put("message", "New bus is already assigned to another trip at the same time.");
                return ResponseEntity.badRequest().body(response);
            }
        }

        if (isDriverChanged) {
            List<Trip> conflictingTripsByDriver = tripService.findConflictingTripsByDriver(driverId,
                    existingTrip.getDepartureTime(),
                    existingTrip.getArrivalTime());
            if (!conflictingTripsByDriver.isEmpty()) {
                response.put("message", "Driver is already assigned to another trip at the same time.");
                return ResponseEntity.badRequest().body(response);
            }
        }

        if (isControllerChanged) {
            List<Trip> conflictingTripsByController = tripService.findConflictingTripsByController(controllerId,
                    existingTrip.getDepartureTime(), existingTrip.getArrivalTime());
            if (!conflictingTripsByController.isEmpty()) {
                response.put("message", "Controller is already assigned to another trip at the same time.");
                return ResponseEntity.badRequest().body(response);
            }
        }

        if (isDepartureTimeChanged || isArrivalTimeChanged) {
            List<Trip> conflictingTripsByBus = tripService.findConflictingTripsByBus(busPlate, departureTime,
                    arrivalTime);
            if (!conflictingTripsByBus.isEmpty()) {
                response.put("message", "Bus is already assigned to another trip at the same time.");
                return ResponseEntity.badRequest().body(response);
            }

            List<Trip> conflictingTripsByDriver = tripService.findConflictingTripsByDriver(driverId, departureTime,
                    arrivalTime);
            if (!conflictingTripsByDriver.isEmpty()) {
                response.put("message", "Driver is already assigned to another trip at the same time.");
                return ResponseEntity.badRequest().body(response);
            }

            List<Trip> conflictingTripsByController = tripService.findConflictingTripsByController(controllerId,
                    departureTime, arrivalTime);
            if (!conflictingTripsByController.isEmpty()) {
                response.put("message", "Controller is already assigned to another trip at the same time.");
                return ResponseEntity.badRequest().body(response);
            }
        }

        if (isBusChanged) {
            List<Bus_Seats> oldSeats = bus_SeatsService.getByBusPlate(existingTrip.getBus());
            for (Bus_Seats oldSeat : oldSeats) {
                SeatReservations reservations = seatReservationService.getReservationBySeatAndTrip(oldSeat,
                        existingTrip);
                seatReservationService.delete(reservations);
            }

            Bus existingBus1 = busService.getByBusPlate(busPlate);
            List<Bus_Seats> newSeats = bus_SeatsService.getByBusPlate(existingBus1);
            for (Bus_Seats newSeat : newSeats) {
                SeatReservations reservation = new SeatReservations();
                reservation.setTrip(existingTrip);
                reservation.setSeat(newSeat);
                reservation.setStatus(SeatReservationStatus.open);
                seatReservationService.save(reservation);
            }
        }

        existingTrip.setDepartureTime(departureTime);
        existingTrip.setArrivalTime(arrivalTime);
        existingTrip.setPrice(price);
        existingTrip.setStatus(status);
        existingTrip.setBus(existingBus);
        existingTrip.setDriver(driver);
        existingTrip.setController(controller);
        existingTrip.setStaff(staff);
        existingTrip.setRoute(route);

        tripService.save(existingTrip);

        response.put("message", "Trip updated successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/selected-trip/{tripId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSelectedTrip(@PathVariable int tripId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Trip trip = tripService.findTripById(tripId);
            if (trip == null) {
                response.put("errorMessage", "Trip not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Bus existingBus = busService.getByBusPlate(trip.getBus().getPlate());
            if (existingBus == null) {
                response.put("errorMessage", "Bus not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Driver driver = driverService.getDriverById(trip.getDriver().getDriverId());
            if (driver == null) {
                response.put("errorMessage", "Driver not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Controller controller = controllerService.getControllerById(trip.getController().getId());
            if (controller == null) {
                response.put("errorMessage", "Controller not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Staff staff = staffService.getStaffById(trip.getStaff().getStaff_id());
            if (staff == null) {
                response.put("errorMessage", "Staff not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Route route = routeService.getRouteByCode(trip.getRoute().getCode());
            if (route == null) {
                response.put("errorMessage", "Route not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Map<String, Object> tripData = new HashMap<>();
            tripData.put("tripId", trip.getTripId());
            tripData.put("price", trip.getPrice());
            tripData.put("status", trip.getStatus().name());
            tripData.put("departureTime", trip.getDepartureTime());
            tripData.put("arrivalTime", trip.getArrivalTime());
            tripData.put("busPlate", trip.getBus().getPlate());
            tripData.put("driverId", trip.getDriver().getDriverId());
            tripData.put("controllerId", trip.getController().getId());
            tripData.put("staff_id", trip.getStaff().getStaff_id());
            tripData.put("routeCode", trip.getRoute().getCode());
            response.put("trip", tripData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete-trip/{tripId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteTrip(@PathVariable int tripId) {
        Map<String, Object> response = new HashMap<>();
        Trip existingTrip = tripService.findTripById(tripId);
        if (existingTrip == null) {
            response.put("message", "Trip not found.");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
        // Check for booking :
        List<Booking> bookings = bookingService.findByTrip(existingTrip);
        if (!bookings.isEmpty()) {
            response.put("message", "Trip has bookings and cannot be deleted.");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }

        // Check for bill :
        List<Bill_Detail> bills = billDetailService.findByTrip(existingTrip);
        if (!bills.isEmpty()) {
            response.put("message", "Trip has billing and cannot be deleted.");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }

        // if booking and bill is empty, proceed delete trip:
        List<SeatReservations> reservations = seatReservationService.getByTrip(existingTrip);
        for (SeatReservations reservation : reservations) {
            seatReservationService.delete(reservation);
        }

        tripService.deleteTripById(tripId);
        response.put("message", "Trip deleted successfully.");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/force-delete-trip/{tripId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> forceDeleteTrip(@PathVariable int tripId) {
        Map<String, Object> response = new HashMap<>();
        Trip existingTrip = tripService.findTripById(tripId);
        if (existingTrip == null) {
            response.put("message", "Trip not found.");
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }

        // Delete all seat reservations associated with the trip
        List<SeatReservations> reservations = seatReservationService.getByTrip(existingTrip);
        if (!reservations.isEmpty()) {
            seatReservationService.deleteAll(reservations);
        }

        List<Bill_Detail> billDetails = billDetailService.findByTrip(existingTrip);
        if (!billDetails.isEmpty()) {
            Bill bill = billDetails.get(0).getBill();
            billDetailService.deleteAll(billDetails);
            billService.delete(bill);
        }

        tripService.deleteTripById(tripId);
        response.put("message", "Trip deleted successfully.");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
