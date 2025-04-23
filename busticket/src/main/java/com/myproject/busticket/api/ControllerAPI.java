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
import com.myproject.busticket.dto.ControllerDTO;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Controller;
import com.myproject.busticket.services.ControllerService;
import com.myproject.busticket.services.RoleService;

@RestController
@RequestMapping("/api/controller")
public class ControllerAPI {
    @Autowired
    private ControllerService controllerService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/controllers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getControllers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Controller> controllerPages;

        if (searchValue != null && !searchValue.isEmpty()) {
            controllerPages = controllerService.searchControllersByNameOrEmail(pageable, searchValue);
        } else {
            controllerPages = controllerService.getAll(pageable);
        }

        List<ControllerDTO> controllerDTOs = controllerPages.getContent().stream()
                .map(controller -> {
                    Account account = controller.getAccount();
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
                    return new ControllerDTO(controller.getId(), accountDTO);
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("controllers", controllerDTOs);
        response.put("currentPage", page);
        response.put("totalPages", controllerPages.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
