package com.myproject.busticket.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyAccountModel {
    private String email;
    private String verificationCode;
}