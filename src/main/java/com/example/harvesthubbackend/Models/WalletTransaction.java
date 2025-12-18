package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "wallet_transactions")
public class WalletTransaction {
    @Id
    private String id;
    
    @Field("walletId")
    private String walletId;
    
    @Field("userId")
    private String userId;
    
    @Field("type")
    private String type; // deposit, withdraw, payment, refund
    
    @Field("amount")
    private double amount;
    
    @Field("balanceBefore")
    private double balanceBefore;
    
    @Field("balanceAfter")
    private double balanceAfter;
    
    @Field("status")
    private String status; // pending, completed, failed, cancelled
    
    @Field("description")
    private String description;
    
    @Field("referenceId")
    private String referenceId; // Order ID, Payment ID, etc.
    
    @Field("referenceType")
    private String referenceType; // order, payment, etc.
    
    @Field("paymentMethod")
    private String paymentMethod; // vnpay, banking, wallet
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public WalletTransaction() {
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getWalletId() {
        return walletId;
    }
    
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public double getBalanceBefore() {
        return balanceBefore;
    }
    
    public void setBalanceBefore(double balanceBefore) {
        this.balanceBefore = balanceBefore;
    }
    
    public double getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getReferenceId() {
        return referenceId;
    }
    
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
    
    public String getReferenceType() {
        return referenceType;
    }
    
    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    public boolean isDeposit() {
        return "deposit".equals(type);
    }
    
    public boolean isWithdraw() {
        return "withdraw".equals(type);
    }
    
    public boolean isPayment() {
        return "payment".equals(type);
    }
}

