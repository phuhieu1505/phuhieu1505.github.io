package com.myproject.busticket.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.myproject.busticket.models.Account;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByPasswordResetToken(String passwordResetToken);

    @Query("SELECT u FROM Account u WHERE u.email LIKE %?1% OR u.fullName LIKE %?1% OR u.phone LIKE %?1%")
    Page<Account> findByEmailContainingOrFullNameContainingOrPhoneContaining(Pageable pageable, String searchValue);
}
