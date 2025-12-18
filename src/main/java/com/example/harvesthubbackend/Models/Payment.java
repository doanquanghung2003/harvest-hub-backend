package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "payments")
public class Payment {
    @Id
    private String id;
    
    @Field("orderId")
    private String orderId;
    
    @Field("userId")
    private String userId;
    
    @Field("amount")
    private double amount;
    
    @Field("currency")
    private String currency;
    
    @Field("method")
    private String method; // cod, banking, vnpay, zalopay, wallet
    
    @Field("status")
    private String status; // pending, processing, completed, failed, cancelled, refunded
    
    @Field("gateway")
    private String gateway; // vnpay, zalopay, banking
    
    @Field("transactionId")
    private String transactionId; // ID từ gateway
    
    @Field("gatewayResponse")
    private Map<String, Object> gatewayResponse;
    
    @Field("paymentUrl")
    private String paymentUrl;
    
    @Field("qrCode")
    private String qrCode;
    
    @Field("bankCode")
    private String bankCode;
    
    @Field("bankName")
    private String bankName;
    
    @Field("accountNumber")
    private String accountNumber;
    
    @Field("accountName")
    private String accountName;
    
    @Field("description")
    private String description;
    
    @Field("fee")
    private double fee;
    
    @Field("refundAmount")
    private double refundAmount;
    
    @Field("refundReason")
    private String refundReason;
    
    @Field("refundedAt")
    private LocalDateTime refundedAt;
    
    @Field("expiredAt")
    private LocalDateTime expiredAt;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "pending";
        this.currency = "VND";
        this.fee = 0.0;
        this.refundAmount = 0.0;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public Map<String, Object> getGatewayResponse() { return gatewayResponse; }
    public void setGatewayResponse(Map<String, Object> gatewayResponse) { this.gatewayResponse = gatewayResponse; }
    
    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }
    
    public double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }
    
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
    
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    public boolean isFailed() {
        return "failed".equals(status);
    }
    
    public boolean isRefunded() {
        return "refunded".equals(status);
    }
    
    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }
    
    public double getTotalAmount() {
        return amount + fee;
    }
    
    public boolean canRefund() {
        return isCompleted() && refundAmount == 0;
    }
}
