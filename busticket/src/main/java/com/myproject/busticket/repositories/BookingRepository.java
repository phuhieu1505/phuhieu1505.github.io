package com.myproject.busticket.repositories;

import com.myproject.busticket.models.Booking;
import com.myproject.busticket.models.Customer;
import com.myproject.busticket.models.Trip;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByTrip(Trip trip);

    List<Booking> findByRoundTripId(String roundTripId);

    List<Booking> findByCustomer(Customer customer);

    @Query("SELECT bk FROM Bill_Detail bd " +
            "JOIN bd.bill b " +
            "JOIN Booking bk ON bk.trip.tripId = bd.trip.tripId " +
            "WHERE bk.customer.customerId = b.customer.customerId " +
            "AND bk.trip.tripId = bd.trip.tripId " +
            "AND bk.isRoundTrip = true " +
            "AND bk.roundTripId IS NOT NULL " +
            "AND EXISTS (SELECT 1 FROM Booking bk2 WHERE bk2.customer.customerId = bk.customer.customerId " +
            "AND bk2.roundTripId = bk.roundTripId AND bk2.bookingId != bk.bookingId)")
    List<Booking> findBookingDetailsByCustomerId(@Param("customerId") int customerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN b.customer c " +
            "JOIN b.trip t " +
            "JOIN t.route r " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchValue, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchValue, '%')) " +
            "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchValue, '%')) " +
            "OR LOWER(r.name) LIKE LOWER(CONCAT('%', :searchValue, '%')) " +
            "OR CAST(t.departureTime AS string) LIKE CONCAT('%', :searchValue, '%') " +
            "OR CAST(t.arrivalTime AS string) LIKE CONCAT('%', :searchValue, '%')")
    Page<Booking> searchBookings(@Param("searchValue") String searchValue, Pageable pageable);
}