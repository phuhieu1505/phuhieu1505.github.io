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

import com.myproject.busticket.models.Role;
import com.myproject.busticket.services.RoleService;

@RestController
@RequestMapping("/api/role")
public class RoleAPI {
    @Autowired
    private RoleService roleService;

    @GetMapping("/roles")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRoles(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Role> rolePage = roleService.getAll(pageable);

        List<Role> roles = rolePage.getContent().stream()
                .map(role -> new Role(role.getRoleId(), role.getRoleName()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("roles", roles);
        response.put("currentPage", page);
        response.put("totalPages", rolePage.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
