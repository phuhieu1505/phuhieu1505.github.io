package com.myproject.busticket.validations;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class CheckpointValidation {

    public static String normalizeVietnamese(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public static String validateCheckpointFields(String placeName, String address, String province, String city,
            String phone, String region) {
        String regex = "^[A-Za-z0-9\\s\\*\\,\\.\\-\\_\\(\\)\\[\\]\\{\\}\\@\\#\\$\\%\\^\\&\\!\\?\\+\\=\\:\\;\\'\\\"]*$";

        // Normalize the input fields
        placeName = normalizeVietnamese(placeName);
        address = normalizeVietnamese(address);
        province = normalizeVietnamese(province);
        city = normalizeVietnamese(city);
        phone = normalizeVietnamese(phone);
        region = normalizeVietnamese(region);

        if (!placeName.matches(regex) || !address.matches(regex) || !province.matches(regex) ||
                !city.matches(regex) || !phone.matches(regex) || !region.matches(regex)) {
            return "Invalid characters in input fields.";
        }
        return null;
    }
}