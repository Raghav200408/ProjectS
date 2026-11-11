package com.project.ProjectS.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ForgotPasswordRateLimiter {
    private static final int MAX_REQUESTS = 5;
    private static final int MAX_KEYS = 10_000;
    private static final Duration WINDOW = Duration.ofHours(1);
    private final ConcurrentHashMap<String, Window> requests = new ConcurrentHashMap<>();

    public boolean allow(String email, String remoteAddress) {
        Instant now = Instant.now();
        if (requests.size() >= MAX_KEYS) {
            requests.entrySet().removeIf(entry -> entry.getValue().startedAt.plus(WINDOW).isBefore(now));
            if (requests.size() >= MAX_KEYS) return false;
        }
        String key = sha256(email.trim().toLowerCase() + "|" + remoteAddress);
        Window window = requests.compute(key, (ignored, current) -> {
            if (current == null || current.startedAt.plus(WINDOW).isBefore(now)) return new Window(now, 1);
            return new Window(current.startedAt, current.count + 1);
        });
        return window.count <= MAX_REQUESTS;
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private record Window(Instant startedAt, int count) {}
}
