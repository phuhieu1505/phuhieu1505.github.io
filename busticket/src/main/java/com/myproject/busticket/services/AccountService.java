package com.myproject.busticket.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.myproject.busticket.models.Account;
import com.myproject.busticket.repositories.AccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository, EmailService emailService) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> getUserByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    public Account getById(int id) {
        return accountRepository.findById(id).orElse(null);
    }

    public Account save(Account user) {
        return accountRepository.save(user);
    }

    // public Optional<User> findUsersByRole(String role) {
    // return accountRepository.findByRole(role);
    // }

    public void delete(int accountId) {
        accountRepository.deleteById(accountId);
    }

    public List<Account> allAccounts() {
        List<Account> users = new ArrayList<>();
        accountRepository.findAll().forEach(users::add);
        return users;
    }

    public Page<Account> getAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    public Page<Account> searchAccountsByEmailAndNameAndPhone(Pageable pageable, String searchValue) {
        return accountRepository.findByEmailContainingOrFullNameContainingOrPhoneContaining(pageable, searchValue);
    }
}
