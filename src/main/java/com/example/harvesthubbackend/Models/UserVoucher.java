package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "user_vouchers")
public class UserVoucher {
    @Id
    private String id;
    
    @Field("userId")
    private String userId;
    
    @Field("voucherId")
    private String voucherId;
    
    @Field("voucherCode")
    private String voucherCode;
    
    @Field("receivedAt")
    private LocalDateTime receivedAt;
    
    @Field("expiresAt")
    private LocalDateTime expiresAt;
    
    @Field("isUsed")
    private boolean isUsed;
    
    @Field("usedAt")
    private LocalDateTime usedAt;
    
    @Field("orderId")
    private String orderId; // Order đã sử dụng voucher này
    
    // Constructor
    public UserVoucher() {
        this.receivedAt = LocalDateTime.now();
        this.isUsed = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    // Helper methods
    public boolean isExpired() {
        if (expiresAt == null) return false;
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !isUsed && !isExpired();
    }
}

