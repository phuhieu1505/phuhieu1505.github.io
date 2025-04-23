package com.myproject.busticket.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Permission;
import com.myproject.busticket.models.Role;
import com.myproject.busticket.models.Role_Permission;
import com.myproject.busticket.services.AccountService;
import com.myproject.busticket.services.JwtService;
import com.myproject.busticket.services.PermissionService;
import com.myproject.busticket.services.RolePermissionService;
import com.myproject.busticket.services.RoleService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/protected-resources")
// NOTE: THIS IS FOR TESTING PURPOSES ONLY
public class ProtectedResourceAPI {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private PermissionService permissionService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/protected-resource")
    public Map<String, Object> getProtectedResource(HttpServletRequest request) {
        // Get the JWT token from the cookies
        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt != null) {
            String email = jwtService.extractUsername(jwt);
            Account account = accountService.getUserByEmail(email).orElse(null);
            long remainingTime = jwtService.getRemainingTime(jwt);
            // Create a response map
            Map<String, Object> response = new HashMap<>();
            response.put("message", "This is a protected resource.");
            response.put("email", account != null ? account.getEmail() : "Email not found");
            response.put("remainingTime", remainingTime);

            return response;
        } else {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles-permissions")
    public Map<String, Object> getRolesAndPermissions() {
        List<Role> roles = roleService.getAllRoles();
        List<Role_Permission> rolePermissions = rolePermissionService.getAll();
        List<Permission> permissions = permissionService.getAll();
        Map<String, Object> response = new HashMap<>();

        for (Role role : roles) {
            List<Role_Permission> rolePermissionList = rolePermissions.stream()
                    .filter(rolePermission -> rolePermission.getRole().getRoleId() == role.getRoleId())
                    .collect(Collectors.toList());

            List<Permission> permissionList = rolePermissionList.stream()
                    .map(rolePermission -> permissions.stream()
                            .filter(permission -> permission.getPermissionId() == rolePermission.getPermission()
                                    .getPermissionId())
                            .findFirst().orElse(null))
                    .collect(Collectors.toList());

            response.put(role.getRoleName(), permissionList);
        }

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role-permissions")
    public Map<String, Object> getPermissionsForRoles(@RequestParam List<String> roleNames) {
        List<Role> roles = roleService.getAllRoles();
        List<Role_Permission> rolePermissions = rolePermissionService.getAll();
        List<Permission> permissions = permissionService.getAll();
        Map<String, Object> response = new HashMap<>();

        for (String roleName : roleNames) {
            Role role = roles.stream()
                    .filter(r -> r.getRoleName().equals(roleName))
                    .findFirst()
                    .orElse(null);

            if (role != null) {
                List<Role_Permission> rolePermissionList = rolePermissions.stream()
                        .filter(rolePermission -> rolePermission.getRole().getRoleId() == role.getRoleId())
                        .collect(Collectors.toList());

                List<Permission> permissionList = rolePermissionList.stream()
                        .map(rolePermission -> permissions.stream()
                                .filter(permission -> permission.getPermissionId() == rolePermission.getPermission()
                                        .getPermissionId())
                                .findFirst().orElse(null))
                        .collect(Collectors.toList());

                response.put(role.getRoleName(), permissionList);
            } else {
                response.put(roleName, "Role not found");
            }
        }

        return response;
    }
}