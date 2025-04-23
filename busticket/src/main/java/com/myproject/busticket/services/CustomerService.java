package com.myproject.busticket.services;

import com.myproject.busticket.dto.CustomerBookingDTO;
import com.myproject.busticket.models.Customer;
import com.myproject.busticket.repositories.CustomerRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    @Autowired
    private CustomerRepository customerRepository;

    public int findIDByEmail(String email) {
        return customerRepository.findByEmail(email).getCustomerId();
    }

    public boolean hasCustomer(String email) {
        return customerRepository.findByEmail(email) != null;
    }

    public Customer getCustomerById(int customerId) {
        return customerRepository.findById(customerId).orElse(null);
    }

    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    public Customer create(Customer customer) {
        return customerRepository.saveAndFlush(customer);
    }

    public Page<Customer> getAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public Page<Customer> searchCustomersByNameOrEmailOrPhone(Pageable pageable, String searchValue) {
        return customerRepository.findByNameContainingOrEmailContainingOrPhoneContainingAllIgnoreCase(searchValue,
                searchValue,
                pageable);
    }

    public List<CustomerBookingDTO> getTopCustomerByBookingsInRange(LocalDateTime startDate, LocalDateTime endDate,
            int limit) {
        logger.info("Fetching top customers by bookings from {} to {} with limit {}", startDate, endDate, limit);
        Pageable pageable = PageRequest.of(0, limit);
        List<CustomerBookingDTO> result = customerRepository.findTopCustomerByBookingsInRange(startDate, endDate,
                pageable);
        logger.info("Fetched {} top customers", result.size());
        for (CustomerBookingDTO row : result) {
            logger.info("Customer: {}, Bookings: {}", row.getEmail(), row.getNumberOfBookings());
        }
        return result;
    }
}
