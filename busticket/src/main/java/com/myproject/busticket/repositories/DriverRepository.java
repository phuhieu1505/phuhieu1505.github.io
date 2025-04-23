package com.myproject.busticket.repositories;

import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Driver;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Integer> {
    @EntityGraph(attributePaths = { "account" })
    Optional<Driver> findById(int driverId);

    List<Driver> findByAccount(Account account);

    @Query("SELECT d FROM Driver d WHERE d.account.fullName LIKE %?1% OR d.account.email LIKE %?2%")
    Page<Driver> findByAccountFullNameContainingOrAccountEmailContainingAllIgnoreCase(String searchValue,
            String searchValue2, Pageable pageable);
}
