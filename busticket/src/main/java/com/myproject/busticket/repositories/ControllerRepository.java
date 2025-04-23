package com.myproject.busticket.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Controller;
import java.util.List;

@Repository
public interface ControllerRepository extends JpaRepository<Controller, Integer> {

    List<Controller> findByAccount(Account account);

    @Query("SELECT c FROM Controller c WHERE c.account.fullName LIKE %?1% OR c.account.email LIKE %?2%")
    Page<Controller> findByAccountFullNameContainingOrAccountEmailContainingAllIgnoreCase(String searchValue,
            String searchValue2, Pageable pageable);
}
