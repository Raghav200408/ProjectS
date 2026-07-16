package com.project.ProjectS.security.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Create Secret Key
     */
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

    }

    /**
     * Generate JWT
     */
    public String generateToken(String email, String role) {

        Date now = new Date();

        Date expiryDate =
                new Date(now.getTime() + expiration);

        return Jwts.builder()

                .subject(email)

                .claim("role", role)

                .issuedAt(now)

                .expiration(expiryDate)

                .signWith(getSigningKey())

                .compact();

    }

    /**
     * Extract Email
     */
    public String extractEmail(String token) {

        return extractClaims(token).getSubject();

    }

    /**
     * Extract Role
     */
    public String extractRole(String token) {

        return extractClaims(token)
                .get("role", String.class);

    }

    /**
     * Validate Token
     */
    public boolean isTokenValid(String token) {

        try {

            extractClaims(token);

            return true;

        } catch (Exception e) {

            return false;

        }

    }

    /**
     * Extract Claims
     */
    private Claims extractClaims(String token) {

        return Jwts.parser()

                .verifyWith(getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();

    }

}