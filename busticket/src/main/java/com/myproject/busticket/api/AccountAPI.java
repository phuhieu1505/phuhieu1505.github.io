package com.myproject.busticket.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.myproject.busticket.dto.AccountDTO;
import com.myproject.busticket.enums.AccountRole;
import com.myproject.busticket.enums.AccountStatus;
import com.myproject.busticket.models.Account;
import com.myproject.busticket.models.Controller;
import com.myproject.busticket.models.Driver;
import com.myproject.busticket.models.Role;
import com.myproject.busticket.models.Staff;
import com.myproject.busticket.models.Trip;
import com.myproject.busticket.services.AccountService;
import com.myproject.busticket.services.AuthenticationService;
import com.myproject.busticket.services.ControllerService;
import com.myproject.busticket.services.DriverService;
import com.myproject.busticket.services.JwtService;
import com.myproject.busticket.services.RoleService;
import com.myproject.busticket.services.StaffService;
import com.myproject.busticket.services.TokenStoreService;
import com.myproject.busticket.services.TripService;
import com.myproject.busticket.utilities.SecurityUtil;
import com.myproject.busticket.validations.AccountValidation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/account")
public class AccountAPI {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TripService tripService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ControllerService controllerService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenStoreService tokenStoreService;

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping("/me")
    @ResponseBody
    public ResponseEntity<Account> authenticatedAccount() {
        Account currentAccount = SecurityUtil.getCurrentAccount();
        if (currentAccount == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(currentAccount);
    }

    @GetMapping("/accounts")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String searchValue) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPages;

        if (searchValue != null && !searchValue.isEmpty()) {
            accountPages = accountService.searchAccountsByEmailAndNameAndPhone(pageable, searchValue);
        } else {
            accountPages = accountService.getAll(pageable);
        }

