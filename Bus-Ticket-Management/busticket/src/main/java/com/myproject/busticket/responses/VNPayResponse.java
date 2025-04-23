package com.myproject.busticket.responses;

public record VNPayResponse(
        String code,
        String message,
        String paymentURL) {
}