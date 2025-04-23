package com.myproject.busticket.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.myproject.busticket.models.Bill;
import com.myproject.busticket.models.Bill_Detail;
import com.myproject.busticket.models.Customer;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.services.BillDetailService;
import com.myproject.busticket.services.BillService;
import com.myproject.busticket.services.CustomerService;
import com.myproject.busticket.services.TripService;

@Controller
public class BillController {
    @Autowired
    private BillService billService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private TripService tripService;

    @Autowired
    private BillDetailService billDetailService;

    @GetMapping("/easy-bus/bill-detail/{billId}")
    public String getBillDetails(@PathVariable int billId, Model model) {
        Bill bill = billService.findById(billId);
        if (bill == null) {
            model.addAttribute("errorMessage", "Bill not found.");
            return "redirect:/easy-bus/bill-management";
        }

        Customer customer = customerService.getCustomerById(bill.getCustomer().getCustomerId());
        if (customer == null) {
            model.addAttribute("errorMessage", "Customer not found.");
            return "redirect:/easy-bus/bill-management";
        }

        List<Bill_Detail> billDetails = billDetailService.findByBillId(bill);
        if (billDetails == null || billDetails.isEmpty()) {
            model.addAttribute("errorMessage", "Bill details not found.");
            return "redirect:/easy-bus/bill-management";
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

        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("bill", bill);
        model.addAttribute("customer", customer);
        model.addAttribute("trip", trip);
        model.addAttribute("roundTrip", roundTrip);
        model.addAttribute("roundTripId", roundTrip != null ? roundTrip.getTripId() : null);
        // model.addAttribute("booking", booking);
        model.addAttribute("billDetails", billDetails);
        return "bill-detail";
    }

    @GetMapping("/easy-bus/new-bill")
    public String newBill(Model model) {
        model.addAttribute("bill", new Bill());
        return "new-bill";
    }

    @PostMapping("/easy-bus/new-bill")
    public String saveBill(Bill bill) {
        billService.save(bill);
        return "redirect:/easy-bus/bill-management";
    }

    @GetMapping("/easy-bus/update-bill/{billId}")
    public String updateBill(@PathVariable int billId, Model model) {
        Bill bill = billService.findById(billId);
        if (bill == null) {
            model.addAttribute("errorMessage", "Bill not found.");
            return "redirect:/easy-bus/bill-management";
        }

        Customer customer = customerService.getCustomerById(bill.getCustomer().getCustomerId());
        if (customer == null) {
            model.addAttribute("errorMessage", "Customer not found.");
            return "redirect:/easy-bus/bill-management";
        }

        List<Bill_Detail> billDetails = billDetailService.findByBillId(bill);
        if (billDetails == null || billDetails.isEmpty()) {
            model.addAttribute("errorMessage", "Bill details not found.");
            return "redirect:/easy-bus/bill-management";
        }

        Trip trip = null;
        Trip roundTrip = null;
        if (billDetails.size() == 1) {
            trip = tripService.findTripById(billDetails.get(0).getTrip().getTripId());
        } else {
            trip = tripService.findTripById(billDetails.get(0).getTrip().getTripId());
            roundTrip = tripService.findTripById(billDetails.get(1).getTrip().getTripId());
        }

        model.addAttribute("bill", bill);
        model.addAttribute("customer", customer);
        model.addAttribute("trip", trip);
        model.addAttribute("roundTrip", roundTrip);
        model.addAttribute("billDetails", billDetails);

        return "update-bill";
    }

    @PostMapping("/easy-bus/update-bill/{billId}")
    public String updateBill(@PathVariable int billId, Bill bill) {
        billService.save(bill);
        return "redirect:/easy-bus/bill-management";
    }
}
