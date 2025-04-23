package com.myproject.busticket.api;

import java.time.LocalDateTime;
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

import com.myproject.busticket.dto.BillDTO;
import com.myproject.busticket.dto.BillDetailDTO;
import com.myproject.busticket.dto.CustomerDTO;
import com.myproject.busticket.dto.TripDTO;
import com.myproject.busticket.enums.PaymentMethod;
import com.myproject.busticket.enums.TicketType;
import com.myproject.busticket.enums.TripStatus;
import com.myproject.busticket.mapper.BillDetailMapper;
import com.myproject.busticket.mapper.BillMapper;
import com.myproject.busticket.mapper.CustomerMapper;
import com.myproject.busticket.mapper.TripMapper;
import com.myproject.busticket.models.Bill;
import com.myproject.busticket.models.Bill_Detail;
import com.myproject.busticket.models.Customer;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.services.AuthenticationService;
import com.myproject.busticket.services.BillDetailService;
import com.myproject.busticket.services.BillService;
import com.myproject.busticket.services.CustomerService;
import com.myproject.busticket.services.TripService;

@RestController
@RequestMapping("/api/bill")
public class BillAPI {
    @Autowired
    private BillService billService;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private BillDetailService billDetailService;

    @Autowired
    private BillDetailMapper billDetailMapper;

    @Autowired
    private TripService tripService;

    @Autowired
    private TripMapper tripMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @GetMapping("/bills")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Bill> billPages;

        if ((searchValue != null && !searchValue.isEmpty())
                || (startDate != null && !startDate.isEmpty()) || (endDate != null && !endDate.isEmpty())) {
            billPages = billService.searchBills(pageable, searchValue, startDate, endDate);
        } else {
            billPages = billService.getAll(pageable);
        }

