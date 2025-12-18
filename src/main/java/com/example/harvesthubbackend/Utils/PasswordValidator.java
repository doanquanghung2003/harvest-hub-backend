package com.example.harvesthubbackend.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    // Patterns for password requirements
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return ValidationResult with isValid flag and list of errors
     */
    public static ValidationResult validate(String password) {
        List<String> errors = new ArrayList<>();
        
        if (password == null || password.isEmpty()) {
            errors.add("Mật khẩu không được để trống");
            return new ValidationResult(false, errors);
        }
        
        // Check length
        if (password.length() < MIN_LENGTH) {
            errors.add("Mật khẩu phải có ít nhất " + MIN_LENGTH + " ký tự");
        }
        
        if (password.length() > MAX_LENGTH) {
            errors.add("Mật khẩu không được vượt quá " + MAX_LENGTH + " ký tự");
        }
        
        // Check for uppercase letter
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            errors.add("Mật khẩu phải chứa ít nhất một chữ cái in hoa");
        }
        
        // Check for lowercase letter
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            errors.add("Mật khẩu phải chứa ít nhất một chữ cái thường");
        }
        
        // Check for digit
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            errors.add("Mật khẩu phải chứa ít nhất một chữ số");
        }
        
        // Check for special character
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            errors.add("Mật khẩu phải chứa ít nhất một ký tự đặc biệt (!@#$%^&*...)");
        }
        
        // Check for common weak passwords
        if (isCommonPassword(password)) {
            errors.add("Mật khẩu quá yếu, vui lòng chọn mật khẩu khác");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Check if password is a common weak password
     */
    private static boolean isCommonPassword(String password) {
        String[] commonPasswords = {
            "password", "12345678", "123456789", "qwerty", "abc123",
            "password123", "admin123", "letmein", "welcome", "monkey"
        };
        
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        
        public ValidationResult(boolean isValid, List<String> errors) {
            this.isValid = isValid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }
}

