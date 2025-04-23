package com.myproject.busticket.services;

import com.myproject.busticket.mapper.DriverMapper;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Driver;
import com.myproject.busticket.repositories.DriverRepository;

import java.util.List;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DriverService {
    @Autowired
    private DriverRepository driverRepository;

    DriverMapper driverMapper = Mappers.getMapper(DriverMapper.class);

    public Driver getDriverById(int driverId) {
        return driverRepository.findById(driverId).orElse(null);
    }

    public Page<Driver> getAllDrivers(Pageable pageable) {
        return driverRepository.findAll(pageable);
    }

    public List<Driver> getDriverByAccount(Account account) {
        return driverRepository.findByAccount(account);
    }

    public void deleteDriver(Driver driver) {
        driverRepository.delete(driver);
    }

    public Driver save(Driver driver) {
        return driverRepository.save(driver);
    }

    public Page<Driver> searchDriversByNameOrEmail(Pageable pageable, String searchValue) {
        return driverRepository.findByAccountFullNameContainingOrAccountEmailContainingAllIgnoreCase(searchValue,
                searchValue, pageable);
    }
}