        List<BillDTO> bills = billMapper.map(billPages.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("bills", bills);
        response.put("currentPage", page);
        response.put("totalPages", billPages.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{billId}")
    public ResponseEntity<Map<String, Object>> getBillDetails(@PathVariable int billId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Bill bill = billService.findById(billId);
            if (bill == null) {
                response.put("success", false);
                response.put("errorMessage", "Bill not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Customer customer = customerService.getCustomerById(bill.getCustomer().getCustomerId());
            if (customer == null) {
                response.put("success", false);
                response.put("errorMessage", "Customer not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            List<Bill_Detail> billDetails = billDetailService.findByBillId(bill);
            if (billDetails == null || billDetails.isEmpty()) {
                response.put("success", false);
                response.put("errorMessage", "Bill details not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Trip trip = null;
            Trip roundTrip = null;
            if (billDetails.size() == 1) {
                trip = tripService.findTripById(billDetails.get(0).getTrip().getTripId());
            } else {
                trip = tripService.findTripById(billDetails.get(0).getTrip().getTripId());
                roundTrip = tripService.findTripById(billDetails.get(1).getTrip().getTripId());
            }
            float totalPrice = 0;
            for (Bill_Detail bill_Detail : billDetails) {
                totalPrice += bill_Detail.getFee();
            }

            CustomerDTO customerDTO = customerMapper.entityToDTO(customer);
            BillDTO billDTO = billMapper.entityToDTO(bill);
            TripDTO tripDTO = tripMapper.entityToDTO(trip);
            TripDTO roundTripDTO = tripMapper.entityToDTO(roundTrip);

            List<BillDetailDTO> billDetailDTOs = billDetails.stream()
                    .map(this::convertToBillDetailDTO)
                    .collect(Collectors.toList());

            response.put("totalPrice", totalPrice);
            response.put("bill", billDTO);
            response.put("customer", customerDTO);
            response.put("trip", tripDTO);
            response.put("roundTrip", roundTripDTO);
            response.put("billDetails", billDetailDTOs);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("errorMessage", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private BillDetailDTO convertToBillDetailDTO(Bill_Detail billDetail) {
        return billDetailMapper.entityToDTO(billDetail);
    }

    @PostMapping("/new-bill")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBill(@RequestBody Map<String, Object> billRequest) {
        Map<String, Object> response = new HashMap<>();
        int customerId;
        try {
            customerId = Integer.parseInt(billRequest.get("customerId").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid customer ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Customer ID must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int tripId;
        try {
            tripId = Integer.parseInt(billRequest.get("tripId").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid trip ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Trip ID must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int numberOfTickets;
        try {
            numberOfTickets = Integer.parseInt(billRequest.get("numberOfTickets").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid number of tickets.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Number of tickets must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int numberOfTicketsRoundTrip = Integer.parseInt(billRequest.get("numberOfTicketsRoundTrip").toString());

        String ticketType = null;
        try {
            ticketType = billRequest.get("ticketType").toString();
        } catch (Exception e) {
            response.put("errorMessage", "Ticket type must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String paymentMethod = null;
        try {
            paymentMethod = billRequest.get("paymentMethod").toString();
        } catch (Exception e) {
            response.put("errorMessage", "Payment method must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        float fee;
        try {
            fee = Float.parseFloat(billRequest.get("fee").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid fee.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Fee must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Integer roundTripId = billRequest.containsKey("roundTripId")
                ? Integer.parseInt(billRequest.get("roundTripId").toString())
                : null;

        ResponseEntity<Map<String, Object>> validationResponse = validateRequest(customerId, tripId,
                numberOfTickets, numberOfTicketsRoundTrip, ticketType, paymentMethod, fee, roundTripId);
        if (validationResponse != null) {
            return validationResponse;
        }

        Bill bill = createBill(customerId, paymentMethod);
        createBillDetails(bill, tripId, roundTripId, numberOfTickets, numberOfTicketsRoundTrip, fee, ticketType);

        response.put("message", "Bill created successfully.");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-bill/{billId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBill(@PathVariable String billId,
            @RequestBody Map<String, Object> billRequest) {
        Map<String, Object> response = new HashMap<>();
        int billIdConvert;
        try {
            billIdConvert = Integer.parseInt(billRequest.get("billId").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid bill ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Bill ID must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int customerId;
        try {
            customerId = Integer.parseInt(billRequest.get("customerId").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid customer ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Customer ID must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int tripId;
        try {
            tripId = Integer.parseInt(billRequest.get("tripId").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid trip ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Trip ID must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int numberOfTickets;
        try {
            numberOfTickets = Integer.parseInt(billRequest.get("numberOfTickets").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid number of tickets.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Number of tickets must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int numberOfTicketsRoundTrip = billRequest.containsKey("numberOfTicketsRoundTrip")
                ? Integer.parseInt(billRequest.get("numberOfTicketsRoundTrip").toString())
                : null;

        String ticketType;
        try {
            ticketType = billRequest.get("ticketType").toString();
        } catch (Exception e) {
            response.put("errorMessage", "Ticket type must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String paymentMethod;
        try {
            paymentMethod = billRequest.get("paymentMethod").toString();
        } catch (Exception e) {
            response.put("errorMessage", "Payment method must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        float fee;
        try {
            fee = Float.parseFloat(billRequest.get("fee").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid fee.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Fee must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        int roundTripId = billRequest.containsKey("roundTripId")
                ? Integer.parseInt(billRequest.get("roundTripId").toString())
                : null;

        ResponseEntity<Map<String, Object>> validationResponse = validateRequest(customerId, tripId,
                numberOfTickets, numberOfTicketsRoundTrip, ticketType, paymentMethod, fee, roundTripId);
        if (validationResponse != null) {
            return validationResponse;
        }

        Bill bill = billService.findById(billIdConvert);
        if (bill == null) {
            response.put("errorMessage", "Bill not found.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            bill.setCustomer(customerService.getCustomerById(customerId));
            bill.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        }
        billService.save(bill);
        billDetailService.deleteByBillId(bill);
        createBillDetails(bill, tripId, roundTripId, numberOfTickets, numberOfTicketsRoundTrip, fee, ticketType);

        response.put("message", "Bill updated successfully.");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-bill/")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBill(@RequestBody Map<String, Object> billRequest) {
        Map<String, Object> response = new HashMap<>();
        int billId;
        try {
            billId = Integer.parseInt(billRequest.get("billId").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid bill ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Bill ID must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Bill bill = billService.findById(billId);
        if (bill == null) {
            response.put("errorMessage", "Bill not found.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        billDetailService.deleteByBillId(bill);
        billService.delete(bill);
        response.put("message", "Bill deleted successfully.");
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> validateRequest(int customerId, int tripId, int numberOfTickets,
            int numberOfTicketsRoundTrip,
            String ticketType,
            String paymentMethod, float fee, Integer roundTripId) {
        Map<String, Object> response = new HashMap<>();

        if (customerService.getCustomerById(customerId) == null) {
            response.put("errorMessage", "Customer not found.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (tripService.findTripById(tripId) == null) {
            response.put("errorMessage", "Trip not found.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!tripService.findTripById(tripId).getStatus().equals(TripStatus.waiting)) {
            response.put("errorMessage", "Trip is not available for booking.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (numberOfTickets <= 0) {
            response.put("errorMessage", "Number of tickets must be greater than 0.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (!ticketType.equals("one_way_ticket") && !ticketType.equals("round_trip_ticket")) {
            response.put("errorMessage", "Ticket type must be one-way or round-trip.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (!paymentMethod.equals("cash") && !paymentMethod.equals("vnpay")) {
            response.put("errorMessage", "Payment method must be cash or credit card.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (fee <= 0) {
            response.put("errorMessage", "Fee must be greater than 0.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (ticketType.equals("round_trip_ticket") && roundTripId == null) {
            response.put("errorMessage", "Round trip ID must be provided for round-trip tickets.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (roundTripId != null && roundTripId == tripId) {
            response.put("errorMessage", "Round trip ID must be different from trip ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (roundTripId != null && !tripService.findTripById(roundTripId).getStatus().equals(TripStatus.waiting)) {
            response.put("errorMessage", "Round trip is not available for booking.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (roundTripId != null) {
            if (tripService.findTripById(tripId).getArrivalTime()
                    .isAfter(tripService.findTripById(roundTripId).getDepartureTime())) {
                response.put("errorMessage", "Departure time of round trip must be after arrival time of trip.");
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (tripService.findTripById(tripId).getArrivalTime()
                    .isAfter(tripService.findTripById(roundTripId).getArrivalTime())) {
                response.put("errorMessage", "Arrival time of round trip must be after arrival time of trip.");
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        return null;
    }

    private Bill createBill(int customerId, String paymentMethod) {
        Bill bill = new Bill();
        bill.setCustomer(customerService.getCustomerById(customerId));
        bill.setPaymentMethod(PaymentMethod.valueOf(paymentMethod));
        bill.setPaymentDate(LocalDateTime.now());
        billService.save(bill);
        return bill;
    }

    private void createBillDetails(Bill bill, int tripId, Integer roundTripId, int numberOfTickets,
            int numberOfTicketsRoundTrip, float fee,
            String ticketType) {
        Bill_Detail billDetail = new Bill_Detail();
        billDetail.setBill(bill);
        billDetail.setTrip(tripService.findTripById(tripId));
        billDetail.setNumberOfTicket(numberOfTickets);
        billDetail.setFee(fee);
        if (ticketType.equals("round_trip_ticket")) {
            billDetail.setTicketType(TicketType.round_trip_ticket);
        } else {
            billDetail.setTicketType(TicketType.one_way_ticket);
        }
        billDetailService.save(billDetail);

        if (ticketType.equals("round_trip_ticket") && roundTripId != null) {
            Bill_Detail roundTripBillDetail = new Bill_Detail();
            roundTripBillDetail.setBill(bill);
            roundTripBillDetail.setTrip(tripService.findTripById(roundTripId));
            roundTripBillDetail.setNumberOfTicket(numberOfTicketsRoundTrip);
            roundTripBillDetail.setFee(fee);
            roundTripBillDetail.setTicketType(TicketType.round_trip_ticket);
            billDetailService.save(roundTripBillDetail);
        }
    }

    @PostMapping("/send-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody Map<String, Object> billRequest) {
        Map<String, Object> response = new HashMap<>();
        int billId;

        try {
            billId = Integer.parseInt(billRequest.get("billId").toString());
        } catch (NumberFormatException e) {
            response.put("errorMessage", "Invalid bill ID.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("errorMessage", "Bill ID must be provided.");
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            Bill bill = billService.findById(billId);
            Customer customer = bill.getCustomer();
            String email = customer.getEmail();
            authenticationService.sendBillingDetail(email);
            response.put("message", "Email sent successfully.");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("errorMessage", "Failed to send email: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
