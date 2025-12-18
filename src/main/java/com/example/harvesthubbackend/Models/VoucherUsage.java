package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "voucher_usages")
public class VoucherUsage {
    @Id
    private String id;
    
    @Field("voucherId")
    private String voucherId;
    
    @Field("voucherCode")
    private String voucherCode;
    
    @Field("userId")
    private String userId;
    
    @Field("orderId")
    private String orderId;
    
    @Field("discountAmount")
    private double discountAmount;
    
    @Field("orderAmount")
    private double orderAmount;
    
    @Field("usedAt")
    private LocalDateTime usedAt;
    
    @Field("status")
    private String status; // used, refunded
    
    @Field("refundedAt")
    private LocalDateTime refundedAt;
    
    // Constructor
    public VoucherUsage() {
        this.usedAt = LocalDateTime.now();
        this.status = "used";
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    
    public double getOrderAmount() { return orderAmount; }
    public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
}

