package com.myproject.busticket.repositories;

import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Bus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface BusRepository extends JpaRepository<Bus, String> {
    Bus findByPlate(String plate);

    @Query("SELECT b FROM Bus b WHERE b.plate LIKE %?1% OR " +
            "CASE WHEN b.seatType = 'economy' THEN 'economy' " +
            "WHEN b.seatType = 'limousine' THEN 'limousine' " +
            "ELSE '' END LIKE %?1%")
    Page<Bus> searchBusesByPlateAndSeatType(Pageable pageable, String searchValue);
}
