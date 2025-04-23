package com.myproject.busticket.utilities;

import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.CustomUserDetails;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.logging.Logger;

@UtilityClass
public class SecurityUtil {
    private static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());

    public static Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            logger.info("Authentication object found: " + authentication);
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
                logger.info("CustomUserDetails object found: " + customUserDetails);
                return customUserDetails.getAccount();
            } else {
                logger.warning("Principal is not an instance of CustomUserDetails");
            }
        } else {
            logger.warning("Authentication object is null");
        }
        return null;
    }

    public static String getCurrentMail() {
        Account account = getCurrentAccount();
        return account != null ? account.getEmail() : null;
    }

    public static boolean isAuthenticated() {
        return getCurrentAccount() != null;
    }

    public static Collection<? extends GrantedAuthority> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            return customUserDetails.getAuthorities();
        } else {
            logger.warning("Authentication object is null or principal is not an instance of CustomUserDetails");
            return null;
        }
    }

    public static boolean hasRole(String role) {
        Collection<? extends GrantedAuthority> authorities = getCurrentUserRoles();
        logger.info("Checking role " + role);
        logger.info("Authorities: " + authorities);
        if (authorities != null) {
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(role)) {
                    logger.info("Role " + role + " found");
                    return true;
                }
            }
        } else {
            logger.warning("Authorities are null");
        }
        logger.info("Role " + role + " not found");
        return false;
    }

}