        List<AccountDTO> accountDTOs = accountPages.getContent().stream()
                .map(account -> new AccountDTO(account.getId(), account.getEmail(),
                        account.getPassword(),
                        account.getFullName(), account.getPhone(),
                        roleService.getRoleById(account.getRole().getRoleId()),
                        account.getStatus(),
                        account.getVerificationCode(), account.getVerificationExpiration(),
                        account.getLoginToken(),
                        account.getPasswordResetToken(), account.getPasswordResetExpiration(),
                        account.isEnabled()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accountDTOs);
        response.put("currentPage", page);
        response.put("totalPages", accountPages.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers-account")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCustomers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accountPages = accountService.getAll(pageable);

        List<AccountDTO> accountDTOs = accountPages.getContent().stream()
                .filter(account -> account.getRole().getRoleId() == 4)
                .map(account -> new AccountDTO(account.getId(), account.getEmail(),
                        account.getPassword(),
                        account.getFullName(), account.getPhone(),
                        roleService.getRoleById(account.getRole().getRoleId()),
                        account.getStatus(),
                        account.getVerificationCode(), account.getVerificationExpiration(),
                        account.getLoginToken(),
                        account.getPasswordResetToken(), account.getPasswordResetExpiration(),
                        account.isEnabled()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("accounts", accountDTOs);
        response.put("currentPage", page);
        response.put("totalPages", accountPages.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-user-id")
    public ResponseEntity<Integer> getCurrentUserId() {
        int userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(userId);
    }

    @GetMapping("/account-detail/{accountId}")
    public ResponseEntity<Map<String, Object>> getAccountDetails(@PathVariable int accountId) {
        Account account = accountService.getById(accountId);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }

        AccountDTO accountDTO = new AccountDTO(account.getId(), account.getEmail(),
                account.getPassword(),
                account.getFullName(), account.getPhone(),
                roleService.getRoleById(account.getRole().getRoleId()),
                account.getStatus(),
                account.getVerificationCode(), account.getVerificationExpiration(),
                account.getLoginToken(),
                account.getPasswordResetToken(), account.getPasswordResetExpiration(),
                account.isEnabled());

        Map<String, Object> response = new HashMap<>();
        response.put("account", accountDTO);

        return ResponseEntity.ok(response);
    }

    private void handleRoleSpecificLogic(Account account, String role) {
        if (AccountRole.driver.toString().equals(role)) {
            deleteControllerAndStaff(account);
            Driver driver = new Driver();
            driver.setAccount(account);
            driverService.save(driver);
        } else if (AccountRole.controller.toString().equals(role)) {
            deleteDriverAndStaff(account);
            Controller controller = new Controller();
            controller.setAccount(account);
            controllerService.save(controller);
        } else if (AccountRole.staff.toString().equals(role)) {
            deleteDriverAndController(account);
            Staff staff = new Staff();
            staff.setAccount(account);
            staffService.save(staff);
        }
    }

    private void deleteControllerAndStaff(Account account) {
        List<Controller> controllers = controllerService.getControllerByAccount(account);
        if (!controllers.isEmpty()) {
            for (Controller controller : controllers) {
                controllerService.deleteController(controller);
            }
        }

        List<Staff> staffs = staffService.getStaffByAccount(account);
        if (!staffs.isEmpty()) {
            for (Staff staff : staffs) {
                staffService.deleteStaff(staff);
            }
        }
    }

    private void deleteDriverAndStaff(Account account) {
        List<Driver> drivers = driverService.getDriverByAccount(account);
        if (!drivers.isEmpty()) {
            for (Driver driver : drivers) {
                driverService.deleteDriver(driver);
            }
        }

        List<Staff> staffs = staffService.getStaffByAccount(account);
        if (!staffs.isEmpty()) {
            for (Staff staff : staffs) {
                staffService.deleteStaff(staff);
            }
        }
    }

    private void deleteDriverAndController(Account account) {
        List<Driver> drivers = driverService.getDriverByAccount(account);
        if (!drivers.isEmpty()) {
            for (Driver driver : drivers) {
                driverService.deleteDriver(driver);
            }
        }

        List<Controller> controllers = controllerService.getControllerByAccount(account);
        if (!controllers.isEmpty()) {
            for (Controller controller : controllers) {
                controllerService.deleteController(controller);
            }
        }
    }

    @PostMapping("/new-account")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> newAccount(@RequestBody Map<String, Object> newAccountData) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate input data
            String email = (String) newAccountData.get("email");
            String role = (String) newAccountData.get("role");
            String fullName = (String) newAccountData.get("fullName");
            String phone = (String) newAccountData.get("phone");
            String status = (String) newAccountData.get("status");
            String password = (String) newAccountData.get("password");
            String confirmPassword = (String) newAccountData.get("confirmPassword");

            if (email == null || email.isEmpty()) {
                response.put("message", "Email is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!AccountValidation.isValidEmail(email)) {
                response.put("message", "Invalid email format.");
                return ResponseEntity.badRequest().body(response);
            }
            if (role == null || role.isEmpty()) {
                response.put("message", "Role is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!EnumSet.of(AccountRole.admin, AccountRole.customer, AccountRole.controller, AccountRole.driver,
                    AccountRole.staff).contains(AccountRole.valueOf(role))) {
                response.put("message", "Invalid role.");
                return ResponseEntity.badRequest().body(response);
            }
            if (fullName == null || fullName.isEmpty()) {
                response.put("message", "Fullname is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (phone == null || phone.isEmpty() || !AccountValidation.isValidPhone(phone)) {
                response.put("message", "Invalid phone number format.");
                return ResponseEntity.badRequest().body(response);
            }
            if (status == null || status.isEmpty()) {
                response.put("message", "Status is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (password == null || password.isEmpty()) {
                response.put("message", "Password is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (confirmPassword == null || confirmPassword.isEmpty()) {
                response.put("message", "Confirm password is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!password.equals(confirmPassword)) {
                response.put("message", "Passwords do not match.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!AccountValidation.isValidPassword(password)) {
                response.put("message",
                        "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
                return ResponseEntity.badRequest().body(response);
            }
            if (accountService.getUserByEmail(email).isPresent()) {
                response.put("message", "Email already in use.");
                return ResponseEntity.badRequest().body(response);
            }

            // Create new account
            Account newAccount = new Account();
            newAccount.setEmail(email);
            Role accountRole = roleService.getRoleByName(AccountRole.valueOf(role).name());
            if (accountRole == null) {
                response.put("message", "Role not found.");
                return ResponseEntity.badRequest().body(response);
            }
            newAccount.setRole(accountRole);
            newAccount.setFullName(fullName);
            newAccount.setPhone(phone);
            newAccount.setStatus(AccountStatus.valueOf(status));
            newAccount.setPassword(passwordEncoder.encode(password));
            newAccount.setEnabled(true);

            // Save the account first
            accountService.save(newAccount);
            handleRoleSpecificLogic(newAccount, role);
            response.put("message", "Account created successfully.");
            if (AccountRole.admin.toString().equals(role)) {
                response.put("warning", "You have created an account with admin privileges.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error creating account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/update-account")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCustomerAccount(@RequestBody Map<String, Object> updatedAccount,
            HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        try {
            Account currentAccount = SecurityUtil.getCurrentAccount();
            if (currentAccount == null) {
                response.put("success", false);
                response.put("message", "You are not logged in.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Validate input data
            String email = (String) updatedAccount.get("email");
            String fullName = (String) updatedAccount.get("fullName");
            String phone = (String) updatedAccount.get("phone");

            // check if anything changed, if not return:
            if (email.equals(currentAccount.getEmail()) && fullName.equals(currentAccount.getFullName())
                    && phone.equals(currentAccount.getPhone())) {
                response.put("success", false);
                response.put("message", "No changes detected.");
                return ResponseEntity.badRequest().body(response);
            }

            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!AccountValidation.isValidEmail(email)) {
                response.put("success", false);
                response.put("message", "Invalid email format.");
                return ResponseEntity.badRequest().body(response);
            }

            // check if email is already in use:
            if (!email.equals(currentAccount.getEmail()) && accountService.getUserByEmail(email).isPresent()) {
                response.put("message", "Email already in use.");
                return ResponseEntity.badRequest().body(response);
            }

            if (fullName == null || fullName.isEmpty()) {
                response.put("success", false);
                response.put("message", "Fullname is required.");
                return ResponseEntity.badRequest().body(response);
            }

            if (phone == null || phone.isEmpty() || !AccountValidation.isValidPhone(phone)) {
                response.put("success", false);
                response.put("message", "Invalid phone number format.");
                return ResponseEntity.badRequest().body(response);
            }

            // Update account details
            currentAccount.setEmail(email);
            currentAccount.setFullName(fullName);
            currentAccount.setPhone(phone);
            accountService.save(currentAccount);

            // Invalidate the current JWT token and issue a new one
            String newJwtToken = jwtService.generateToken(currentAccount);
            Cookie cookie = new Cookie("jwtToken", newJwtToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            httpResponse.addCookie(cookie);
            response.put("jwtToken", newJwtToken);
            response.put("success", true);
            response.put("message", "Account updated successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/update-account/{accountId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAccount(@PathVariable int accountId,
            @RequestBody Map<String, Object> updatedAccount, HttpServletResponse httpResponse) {
        Map<String, Object> response = new HashMap<>();
        try {
            Account account = accountService.getById(accountId);
            if (account == null) {
                response.put("message", "Account not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // check account role:
            boolean isAdmin = SecurityUtil.getCurrentUserRoles().stream()
                    .anyMatch(role -> role.getAuthority().equals(AccountRole.admin.toString().toUpperCase()));

            Account currentAccount = SecurityUtil.getCurrentAccount();
            if (isAdmin && account.getRole().getRoleName().equalsIgnoreCase(AccountRole.admin.toString())
                    && currentAccount.getId() != accountId) {
                response.put("message", "You can't update another admin account.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Validate input data
            String email = (String) updatedAccount.get("email");
            String role = (String) updatedAccount.get("role");
            String fullName = (String) updatedAccount.get("fullName");
            String phone = (String) updatedAccount.get("phone");
            String status = (String) updatedAccount.get("status");
            String password = (String) updatedAccount.get("password");
            String confirmPassword = (String) updatedAccount.get("confirmPassword");

            // check if anything changed, if not return:
            if (email.equals(account.getEmail()) && role.equals(account.getRole().getRoleName())
                    && fullName.equals(account.getFullName()) && phone.equals(account.getPhone())
                    && status.equals(account.getStatus().name()) && (password == null || password.isEmpty())) {
                response.put("message", "No changes detected.");
                return ResponseEntity.badRequest().body(response);
            }

            if (email == null || email.isEmpty()) {
                response.put("message", "Email is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!AccountValidation.isValidEmail(email)) {
                response.put("message", "Invalid email format.");
                return ResponseEntity.badRequest().body(response);
            }

            if (role == null || role.isEmpty()) {
                response.put("message", "Role is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (!EnumSet.of(AccountRole.admin, AccountRole.customer, AccountRole.controller, AccountRole.driver,
                    AccountRole.staff).contains(AccountRole.valueOf(role))) {
                response.put("message", "Invalid role.");
                return ResponseEntity.badRequest().body(response);
            }
            if (fullName == null || fullName.isEmpty()) {
                response.put("message", "Fullname is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (phone == null || phone.isEmpty() || !AccountValidation.isValidPhone(phone)) {
                response.put("message", "Invalid phone number format.");
                return ResponseEntity.badRequest().body(response);
            }
            if (status == null || status.isEmpty()) {
                response.put("message", "Status is required.");
                return ResponseEntity.badRequest().body(response);
            }
            if (password != null && !password.isEmpty()) {
                if (confirmPassword == null || confirmPassword.isEmpty()) {
                    response.put("message", "Confirm password is required.");
                    return ResponseEntity.badRequest().body(response);
                }
                if (!password.equals(confirmPassword)) {
                    response.put("message", "Passwords do not match.");
                    return ResponseEntity.badRequest().body(response);
                }
                if (!AccountValidation.isValidPassword(password)) {
                    response.put("message",
                            "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
                    return ResponseEntity.badRequest().body(response);
                }
                account.setPassword(passwordEncoder.encode(password));
            }

            // Check if the role is being changed
            boolean roleChanged = !account.getRole().getRoleName().equalsIgnoreCase(role);
            if (roleChanged) {
                // Check if the account is assigned to any trips
                if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.controller.name())) {
                    List<Controller> controllers = controllerService.getControllerByAccount(account);
                    if (!controllers.isEmpty()) {
                        List<Trip> trips = tripService.findTripsByController(controllers.get(0));
                        if (!trips.isEmpty()) {
                            response.put("message", "Controller is assigned to a trip and cannot be updated.");
                            return ResponseEntity.badRequest().body(response);
                        }
                    }
                } else if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.driver.name())) {
                    List<Driver> drivers = driverService.getDriverByAccount(account);
                    if (!drivers.isEmpty()) {
                        List<Trip> trips = tripService.findTripsByDriver(drivers.get(0));
                        if (!trips.isEmpty()) {
                            response.put("message", "Driver is assigned to a trip and cannot be updated.");
                            return ResponseEntity.badRequest().body(response);
                        }
                    }
                } else if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.staff.name())) {
                    List<Staff> staffs = staffService.getStaffByAccount(account);
                    if (!staffs.isEmpty()) {
                        List<Trip> trips = tripService.findTripsByStaff(staffs.get(0));
                        if (!trips.isEmpty()) {
                            response.put("message", "Staff is assigned to a trip and cannot be updated.");
                            return ResponseEntity.badRequest().body(response);
                        }
                    }
                }

                // Update the role
                Role accountRole = roleService.getRoleByName(AccountRole.valueOf(role).name());
                if (accountRole == null) {
                    response.put("message", "Role not found.");
                    return ResponseEntity.badRequest().body(response);
                }
                account.setRole(accountRole);

                // If changing to customer, delete existing records in driver, controller, and
                // staff tables
                if (AccountRole.customer.name().equalsIgnoreCase(role)) {
                    if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.driver.name())) {
                        List<Driver> drivers = driverService.getDriverByAccount(account);
                        for (Driver driver : drivers) {
                            driverService.deleteDriver(driver);
                        }
                    } else if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.controller.name())) {
                        List<Controller> controllers = controllerService.getControllerByAccount(account);
                        for (Controller controller : controllers) {
                            controllerService.deleteController(controller);
                        }
                    } else if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.staff.name())) {
                        List<Staff> staffs = staffService.getStaffByAccount(account);
                        for (Staff staff : staffs) {
                            staffService.deleteStaff(staff);
                        }
                    }
                }
            }

            account.setEmail(email);
            account.setFullName(fullName);
            account.setPhone(phone);
            account.setStatus(AccountStatus.valueOf(status));
            handleRoleSpecificLogic(account, role);
            accountService.save(account);

            if (currentAccount != null && currentAccount.getId() == accountId) {
                if (!currentAccount.getEmail().equals(email)) {
                    // Invalidate the current JWT token and issue a new one
                    String newJwtToken = jwtService.generateToken(account);
                    Cookie cookie = new Cookie("jwtToken", newJwtToken);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    cookie.setPath("/");
                    cookie.setMaxAge(24 * 60 * 60);
                    httpResponse.addCookie(cookie);
                    response.put("jwtToken", newJwtToken);

                    // Store the new token
                    tokenStoreService.storeToken(email, newJwtToken);
                }
            }

            response.put("message", "Account updated successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error updating account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/delete-account/{accountId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAccount(@PathVariable int accountId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Account account = accountService.getById(accountId);
            if (account == null) {
                response.put("message", "Account not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Account currentAccount = SecurityUtil.getCurrentAccount();
            if (currentAccount != null && currentAccount.getId() == accountId) {
                response.put("message", "You cannot delete your own account.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            boolean isAdmin = SecurityUtil.getCurrentUserRoles().stream()
                    .anyMatch(role -> role.getAuthority().equals(AccountRole.admin.toString().toUpperCase()));
            if (isAdmin && account.getRole().getRoleName().equalsIgnoreCase(AccountRole.admin.toString())) {
                response.put("message", "You can't delete another admin account.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Check if the role is controller, staff, or driver and ensure they are not
            // assigned to any trips
            if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.controller.name())) {
                List<Controller> controllers = controllerService.getControllerByAccount(account);
                if (!controllers.isEmpty()) {
                    List<Trip> trips = tripService.findTripsByController(controllers.get(0));
                    if (!trips.isEmpty()) {
                        response.put("message", "Controller is assigned to a trip and cannot be deleted.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }
            } else if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.driver.name())) {
                List<Driver> drivers = driverService.getDriverByAccount(account);
                if (!drivers.isEmpty()) {
                    List<Trip> trips = tripService.findTripsByDriver(drivers.get(0));
                    if (!trips.isEmpty()) {
                        response.put("message", "Driver is assigned to a trip and cannot be deleted.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }
            } else if (account.getRole().getRoleName().equalsIgnoreCase(AccountRole.staff.name())) {
                List<Staff> staffs = staffService.getStaffByAccount(account);
                if (!staffs.isEmpty()) {
                    List<Trip> trips = tripService.findTripsByStaff(staffs.get(0));
                    if (!trips.isEmpty()) {
                        response.put("message", "Staff is assigned to a trip and cannot be deleted.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                }
            }

            accountService.delete(accountId);
            response.put("message", "Account deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Error deleting account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
