package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "wallets")
public class Wallet {
    @Id
    private String id;
    
    @Field("userId")
    private String userId;
    
    @Field("balance")
    private double balance; // Số dư hiện tại
    
    @Field("currency")
    private String currency; // VND, USD, etc.
    
    @Field("status")
    private String status; // active, frozen, closed
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public Wallet() {
        this.balance = 0.0;
        this.currency = "VND";
        this.status = "active";
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
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    public boolean isActive() {
        return "active".equals(status);
    }
    
    public boolean hasSufficientBalance(double amount) {
        return balance >= amount;
    }
    
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void withdraw(double amount) {
        if (amount > 0 && hasSufficientBalance(amount)) {
            this.balance -= amount;
            this.updatedAt = LocalDateTime.now();
        }
    }
}

