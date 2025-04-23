package com.myproject.busticket.services;

import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.CustomUserDetails;
import com.myproject.busticket.models.Role;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CustomerUserDetailsService implements UserDetailsService {
        private static final Logger logger = Logger.getLogger(CustomerUserDetailsService.class.getName());

        @Autowired
        private AccountService accountService;

        @Autowired
        private RolePermissionService rolePermissionService;

        @Override
        @Transactional
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                logger.info("Attempting to load user by username: " + username);

                Account account = accountService.getUserByEmail(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                Role role = account.getRole();
                List<GrantedAuthority> authorities = rolePermissionService.findByRole_RoleId(role.getRoleId()).stream()
                                .map(rolePermission -> new SimpleGrantedAuthority(
                                                rolePermission.getPermission().getPermissionName().toUpperCase()))
                                .collect(Collectors.toList());
                authorities.add(new SimpleGrantedAuthority(role.getRoleName().toUpperCase()));

                logger.info("Loaded user details for email: " + username + " with authorities: " + authorities);
                return new CustomUserDetails(account, authorities);
        }
}