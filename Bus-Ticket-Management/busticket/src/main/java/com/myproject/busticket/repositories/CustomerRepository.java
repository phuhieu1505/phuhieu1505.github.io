package com.myproject.busticket.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.myproject.busticket.dto.CustomerBookingDTO;
import com.myproject.busticket.models.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
        Customer findByEmail(String email);

        Boolean existsByEmail(String email);

        @Query("SELECT c FROM Customer c WHERE c.name LIKE %?1% OR c.email LIKE %?1% OR c.phone LIKE %?1%")
        Page<Customer> findByNameContainingOrEmailContainingOrPhoneContainingAllIgnoreCase(String searchValue,
                        String searchValue2, Pageable pageable);

        @Query("SELECT new com.myproject.busticket.dto.CustomerBookingDTO(c.email, COUNT(bk.bookingId)) " +
                        "FROM Customer c " +
                        "JOIN Booking bk ON c.customerId = bk.customer.customerId " +
                        "JOIN Bill_Detail bd ON bk.trip.tripId = bd.trip.tripId " +
                        "JOIN Bill b ON bd.bill.billId = b.billId " +
                        "WHERE b.paymentDate BETWEEN :startDate AND :endDate " +
                        "GROUP BY c.email " +
                        "ORDER BY COUNT(bk.bookingId) DESC")
        List<CustomerBookingDTO> findTopCustomerByBookingsInRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate, Pageable pageable);
}