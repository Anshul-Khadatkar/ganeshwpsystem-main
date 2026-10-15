package com.example.ganpativargani.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;
    private final TokenDenylist tokenDenylist;

    public JwtService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-hours:12}") long expirationHours,
            TokenDenylist tokenDenylist
    ) {
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set to a random string of at least 32 characters. " +
                    "Generate one with: openssl rand -base64 48");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationHours * 3600_000;
        this.tokenDenylist = tokenDenylist;
    }

    public String issueToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    /** Returns the username if the token is valid, unexpired, and not revoked; otherwise null. */
    public String validateAndGetUsername(String token) {
        try {
            if (tokenDenylist.isRevoked(token)) {
                return null;
            }
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return claims.getSubject();
        } catch (Exception ex) {
            return null;
        }
    }

    /** Revoke a token immediately so it cannot be reused until its natural expiry. */
    public void revokeToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            Date expiration = claims.getExpiration();
            Instant until = expiration != null ? expiration.toInstant() : Instant.now().plusMillis(expirationMillis);
            tokenDenylist.revoke(token, until);
        } catch (Exception ex) {
            // Still mark as revoked for a short window if parsing fails after logout attempt
            tokenDenylist.revoke(token, Instant.now().plusMillis(expirationMillis));
        }
    }
}
