package com.myproject.busticket.validations;

public class AccountValidation {

    public static boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$");
    }

    // Password must contain at least 8 characters, 1 uppercase, 1 lowercase, 1
    // special character, 1 number.
    public static boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    public static boolean isValidName(String name) {
        return name.matches("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$");
    }

    public static boolean isValidPhone(String phone) {
        return phone.matches("^(\\+?[0-9]{1,3})?([0-9]{10,14})$");
    }
}
