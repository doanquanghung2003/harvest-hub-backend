package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountLockoutService {
    
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    
    @Autowired
    private UserService userService;
    
    /**
     * Record a failed login attempt
     */
    public void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            // Lock account for LOCKOUT_DURATION_MINUTES
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            user.setAccountNonLocked(false);
        }
        
        userService.update(user.getId(), user);
    }
    
    /**
     * Reset failed login attempts on successful login
     */
    public void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setAccountNonLocked(true);
            userService.update(user.getId(), user);
        }
    }
    
    /**
     * Check if account is locked
     */
    public boolean isAccountLocked(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        
        // If lockout period has passed, unlock the account
        if (LocalDateTime.now().isAfter(user.getLockedUntil())) {
            unlockAccount(user);
            return false;
        }
        
        return true;
    }
    
    /**
     * Unlock account manually
     */
    public void unlockAccount(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setAccountNonLocked(true);
        userService.update(user.getId(), user);
    }
    
    /**
     * Get remaining lockout time in minutes
     */
    public long getRemainingLockoutMinutes(User user) {
        if (user.getLockedUntil() == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(user.getLockedUntil())) {
            return 0;
        }
        
        return java.time.Duration.between(now, user.getLockedUntil()).toMinutes();
    }
    
    /**
     * Get remaining attempts before lockout
     */
    public int getRemainingAttempts(User user) {
        return Math.max(0, MAX_FAILED_ATTEMPTS - user.getFailedLoginAttempts());
    }
}

