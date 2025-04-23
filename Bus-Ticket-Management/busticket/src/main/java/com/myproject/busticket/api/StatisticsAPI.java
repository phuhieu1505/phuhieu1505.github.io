package com.myproject.busticket.api;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.dto.CustomerBookingDTO;
import com.myproject.busticket.dto.RouteTripCountDTO;
import com.myproject.busticket.services.BillService;
import com.myproject.busticket.services.CustomerService;
import com.myproject.busticket.services.TripService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsAPI {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TripService tripService;

    @Autowired
    private BillService billService;

    @GetMapping("/top-customer")
    public List<CustomerBookingDTO> getTopCustomerByBookings(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return customerService.getTopCustomerByBookingsInRange(startDate, endDate, 5);
    }

    @GetMapping("/trip-count-by-route")
    public ResponseEntity<List<RouteTripCountDTO>> countTripsByRouteCode(
            @RequestParam(required = false) String routeCode) {
        List<RouteTripCountDTO> result = tripService.getTripCountByRouteCode(routeCode);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusWeeks(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        Double totalRevenue = billService.getTotalRevenueInRange(startDate, endDate);
        Long transactionCount = billService.getTransactionCountInRange(startDate, endDate);
        Double averageRevenue = (transactionCount != null && transactionCount > 0) ? totalRevenue / transactionCount
                : 0.0;

        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue);
        response.put("transactionCount", transactionCount);
        response.put("averageRevenue", averageRevenue);

        return ResponseEntity.ok(response);
    }
}