package com.example.ganpativargani.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory denylist of revoked JWTs until their natural expiry.
 * Single-instance deployments (one EC2 backend) are fully covered.
 */
@Component
public class TokenDenylist {

    private final ConcurrentHashMap<String, Instant> revokedUntil = new ConcurrentHashMap<>();

    public void revoke(String token, Instant expiresAt) {
        if (token == null || token.isBlank() || expiresAt == null) {
            return;
        }
        purgeExpired();
        revokedUntil.put(hash(token), expiresAt);
    }

    public boolean isRevoked(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Instant until = revokedUntil.get(hash(token));
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            revokedUntil.remove(hash(token), until);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, Instant>> it = revokedUntil.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Instant> entry = it.next();
            if (now.isAfter(entry.getValue())) {
                it.remove();
            }
        }
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
