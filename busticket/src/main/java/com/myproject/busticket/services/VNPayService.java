package com.myproject.busticket.services;

import java.util.Map;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.myproject.busticket.config.VNPayConfiguration;
import com.myproject.busticket.responses.VNPayResponse;
import com.myproject.busticket.utilities.VNPayUtil;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class VNPayService {
    @Autowired
    private VNPayConfiguration vnPayConfiguration;

    // public VNPayResponse createVNPayPayment(long amount, String bankCode, String
    // orderID, HttpServletRequest request) {
    // long vnpAmount = amount * 100L;

    // Map<String, String> vnpParamsMap = vnPayConfiguration.getVNPayConfig();
    // vnpParamsMap.put("vnp_Amount", String.valueOf(vnpAmount));

    // if (bankCode != null && !bankCode.isEmpty()) {
    // vnpParamsMap.put("vnp_BankCode", bankCode);
    // }

    // vnpParamsMap.put("vnp_TxnRef", orderID);

    // vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

    // String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
    // String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
    // String vnpSecureHash =
    // VNPayUtil.hmacSHA512(vnPayConfiguration.getSecretKey(), hashData);
    // queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

    // String paymentUrl = vnPayConfiguration.getVnp_PayUrl() + "?" + queryUrl;
    // return new VNPayResponse("ok", "success", paymentUrl);
    // }

    public VNPayResponse createVNPayPayment(long amount, String bankCode, HttpServletRequest request) {
        long vnpAmount = amount * 100L;

        Map<String, String> vnpParamsMap = vnPayConfiguration.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf(vnpAmount));

        if (bankCode != null && !bankCode.isEmpty()) {
            vnpParamsMap.put("vnp_BankCode", bankCode);
        }

        //vnpParamsMap.put("vnp_TxnRef", orderID);
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        vnpParamsMap.put("vnp_OrderInfo",
                "Thanh toan don hang thoi gian: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        vnpParamsMap.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        vnpParamsMap.put("vnp_ExpireDate",
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis() + 15 * 60 * 1000))); // 15
                                                                                                                       // minutes
                                                                                                                       // expiry

        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfiguration.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        String paymentUrl = vnPayConfiguration.getVnp_PayUrl() + "?" + queryUrl;
        return new VNPayResponse("ok", "success", paymentUrl);
    }

    public boolean verifyVNPayPayment(Map<String, String> params, String secureHash) {
        Map<String, String> filteredParams = params.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("vnp_SecureHash"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfiguration.getSecretKey(),
                VNPayUtil.getPaymentURL(filteredParams, false));

        return calculatedHash.equals(secureHash);
    }
}
