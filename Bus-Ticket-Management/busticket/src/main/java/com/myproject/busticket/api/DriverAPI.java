package com.myproject.busticket.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.dto.AccountDTO;
import com.myproject.busticket.dto.DriverDTO;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Driver;
import com.myproject.busticket.services.DriverService;
import com.myproject.busticket.services.RoleService;

@RestController
@RequestMapping("/api/driver")
public class DriverAPI {
    @Autowired
    private DriverService driverService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/drivers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDrivers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Driver> driverPages;
        if (searchValue != null && !searchValue.isEmpty()) {
            driverPages = driverService.searchDriversByNameOrEmail(pageable, searchValue);
        } else {
            driverPages = driverService.getAllDrivers(pageable);
        }

        List<DriverDTO> driverDTOs = driverPages.getContent().stream()
                .map(driver -> {
                    Account account = driver.getAccount();
                    AccountDTO accountDTO = new AccountDTO(
                            account.getId(),
                            account.getEmail(),
                            account.getPassword(),
                            account.getFullName(),
                            account.getPhone(),
                            roleService.getRoleById(account.getRole().getRoleId()),
                            account.getStatus(),
                            account.getVerificationCode(),
                            account.getVerificationExpiration(),
                            account.getLoginToken(),
                            account.getPasswordResetToken(),
                            account.getPasswordResetExpiration(),
                            account.isEnabled());
                    return new DriverDTO(driver.getDriverId(), accountDTO);
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("drivers", driverDTOs);
        response.put("currentPage", page);
        response.put("totalPages", driverPages.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
