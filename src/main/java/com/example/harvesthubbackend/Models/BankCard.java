package com.example.harvesthubbackend.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "bank_cards")
public class BankCard {
    @Id
    private String id;
    
    @Field("userId")
    private String userId;
    
    @Field("cardNumber")
    private String cardNumber; // Số thẻ (chỉ lưu 4 số cuối)
    
    @Field("cardNumberMasked")
    private String cardNumberMasked; // Số thẻ đã mask (ví dụ: **** **** **** 1234)
    
    @Field("cardHolderName")
    private String cardHolderName; // Tên chủ thẻ
    
    @Field("bankName")
    private String bankName; // Tên ngân hàng
    
    @Field("bankCode")
    private String bankCode; // Mã ngân hàng
    
    @Field("expiryMonth")
    private String expiryMonth; // Tháng hết hạn (MM)
    
    @Field("expiryYear")
    private String expiryYear; // Năm hết hạn (YYYY)
    
    @Field("cardType")
    private String cardType; // debit, credit, prepaid
    
    @Field("isDefault")
    private boolean isDefault; // Thẻ mặc định
    
    @Field("status")
    private String status; // active, inactive, expired
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public BankCard() {
        this.isDefault = false;
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
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getCardNumberMasked() {
        return cardNumberMasked;
    }
    
    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getBankCode() {
        return bankCode;
    }
    
    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }
    
    public String getExpiryMonth() {
        return expiryMonth;
    }
    
    public void setExpiryMonth(String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }
    
    public String getExpiryYear() {
        return expiryYear;
    }
    
    public void setExpiryYear(String expiryYear) {
        this.expiryYear = expiryYear;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    @JsonProperty("isDefault")
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
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
    
    public boolean isExpired() {
        if (expiryYear == null || expiryMonth == null) {
            return false;
        }
        try {
            int year = Integer.parseInt(expiryYear);
            int month = Integer.parseInt(expiryMonth);
            LocalDateTime expiryDate = LocalDateTime.of(year, month, 1, 0, 0);
            return LocalDateTime.now().isAfter(expiryDate);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public String getExpiryDateFormatted() {
        if (expiryMonth != null && expiryYear != null) {
            return expiryMonth + "/" + expiryYear;
        }
        return "";
    }
}

