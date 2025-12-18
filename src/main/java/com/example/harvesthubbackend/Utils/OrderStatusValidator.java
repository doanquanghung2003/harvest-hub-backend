package com.example.harvesthubbackend.Utils;

import java.util.*;

public class OrderStatusValidator {
    
    // Order status constants
    public static final String PENDING = "pending";
    public static final String PROCESSING = "processing";
    public static final String CONFIRMED = "confirmed";
    public static final String PACKED = "packed";
    public static final String SHIPPING = "shipping";
    public static final String DELIVERED = "delivered";
    public static final String CANCELLED = "cancelled";
    public static final String RETURNED = "returned";
    public static final String REFUNDED = "refunded";
    
    // Valid status transitions map
    private static final Map<String, Set<String>> VALID_TRANSITIONS = new HashMap<>();
    
    static {
        // From PENDING: can go to PROCESSING, CONFIRMED, or CANCELLED
        VALID_TRANSITIONS.put(PENDING, Set.of(PROCESSING, CONFIRMED, CANCELLED));
        
        // From PROCESSING: can go to CONFIRMED, PACKED, or CANCELLED
        VALID_TRANSITIONS.put(PROCESSING, Set.of(CONFIRMED, PACKED, CANCELLED));
        
        // From CONFIRMED: can go to PACKED or CANCELLED
        VALID_TRANSITIONS.put(CONFIRMED, Set.of(PACKED, CANCELLED));
        
        // From PACKED: can go to SHIPPING or CANCELLED
        VALID_TRANSITIONS.put(PACKED, Set.of(SHIPPING, CANCELLED));
        
        // From SHIPPING: can go to DELIVERED, RETURNED, or CANCELLED
        VALID_TRANSITIONS.put(SHIPPING, Set.of(DELIVERED, RETURNED, CANCELLED));
        
        // From DELIVERED: can go to RETURNED or REFUNDED (final states mostly)
        VALID_TRANSITIONS.put(DELIVERED, Set.of(RETURNED, REFUNDED));
        
        // From CANCELLED: cannot transition to any other state (final state)
        VALID_TRANSITIONS.put(CANCELLED, Set.of());
        
        // From RETURNED: can go to REFUNDED
        VALID_TRANSITIONS.put(RETURNED, Set.of(REFUNDED));
        
        // From REFUNDED: cannot transition to any other state (final state)
        VALID_TRANSITIONS.put(REFUNDED, Set.of());
    }
    
    /**
     * Check if status transition is valid
     * @param currentStatus Current order status
     * @param newStatus New status to transition to
     * @return ValidationResult with isValid flag and error message
     */
    public static ValidationResult validateTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || currentStatus.trim().isEmpty()) {
            return new ValidationResult(false, "Current status cannot be null or empty");
        }
        
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return new ValidationResult(false, "New status cannot be null or empty");
        }
        
        // Normalize status (lowercase)
        currentStatus = currentStatus.toLowerCase().trim();
        newStatus = newStatus.toLowerCase().trim();
        
        // If same status, allow (idempotent)
        if (currentStatus.equals(newStatus)) {
            return new ValidationResult(true, null);
        }
        
        // Check if current status exists in transitions map
        Set<String> allowedTransitions = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTransitions == null) {
            return new ValidationResult(false, "Unknown current status: " + currentStatus);
        }
        
        // Check if transition is allowed
        if (!allowedTransitions.contains(newStatus)) {
            String allowedStatuses = String.join(", ", allowedTransitions);
            return new ValidationResult(
                false, 
                String.format("Cannot transition from '%s' to '%s'. Allowed transitions: %s", 
                    currentStatus, newStatus, 
                    allowedStatuses.isEmpty() ? "none (final state)" : allowedStatuses)
            );
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Check if status is a final state (cannot transition further)
     */
    public static boolean isFinalState(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        status = status.toLowerCase().trim();
        Set<String> allowedTransitions = VALID_TRANSITIONS.get(status);
        return allowedTransitions != null && allowedTransitions.isEmpty();
    }
    
    /**
     * Check if status allows cancellation
     */
    public static boolean canCancel(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        status = status.toLowerCase().trim();
        Set<String> allowedTransitions = VALID_TRANSITIONS.get(status);
        return allowedTransitions != null && allowedTransitions.contains(CANCELLED);
    }
    
    /**
     * Get all valid statuses
     */
    public static Set<String> getAllStatuses() {
        return VALID_TRANSITIONS.keySet();
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

