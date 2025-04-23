package com.myproject.busticket.dto;

import com.myproject.busticket.enums.AccountStatus;
import com.myproject.busticket.models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private int id;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private Role role;
    private AccountStatus status;
    private String verificationCode;
    private LocalDateTime verificationExpiration;
    private String loginToken;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiration;
    private boolean enabled;
}
