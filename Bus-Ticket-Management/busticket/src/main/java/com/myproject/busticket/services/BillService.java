package com.myproject.busticket.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.myproject.busticket.models.Bill;
import com.myproject.busticket.repositories.BillRepository;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    public Optional<Bill> findByBillId(Bill bill) {
        return billRepository.findById(bill.getBillId());
    }

    public Bill findById(int billId) {
        return billRepository.findById(billId).orElse(null);
    }

    public void delete(Bill bill) {
        billRepository.delete(bill);
    }

    public void deleteAllById(List<Integer> billIds) {
        billRepository.deleteAllById(billIds);
    }

    public Page<Bill> getAll(Pageable pageable) {
        return billRepository.findAll(pageable);
    }

    public void save(Bill bill) {
        billRepository.save(bill);
    }

    public Bill findByBillIdAndCustomer(int billId, int customerId) {
        return billRepository.findByBillIdAndCustomer(billId, customerId);
    }

    public Page<Bill> searchBills(Pageable pageable, String query, String startDate, String endDate) {
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null && !startDate.isEmpty()) {
            startDateTime = LocalDateTime.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            endDateTime = LocalDateTime.parse(endDate);
        }

        if (query == null || query.isEmpty()) {
            query = "";
        }

        if (startDateTime == null && endDateTime == null) {
            return billRepository.findByCustomerNameContainingOrCustomerEmailContaining(
                    query, pageable);
        } else if (startDateTime == null) {
            return billRepository.findByCustomerNameContainingOrCustomerEmailContainingAndPaymentDateBefore(
                    query, endDateTime, pageable);
        } else if (endDateTime == null) {
            return billRepository.findByCustomerNameContainingOrCustomerEmailContainingAndPaymentDateAfter(
                    query, startDateTime, pageable);
        } else {
            return billRepository.findByCustomerNameContainingOrCustomerEmailContainingAndPaymentDateBetween(
                    query, startDateTime, endDateTime, pageable);
        }
    }

    public Double getTotalRevenueInRange(LocalDateTime startDate, LocalDateTime endDate) {
        return billRepository.findTotalRevenueInRange(startDate, endDate);
    }

    public Long getTransactionCountInRange(LocalDateTime startDate, LocalDateTime endDate) {
        return billRepository.countTransactionsInRange(startDate, endDate);
    }
}