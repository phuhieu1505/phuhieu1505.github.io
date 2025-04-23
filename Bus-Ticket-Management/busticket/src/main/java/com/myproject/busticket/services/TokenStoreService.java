package com.myproject.busticket.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenStoreService {
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    public void storeToken(String email, String token) {
        activeTokens.put(email, token);
    }

    public String getToken(String email) {
        return activeTokens.get(email);
    }

    public void invalidateToken(String email) {
        activeTokens.remove(email);
    }

    public boolean isTokenActive(String email) {
        return activeTokens.containsKey(email);
    }
}