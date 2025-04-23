package com.myproject.busticket.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.myproject.busticket.exceptions.AccountStatusException;
import com.myproject.busticket.exceptions.ModelNotFoundException;
import com.myproject.busticket.exceptions.TimeOutException;
import com.myproject.busticket.exceptions.ValidationException;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.LoginUserModel;
import com.myproject.busticket.models.RegisterUserModel;
import com.myproject.busticket.models.VerifyAccountModel;
import com.myproject.busticket.services.AuthenticationService;
import com.myproject.busticket.services.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RequestMapping("/auth")
@Controller
public class AuthenticationController {
    private static final Logger logger = Logger.getLogger(AuthenticationController.class.getName());

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/verify")
    public String showVerificationPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "verify";
    }

    @GetMapping("/forgot-password")
    public String requestPasswordResetPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @ResponseBody
    @PostMapping("/signup")
    public Map<String, Object> signUp(@RequestBody RegisterUserModel registerUserDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Account registerAccount = authenticationService.signUp(registerUserDto);
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("data", registerAccount);
            return response;
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> signIn(@RequestBody LoginUserModel loginUserDto,
            HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            Account authenticatedAccount = authenticationService.signIn(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedAccount);
            Cookie cookie = new Cookie("jwtToken", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            responseBody.put("success", true);
            responseBody.put("message", "Login successful");
            responseBody.put("data", authenticatedAccount);
            responseBody.put("token", jwtToken);
            return ResponseEntity.ok(responseBody);
        } catch (AccountStatusException e) {
            responseBody.put("success", false);
            responseBody.put("message", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
        } catch (ValidationException e) {
            responseBody.put("success", false);
            responseBody.put("message", "Authentication failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        } catch (Exception e) {
            responseBody.put("success", false);
            responseBody.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
        }
    }

    @ResponseBody
    @PostMapping("/logout")
    public Map<String, Object> signOut(@RequestBody Map<String, String> request, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Attempting to clear JWT token");

            // Invalidate the JWT token by setting its max age to 0
            Cookie cookie = new Cookie("jwtToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            httpResponse.addCookie(cookie);

            clearCurrentSession(httpRequest, httpResponse);

            logger.info("JWT token cleared and current session invalidated.");
            response.put("success", true);
            response.put("message", "User signed out successfully");
        } catch (Exception e) {
            logger.severe("An error occurred during signing out: " + e.getMessage());
            response.put("success", false);
            response.put("message", "An error occurred during signing out");
        }
        return response;
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAccount(@RequestBody VerifyAccountModel verifyAccountModel) {
        Map<String, Object> response = new HashMap<>();
        try {
            authenticationService.verifyAccount(verifyAccountModel);
            response.put("success", true);
            response.put("message", "Account verified successfully");
            return ResponseEntity.ok(response);
        } catch (ModelNotFoundException | ValidationException | TimeOutException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @ResponseBody
    @PostMapping("/forgot-password")
    public Map<String, Object> requestPasswordReset(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            authenticationService.requestPasswordReset(email);
            response.put("success", true);
            response.put("message", "Password reset link has been sent to your email");
            return response;
        } catch (ModelNotFoundException | AccountStatusException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }
    }

    @ResponseBody
    @PostMapping(value = "/reset-password", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword) {
        Map<String, Object> response = new HashMap<>();
        try {
            authenticationService.resetPassword(token, newPassword);
            String message = "Password reset successfully";
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/auth/login?message=" + message)
                    .body(response);
        } catch (ModelNotFoundException | ValidationException | AccountStatusException e) {
            String message = e.getMessage();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/auth/login?error=" + message)
                    .body(response);
        }
    }

    @ResponseBody
    @PostMapping("/resend")
    public ResponseEntity<Map<String, Object>> resendVerificationEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            authenticationService.resendVerificationCode(email);
            response.put("success", true);
            response.put("message", "Verification email has been resent");
            return ResponseEntity.ok(response);
        } catch (ModelNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @ResponseBody
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String currentPassword, @RequestParam String newPassword) {
        authenticationService.changePassword(currentPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    private void clearCurrentSession(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            logger.info("Invalidating session for user: " + auth.getName());
            new SecurityContextLogoutHandler().logout(request, response, auth);
            logger.info("Session invalidated and SecurityContext cleared.");
        } else {
            logger.info("No authentication found in SecurityContext.");
        }
    }
}