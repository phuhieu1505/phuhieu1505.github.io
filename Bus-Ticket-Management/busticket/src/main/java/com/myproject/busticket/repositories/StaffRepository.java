package com.myproject.busticket.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Staff;
import java.util.Optional;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {
    Optional<Staff> findById(int staff_id);

    List<Staff> findByAccount(Account account);

    @Query("SELECT s FROM Staff s WHERE s.account.fullName LIKE %?1% OR s.account.email LIKE %?2%")
    Page<Staff> findByAccountFullNameContainingOrAccountEmailContainingAllIgnoreCase(String searchValue,
            String searchValue2, Pageable pageable);
}
