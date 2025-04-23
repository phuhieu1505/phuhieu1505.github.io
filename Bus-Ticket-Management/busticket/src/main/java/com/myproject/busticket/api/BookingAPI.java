package com.myproject.busticket.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.dto.AccountDTO;
import com.myproject.busticket.dto.BookingDTO;
import com.myproject.busticket.dto.BusDTO;
import com.myproject.busticket.dto.ControllerDTO;
import com.myproject.busticket.dto.CustomerDTO;
import com.myproject.busticket.dto.DriverDTO;
import com.myproject.busticket.dto.RouteDTO;
import com.myproject.busticket.dto.StaffDTO;
import com.myproject.busticket.dto.TripDTO;
import com.myproject.busticket.enums.SeatReservationStatus;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Booking;
import com.myproject.busticket.models.Bus_Seats;
import com.myproject.busticket.models.Customer;
import com.myproject.busticket.models.SeatReservations;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.services.AccountService;
import com.myproject.busticket.services.BookingService;
import com.myproject.busticket.services.Bus_SeatsService;
import com.myproject.busticket.services.CustomerService;
import com.myproject.busticket.services.SeatReservationsService;
import com.myproject.busticket.services.TripService;

@RestController
@RequestMapping("/api/booking")
public class BookingAPI {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private Bus_SeatsService bus_SeatsService;

    @Autowired
    private TripService tripService;

