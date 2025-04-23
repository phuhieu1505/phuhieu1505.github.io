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
import com.myproject.busticket.dto.StaffDTO;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Staff;
import com.myproject.busticket.services.RoleService;
import com.myproject.busticket.services.StaffService;

@RestController
@RequestMapping("/api/staff")
public class StaffAPI {
    @Autowired
    private StaffService staffService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/staffs")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStaffs(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Staff> staffPages;

        if (searchValue != null && !searchValue.isEmpty()) {
            staffPages = staffService.searchStaffsByNameOrEmail(pageable, searchValue);
        } else {
            staffPages = staffService.getAllStaffs(pageable);
        }

        List<StaffDTO> staffDTOs = staffPages.getContent().stream()
                .map(staff -> {
                    Account account = staff.getAccount();
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
                    return new StaffDTO(staff.getStaff_id(), accountDTO);
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("staffs", staffDTOs);
        response.put("currentPage", page);
        response.put("totalPages", staffPages.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
