package com.myproject.busticket.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Bill;
import com.myproject.busticket.models.Bill_Detail;
import com.myproject.busticket.models.Trip;

import java.util.List;

@Repository
public interface BillDetailRepository extends JpaRepository<Bill_Detail, Integer> {
    List<Bill_Detail> findByTrip(Trip trip);

    List<Bill_Detail> findByBill(Bill bill);

}
