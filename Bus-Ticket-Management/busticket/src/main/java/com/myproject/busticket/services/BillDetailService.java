package com.myproject.busticket.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myproject.busticket.models.Bill;
import com.myproject.busticket.models.Bill_Detail;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.repositories.BillDetailRepository;

@Service
public class BillDetailService {
    @Autowired
    private BillDetailRepository billDetailRepository;

    public List<Bill_Detail> findByTrip(Trip trip) {
        return billDetailRepository.findByTrip(trip);
    }

    public void delete(Bill_Detail bill) {
        billDetailRepository.delete(bill);
    }

    public void deleteAll(List<Bill_Detail> billDetails) {
        billDetailRepository.deleteAll(billDetails);
    }

    public List<Bill_Detail> findByBillId(Bill bill) {
        return billDetailRepository.findByBill(bill);
    }

    public Bill findByBill(Bill bill) {
        return billDetailRepository.findByBill(bill).get(0).getBill();
    }

    public void save(Bill_Detail roundTripBillDetail) {
        billDetailRepository.save(roundTripBillDetail);
    }

    public void deleteByBillId(Bill bill) {
        List<Bill_Detail> billDetails = findByBillId(bill);
        deleteAll(billDetails);
    }
}
