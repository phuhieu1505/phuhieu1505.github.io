package com.myproject.busticket.validations;

import java.time.LocalDateTime;

public class RouteValidation {
    public static boolean isValidCode(String code) {
        return code.matches("^[A-Z]+$");
    }

    public static boolean isValidName(String name) {
        return name.matches("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$");
    }

    public static boolean isValidDistance(int distance) {
        return distance > 0;
    }

    public static boolean isValidTime(LocalDateTime time) {
        return time.isAfter(LocalDateTime.now());
    }
}
