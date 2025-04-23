package com.myproject.busticket.models;

public record VNPayRequest(
        long amount,
        String orderId) {
}