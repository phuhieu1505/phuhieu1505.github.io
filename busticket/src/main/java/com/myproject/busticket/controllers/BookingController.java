package com.myproject.busticket.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.myproject.busticket.dto.BookingInfoDTO;
import com.myproject.busticket.dto.SeatReservationsDTO;
import com.myproject.busticket.dto.TripDTO;
import com.myproject.busticket.models.Booking;
import com.myproject.busticket.models.SeatReservations;
import com.myproject.busticket.responses.VNPayResponse;
import com.myproject.busticket.services.BookingService;
import com.myproject.busticket.services.CustomerService;
import com.myproject.busticket.services.RouteCheckpointService;
import com.myproject.busticket.services.SeatReservationsService;
import com.myproject.busticket.services.TripService;
import com.myproject.busticket.services.VNPayService;
import com.myproject.busticket.utilities.VNPayUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TripService tripService;

    @Autowired
    private RouteCheckpointService routeCheckpointService;

    @Autowired
    private SeatReservationsService seatReservationsService;

    @Autowired
    private VNPayService vnPayService;

    @GetMapping("/home/index/search")
    public String searchForm(@RequestParam String tripType, @RequestParam String departure,
            @RequestParam String destination,
            @RequestParam String date,
            @RequestParam(required = false) String returnDate,
            @RequestParam String ticketNum, Model model) {
        int numberOfTickets = Integer.parseInt(ticketNum);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dateDeparture = LocalDate.parse(date, formatter).atStartOfDay();

        // Lấy chuyến đi
        List<TripDTO> trips = tripService.searchTrip(departure, destination, dateDeparture, numberOfTickets);

        // Lấy chuyến về nếu có
        if ("round-trip".equals(tripType) && returnDate != null && !returnDate.isEmpty()) {
            LocalDateTime dateReturn = LocalDate.parse(returnDate, formatter).atStartOfDay();
            List<TripDTO> returnTrips = tripService.searchTrip(destination, departure, dateReturn, numberOfTickets);
            model.addAttribute("returnTrips", returnTrips);  
        }

        // Lấy thông tin các tỉnh/thành cho dropdown bên front end
        List<String> provinces = routeCheckpointService.getAllProvinces();
        List<String> cities = routeCheckpointService.getAllCities();

        model.addAttribute("tripType", "one-way" );
        model.addAttribute("provinces", provinces);
        model.addAttribute("cities", cities);
              
        model.addAttribute("trips", trips);
        
        model.addAttribute("tripType", tripType);
        model.addAttribute("departure", departure);
        model.addAttribute("destination", destination);
        model.addAttribute("date", date);
        model.addAttribute("returnDate", returnDate);
        model.addAttribute("ticketNum", ticketNum);
        return "search-booking";
    }

    @GetMapping("/home/index/booking/{TripId}")
    public String chooseInfo(@PathVariable Integer TripId, Model model) {
        TripDTO selectedTrip = tripService.findById(TripId);
        System.out.println(selectedTrip.getBus().getSeatType());
        List<SeatReservationsDTO> statusSeats = seatReservationsService.getListStatusSeat(TripId);
        List<SeatReservationsDTO> firstFloor = statusSeats.subList(0, statusSeats.size() / 2);
        List<SeatReservationsDTO> secondFloor = statusSeats.subList(statusSeats.size() / 2, statusSeats.size());
        model.addAttribute("selectedTrip", selectedTrip);
        model.addAttribute("firstFloor", firstFloor);
        model.addAttribute("secondFloor", secondFloor);
        model.addAttribute("seatType", selectedTrip.getBus().getSeatType().toString().trim());
        return "booking";
    }

    // @GetMapping("/booking/payment/{TripId}")
    // public String payment(@RequestParam("seat_ids") String seatIds,
    // @RequestParam("total_price") String totalPrice,
    // @RequestParam("name") String name,
    // @RequestParam("phone") String phone,
    // @RequestParam("email") String email,
    // @PathVariable Integer TripId, Model model) {
    // model.addAttribute("seatIds", seatIds);
    // model.addAttribute("totalPrice", totalPrice);
    // model.addAttribute("name", name);
    // model.addAttribute("phone", phone);
    // model.addAttribute("email", email);
    // model.addAttribute("tripId", tripId);
    // return "payment";
    // }
    // @PostMapping("/home/index/booking/oneway")
    // public String booking(@RequestBody BookingInfoDTO bookingInfoDTO, Model
    // model) {
    // //bookingService.createTicketOneWay(bookingInfoDTO);
    // TripDTO selectedTrip =
    // tripService.findById(bookingInfoDTO.getTicketInfoDTO().getTripId());
    // model.addAttribute("selectedTrip", selectedTrip);
    // model.addAttribute("bookingInfoDTO", bookingInfoDTO);
    // return "redirect:/home/index/booking/oneway-payment";
    // }
    @PostMapping("/home/index/booking/oneway-payment")
    public ResponseEntity<String> payment(@RequestBody BookingInfoDTO bookingInfoDTO, Model model,
            HttpServletRequest request) {
        // bookingService.createTicketOneWay(bookingInfoDTO);
        // TripDTO selectedTrip =
        // tripService.findById(bookingInfoDTO.getTicketInfoDTO().getTripId());
        // Lưu BookingInfoDTO vào session
        // session.setAttribute("bookingInfoDTO", bookingInfoDTO);

        try {
            // Giữ chỗ trong 15p
            // đổi trạng thái số ghế
            List<Integer> listSeatId = bookingInfoDTO.getTicketInfoDTO().getSeatNumbers().stream()
                    .map(Integer::valueOf)
                    .toList();
            seatReservationsService.updateStatusReserved(listSeatId,
                    bookingInfoDTO.getTicketInfoDTO().getTripId());

            long amount = (long) (bookingInfoDTO.getTicketInfoDTO().getPrice() * 25000);
            VNPayResponse vnPayResponse = vnPayService.createVNPayPayment(amount, "NCB", request);

            Map<String, String> params = VNPayUtil.extractParamsFromUrl(vnPayResponse.paymentURL());
            boolean isPaymentValid = vnPayService.verifyVNPayPayment(params, params.get("vnp_SecureHash"));

            if (isPaymentValid) {
                System.out.println("Payment URL: " + vnPayResponse.paymentURL());
                return ResponseEntity.ok(vnPayResponse.paymentURL());
            } else {
                return ResponseEntity.badRequest().body("Không thể tạo link thanh toán");
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Log đầy đủ stacktrace
            throw ex; // Để @ExceptionHandler xử lý
        }

    }

    @PostMapping("/home/index/booking/oneway-payment/save")
    public String saveBooking(@RequestBody BookingInfoDTO bookingInfoDTO) throws MessagingException {
        //BookingInfoDTO bookingInfoDTO = (BookingInfoDTO) session.getAttribute("bookingInfoDTO");
        String paymentDate = bookingInfoDTO.getPaymentDate();
        // Định dạng của chuỗi
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Chuyển đổi chuỗi thành LocalDateTime
        LocalDateTime paymentFormat = LocalDateTime.parse(paymentDate, formatter);
        bookingService.createTicketOneWay(bookingInfoDTO, paymentFormat);
        return "index";
    }

    // =============ROUND TRIP============
    @GetMapping("/home/index/booking-roundtrip")
    public String chooseInfRoundTrip(
            @RequestParam("departureTripId") String departureTripId,
            @RequestParam("returnTripId") String returnTripId,
            Model model) {
        int tripId = Integer.parseInt(departureTripId);
        int roundTripId = Integer.parseInt(returnTripId);
        TripDTO trip = tripService.findById(tripId);
        TripDTO roundTrip = tripService.findById(roundTripId);

        List<SeatReservationsDTO> statusSeatsTrip = seatReservationsService.getListStatusSeat(tripId);
        List<SeatReservationsDTO> statusSeatsRoundTrip = seatReservationsService.getListStatusSeat(roundTripId);

        //Trip

        List<SeatReservationsDTO> firstFloorTrip = statusSeatsTrip.subList(0, statusSeatsTrip.size() / 2);
        List<SeatReservationsDTO> secondFloorTrip = statusSeatsTrip.subList(statusSeatsTrip.size() / 2, statusSeatsTrip.size());

        //Round Trip
        List<SeatReservationsDTO> firstFloorRoundTrip = statusSeatsRoundTrip.subList(0, statusSeatsRoundTrip.size() / 2);
        List<SeatReservationsDTO> secondFloorRoundTrip = statusSeatsRoundTrip.subList(statusSeatsRoundTrip.size() / 2, statusSeatsRoundTrip.size());

        //Trip
        model.addAttribute("trip", trip);
        model.addAttribute("roundTrip", roundTrip);
        model.addAttribute("firstFloorTrip", firstFloorTrip);
        model.addAttribute("secondFloorTrip", secondFloorTrip);

        //Round Trip
        model.addAttribute("firstFloorRoundTrip", firstFloorRoundTrip);
        model.addAttribute("secondFloorRoundTrip", secondFloorRoundTrip);

        model.addAttribute("seatTypeTrip", trip.getBus().getSeatType().toString().trim());
        model.addAttribute("seatTypeRoundTrip", roundTrip.getBus().getSeatType().toString().trim());
        System.out.println("firstFloorRoundTrip: " + firstFloorRoundTrip);
        System.out.println("secondFloorRoundTrip: " + secondFloorRoundTrip);
        System.out.println("seatTypeTrip: " + trip.getBus().getSeatType().toString().trim());
        System.out.println("seatTypeRoundTrip: " + roundTrip.getBus().getSeatType().toString().trim());
        return "booking-round-trip";
    }
    @PostMapping("/home/index/booking/roundtrip-payment")
    public ResponseEntity<String> booking(@RequestBody List<BookingInfoDTO> listBooking,
                                          HttpServletRequest request) {
        try {
            long amount = 0;
            for (BookingInfoDTO bookingInfoDTO : listBooking) {
                // Giữ chỗ trong 15p
                // đổi trạng thái số ghế
                List<Integer> listSeatId = bookingInfoDTO.getTicketInfoDTO().getSeatNumbers().stream()
                        .map(Integer::valueOf)
                        .toList();
                seatReservationsService.updateStatusReserved(listSeatId,
                        bookingInfoDTO.getTicketInfoDTO().getTripId());

                // format amount
                amount += (long) (bookingInfoDTO.getTicketInfoDTO().getPrice() * 25000);
            }

            //long amount = (long) (bookingInfoDTO.getTicketInfoDTO().getPrice() * 25000);
            VNPayResponse vnPayResponse = vnPayService.createVNPayPayment(amount, "NCB", request);

            Map<String, String> params = VNPayUtil.extractParamsFromUrl(vnPayResponse.paymentURL());
            boolean isPaymentValid = vnPayService.verifyVNPayPayment(params, params.get("vnp_SecureHash"));

            if (isPaymentValid) {
                System.out.println("Payment URL: " + vnPayResponse.paymentURL());
                return ResponseEntity.ok(vnPayResponse.paymentURL());
            } else {
                return ResponseEntity.badRequest().body("Không thể tạo link thanh toán");
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Log đầy đủ stacktrace
            throw ex; // Để @ExceptionHandler xử lý
        }
    }

    @PostMapping("/home/index/booking/roundtrip-payment/save")
    public String saveBooking(@RequestBody List<BookingInfoDTO> bookingInfoDTO) throws MessagingException {
        String paymentDate = bookingInfoDTO.get(0).getPaymentDate();
        // Định dạng của chuỗi
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        // Chuyển đổi chuỗi thành LocalDateTime
        LocalDateTime paymentDateFormat = LocalDateTime.parse(paymentDate, formatter);
        bookingService.createTicketRoundTrip(bookingInfoDTO, paymentDateFormat);
        return "index";
    }

    @GetMapping("/easy-bus/booking-detail/{bookingId}")
    public String getBookingDetail(@PathVariable int bookingId, Model model) {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            model.addAttribute("message", "Booking not found.");
            return "error";
        }

        // For round trips, get the first booking in sequence
        if (booking.isRoundTrip() && booking.getRoundTripId() != null) {
            List<Booking> roundTripBookings = bookingService.findByRoundTripId(booking.getRoundTripId());
            if (!roundTripBookings.isEmpty()) {
                // Get first booking by booking ID (earliest created)
                booking = roundTripBookings.stream()
                        .min(Comparator.comparing(Booking::getBookingId))
                        .orElse(booking);
            }
        }

        model.addAttribute("booking", booking);

        if (booking.getCustomer() == null) {
            model.addAttribute("message", "Customer information is missing.");
            return "error";
        }

        if (booking.getTrip() == null) {
            model.addAttribute("message", "Trip information is missing.");
            return "error";
        }

        if (booking.getTrip().getBus() == null) {
            model.addAttribute("message", "Bus information is missing.");
            return "error";
        }

        if (booking.getTrip().getDriver() == null || booking.getTrip().getDriver().getAccount() == null) {
            model.addAttribute("message", "Driver information is missing.");
            return "error";
        }

        if (booking.getTrip().getController() == null || booking.getTrip().getController().getAccount() == null) {
            model.addAttribute("message", "Controller information is missing.");
            return "error";
        }

        if (booking.getTrip().getStaff() == null || booking.getTrip().getStaff().getAccount() == null) {
            model.addAttribute("message", "Staff information is missing.");
            return "error";
        }

        if (booking.getTrip().getRoute() == null) {
            model.addAttribute("message", "Route information is missing.");
            return "error";
        }

        model.addAttribute("numberOfSeat", booking.getNumberOfSeat());
        model.addAttribute("roundTrip", booking.isRoundTrip());
        model.addAttribute("roundTripId", booking.getRoundTripId());

        List<SeatReservations> seatReservations = seatReservationsService.getByTrip(booking.getTrip());
        if (seatReservations.isEmpty()) {
            model.addAttribute("message", "Seat reservation not found.");
            return "error";
        }

        List<SeatReservations> seatReservationsWithBookingId = seatReservations.stream()
                .filter(seatReservation -> seatReservation.getBooking() != null).collect(Collectors.toList());

        if (seatReservationsWithBookingId.isEmpty()) {
            model.addAttribute("message", "Seat reservation not found.");
            return "error";
        }

        List<Map<String, Object>> seatDetails = seatReservationsWithBookingId.stream().map(seatReservation -> {
            Map<String, Object> seatMap = new HashMap<>();
            seatMap.put("seatId", seatReservation.getSeat().getId());
            seatMap.put("seatNumber", seatReservation.getSeat().getSeatName());
            seatMap.put("status", seatReservation.getStatus().toString());
            seatMap.put("bookingId", seatReservation.getBooking().getBookingId());
            return seatMap;
        }).collect(Collectors.toList());

        model.addAttribute("seatDetails", seatDetails);

        if (booking.isRoundTrip()) {
            model.addAttribute("roundTrip", true);
            String roundTripId = booking.getRoundTripId();
            List<Booking> roundTripBookings = bookingService.findByRoundTripId(roundTripId);
            if (roundTripBookings.isEmpty()) {
                model.addAttribute("message", "Round trip booking not found.");
                return "error";
            }
            final Booking finalBooking = booking;
            // Get the return trip (second booking in sequence)
            Booking roundTripBooking = roundTripBookings.stream()
                    .filter(b -> b.getBookingId() != finalBooking.getBookingId())
                    .min(Comparator.comparing(Booking::getBookingId))
                    .orElse(null);

            if (roundTripBooking == null) {
                model.addAttribute("message", "Return trip booking not found.");
                return "error";
            }

            model.addAttribute("roundTrip", roundTripBooking);

            List<SeatReservations> roundTripSeatReservations = seatReservationsService
                    .getByTrip(roundTripBooking.getTrip());
            if (roundTripSeatReservations.isEmpty()) {
                model.addAttribute("message", "Seat reservation not found for round trip.");
                return "error";
            }

            List<SeatReservations> roundTripSeatReservationsWithBookingId = roundTripSeatReservations.stream()
                    .filter(seatReservation -> seatReservation.getBooking() != null).collect(Collectors.toList());

            if (roundTripSeatReservationsWithBookingId.isEmpty()) {
                model.addAttribute("message", "Seat reservation not found for round trip.");
                return "error";
            }

            List<Map<String, Object>> roundTripSeatDetails = roundTripSeatReservationsWithBookingId.stream()
                    .map(seatReservation -> {
                        Map<String, Object> seatMap = new HashMap<>();
                        seatMap.put("seatId", seatReservation.getSeat().getId());
                        seatMap.put("seatNumber", seatReservation.getSeat().getSeatName());
                        seatMap.put("status", seatReservation.getStatus().toString());
                        seatMap.put("bookingId", seatReservation.getBooking().getBookingId());
                        return seatMap;
                    }).collect(Collectors.toList());
            model.addAttribute("roundTripSeatDetails", roundTripSeatDetails);
            model.addAttribute("tripId", roundTripBooking.getTrip().getTripId());
            model.addAttribute("departureTime", roundTripBooking.getTrip().getDepartureTime());
            model.addAttribute("arrivalTime", roundTripBooking.getTrip().getArrivalTime());
            model.addAttribute("price", roundTripBooking.getTrip().getPrice());
            model.addAttribute("status", roundTripBooking.getTrip().getStatus());
        }

        return "booking-detail";
    }

    @GetMapping("/easy-bus/new-booking")
    public String newBooking(Model model) {
        model.addAttribute("booking", new Booking());
        return "new-booking";
    }

    @PostMapping("/easy-bus/new-booking")
    public String saveBooking(Booking booking) {
        // bookingService.createTicket(booking);
        return "redirect:/easy-bus/booking-management";
    }
}
