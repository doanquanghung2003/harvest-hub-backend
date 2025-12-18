package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id;
    private String userId;
    private String token;
    private LocalDateTime expiresAt;
    private boolean used;

    public PasswordResetToken() {}

    public PasswordResetToken(String userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.used = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

