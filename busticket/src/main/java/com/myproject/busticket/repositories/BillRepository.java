package com.myproject.busticket.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Integer> {

    @Query("SELECT b FROM Bill b WHERE b.billId = ?1 AND b.customer.customerId = ?2")
    Bill findByBillIdAndCustomer(int billId, int customerId);

    @Query("SELECT b FROM Bill b WHERE (b.customer.name LIKE %?1% OR b.customer.email LIKE %?1%)")
    Page<Bill> findByCustomerNameContainingOrCustomerEmailContaining(
            String query, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE (b.customer.name LIKE %?1% OR b.customer.email LIKE %?1%) AND " +
            "(?2 IS NULL OR b.paymentDate <= ?2)")
    Page<Bill> findByCustomerNameContainingOrCustomerEmailContainingAndPaymentDateBefore(
            String query, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE (b.customer.name LIKE %?1% OR b.customer.email LIKE %?1%) AND " +
            "(?2 IS NULL OR b.paymentDate >= ?2)")
    Page<Bill> findByCustomerNameContainingOrCustomerEmailContainingAndPaymentDateAfter(
            String query, LocalDateTime startDate, Pageable pageable);

    @Query("SELECT b FROM Bill b WHERE (b.customer.name LIKE %?1% OR b.customer.email LIKE %?1%) AND " +
            "(?2 IS NULL OR b.paymentDate >= ?2) AND " +
            "(?3 IS NULL OR b.paymentDate <= ?3)")
    Page<Bill> findByCustomerNameContainingOrCustomerEmailContainingAndPaymentDateBetween(
            String query, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT SUM(bd.fee) FROM Bill_Detail bd " +
            "JOIN bd.bill b " +
            "WHERE b.paymentDate BETWEEN :startDate AND :endDate")
    Double findTotalRevenueInRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Bill b " +
            "WHERE b.paymentDate BETWEEN :startDate AND :endDate")
    Long countTransactionsInRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}