    @Autowired
    private SeatReservationsService seatReservationService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/bookings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookings(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPages;

        if (searchValue != null && !searchValue.isEmpty()) {
            bookingPages = bookingService.searchBookings(searchValue, pageable);
        } else {
            bookingPages = bookingService.getAll(pageable);
        }

        // Group bookings by round_trip_id and get only the first booking of each group
        List<BookingDTO> bookings = bookingPages.getContent().stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getRoundTripId() != null ? booking.getRoundTripId()
                                : booking.getBookingId()))
                .values().stream()
                .map(groupedBookings -> groupedBookings.stream()
                        .min(Comparator.comparing(Booking::getBookingId))
                        .map(this::convertToBookingDTO)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        response.put("totalPages", bookingPages.getTotalPages());
        response.put("currentPage", page);

        return ResponseEntity.ok(response);
    }

    private BookingDTO convertToBookingDTO(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                new CustomerDTO(
                        booking.getCustomer().getCustomerId(),
                        booking.getCustomer().getEmail(),
                        booking.getCustomer().getName(),
                        booking.getCustomer().getPhone()),
                new TripDTO(
                        booking.getTrip().getTripId(),
                        booking.getTrip().getDepartureTime(),
                        booking.getTrip().getArrivalTime(),
                        booking.getTrip().getPrice(),
                        booking.getTrip().getStatus(),
                        new BusDTO(
                                booking.getTrip().getBus().getPlate(),
                                booking.getTrip().getBus().getNumberOfSeat(),
                                booking.getTrip().getBus().getSeatType()),
                        new DriverDTO(
                                booking.getTrip().getDriver().getDriverId(),
                                new AccountDTO(
                                        booking.getTrip().getDriver().getAccount().getId(),
                                        booking.getTrip().getDriver().getAccount().getEmail(),
                                        booking.getTrip().getDriver().getAccount().getPassword(),
                                        booking.getTrip().getDriver().getAccount().getFullName(),
                                        booking.getTrip().getDriver().getAccount().getPhone(),
                                        booking.getTrip().getDriver().getAccount().getRole(),
                                        booking.getTrip().getDriver().getAccount().getStatus(),
                                        booking.getTrip().getDriver().getAccount().getVerificationCode(),
                                        booking.getTrip().getDriver().getAccount().getVerificationExpiration(),
                                        booking.getTrip().getDriver().getAccount().getLoginToken(),
                                        booking.getTrip().getDriver().getAccount().getPasswordResetToken(),
                                        booking.getTrip().getDriver().getAccount().getPasswordResetExpiration(),
                                        booking.getTrip().getDriver().getAccount().isEnabled())),
                        new ControllerDTO(
                                booking.getTrip().getController().getId(),
                                new AccountDTO(
                                        booking.getTrip().getController().getAccount().getId(),
                                        booking.getTrip().getController().getAccount().getEmail(),
                                        booking.getTrip().getController().getAccount().getPassword(),
                                        booking.getTrip().getController().getAccount().getFullName(),
                                        booking.getTrip().getController().getAccount().getPhone(),
                                        booking.getTrip().getController().getAccount().getRole(),
                                        booking.getTrip().getController().getAccount().getStatus(),
                                        booking.getTrip().getController().getAccount().getVerificationCode(),
                                        booking.getTrip().getController().getAccount()
                                                .getVerificationExpiration(),
                                        booking.getTrip().getController().getAccount().getLoginToken(),
                                        booking.getTrip().getController().getAccount().getPasswordResetToken(),
                                        booking.getTrip().getController().getAccount()
                                                .getPasswordResetExpiration(),
                                        booking.getTrip().getController().getAccount().isEnabled())),
                        new StaffDTO(
                                booking.getTrip().getStaff().getStaff_id(),
                                new AccountDTO(
                                        booking.getTrip().getStaff().getAccount().getId(),
                                        booking.getTrip().getStaff().getAccount().getEmail(),
                                        booking.getTrip().getStaff().getAccount().getPassword(),
                                        booking.getTrip().getStaff().getAccount().getFullName(),
                                        booking.getTrip().getStaff().getAccount().getPhone(),
                                        booking.getTrip().getStaff().getAccount().getRole(),
                                        booking.getTrip().getStaff().getAccount().getStatus(),
                                        booking.getTrip().getStaff().getAccount().getVerificationCode(),
                                        booking.getTrip().getStaff().getAccount().getVerificationExpiration(),
                                        booking.getTrip().getStaff().getAccount().getLoginToken(),
                                        booking.getTrip().getStaff().getAccount().getPasswordResetToken(),
                                        booking.getTrip().getStaff().getAccount().getPasswordResetExpiration(),
                                        booking.getTrip().getStaff().getAccount().isEnabled())),
                        new RouteDTO(
                                booking.getTrip().getRoute().getRouteId(),
                                booking.getTrip().getRoute().getCode(),
                                booking.getTrip().getRoute().getName(),
                                booking.getTrip().getRoute().getTime(),
                                booking.getTrip().getRoute().getDistance())),
                booking.getNumberOfSeat(),
                booking.isRoundTrip(),
                booking.getRoundTripId());
    }

    @PostMapping("/new-booking")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBooking(@RequestBody Map<String, Object> bookingRequest) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = null;
            // Get customer information if customer type = Guest:
            String customerType = (String) bookingRequest.get("customerType");
            if (customerType.equals("guest")) {
                // Retrieve customer information:
                String customerName = (String) bookingRequest.get("customerName");
                String customerEmail = (String) bookingRequest.get("customerEmail");
                String customerPhone = (String) bookingRequest.get("customerPhone");

                // Create new customer
                customer = new Customer();
                customer.setName(customerName);
                customer.setEmail(customerEmail);
                customer.setPhone(customerPhone);
            } else {
                // Retrieve account ID if customer type = Account:
                int accountId = (int) bookingRequest.get("accountId");
                Account account = accountService.getById(accountId);
                if (account == null) {
                    response.put("message", "Account not found.");
                    return ResponseEntity.badRequest().body(response);
                }

                // create new customer:
                customer = new Customer();
                if (account.getEmail() == null || account.getFullName() == null || account.getPhone() == null) {
                    response.put("message", "Account information is incomplete.");
                    return ResponseEntity.badRequest().body(response);
                }
                customer.setName(account.getFullName());
                customer.setEmail(account.getEmail());
                customer.setPhone(account.getPhone());
            }

            // Handle selection trip:
            int tripId = Integer.parseInt(bookingRequest.get("tripId").toString());
            Trip trip = tripService.findTripById(tripId);
            if (trip == null) {
                response.put("message", "Trip not found.");
                return ResponseEntity.badRequest().body(response);
            }

            // Handle number of seats:
            byte numberOfSeats = (bookingRequest.get("numberOfSeats").toString()).isEmpty()
                    ? 0
                    : Byte.parseByte(bookingRequest.get("numberOfSeats").toString());
            if (numberOfSeats < 1) {
                response.put("message", "At least one seat must be selected.");
                return ResponseEntity.badRequest().body(response);
            }

            // Handle selected seats:
            List<Integer> selectedSeats = (List<Integer>) bookingRequest.get("selectedSeats");
            if (selectedSeats.size() != numberOfSeats) {
                response.put("message", "Number of selected seats does not match the number of seats.");
                return ResponseEntity.badRequest().body(response);
            }

            // Get seat reservations by trip and seat:
            List<SeatReservations> seatReservations = new ArrayList<>();
            for (int seatId : selectedSeats) {
                // Get bus seat by seat ID:
                Bus_Seats seat = bus_SeatsService.getById(seatId);
                // Get seat reservation by seat and trip:
                SeatReservations seatReservation = seatReservationService.getReservationBySeatAndTrip(seat, trip);
                if (seatReservation == null || !seatReservation.getStatus().equals(SeatReservationStatus.open)) {
                    response.put("message", "Selected seat is not available.");
                    return ResponseEntity.badRequest().body(response);
                }
                seatReservations.add(seatReservation);
            }

            // Get ticket type:
            String ticketType = (String) bookingRequest.get("ticketType");
            if (ticketType == null || ticketType.isEmpty()) {
                response.put("message", "Ticket type is required.");
                return ResponseEntity.badRequest().body(response);
            }

            if (ticketType.equals("One-way")) {
                // Create booking
                Booking booking = new Booking();
                booking.setCustomer(customer);
                booking.setTrip(trip);
                booking.setNumberOfSeat(numberOfSeats);
                booking.setRoundTrip(false);
                booking.setRoundTripId(null);

                // Retrieve selected seats and update seat_reservations to reserved:
                for (SeatReservations seatReservation : seatReservations) {
                    seatReservation.setStatus(SeatReservationStatus.reserved);
                    seatReservationService.save(seatReservation);
                }
                customerService.create(customer);
                // bookingService.createTicket(booking);
                response.put("message", "Booking created successfully.");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error creating booking: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
