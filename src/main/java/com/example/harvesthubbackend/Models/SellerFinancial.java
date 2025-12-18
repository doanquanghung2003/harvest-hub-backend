package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "seller_financials")
public class SellerFinancial {
    @Id
    private String id;
    
    // Thông tin seller
    private String sellerId;
    private String userId;
    
    // Tài khoản ngân hàng
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankBranch;
    
    // Tài chính
    private double totalRevenue; // Tổng doanh thu
    private double availableBalance; // Số dư khả dụng (có thể rút)
    private double pendingBalance; // Số dư đang chờ (đơn hàng chưa hoàn thành)
    private double withdrawnAmount; // Tổng số tiền đã rút
    private double commissionRate; // Tỷ lệ hoa hồng (%)
    private double totalCommission; // Tổng hoa hồng đã trừ
    
    // Rút tiền
    private double minWithdrawalAmount; // Số tiền tối thiểu để rút
    private int withdrawalDays; // Số ngày chờ để rút (sau khi đơn hàng hoàn thành)
    
    // Thống kê
    private int totalOrders;
    private int completedOrders;
    private int pendingOrders;
    private double averageOrderValue;
    
    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastWithdrawalAt;
    
    public SellerFinancial() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalRevenue = 0.0;
        this.availableBalance = 0.0;
        this.pendingBalance = 0.0;
        this.withdrawnAmount = 0.0;
        this.commissionRate = 5.0; // Mặc định 5%
        this.totalCommission = 0.0;
        this.minWithdrawalAmount = 100000; // 100k VND
        this.withdrawalDays = 7; // 7 ngày
        this.totalOrders = 0;
        this.completedOrders = 0;
        this.pendingOrders = 0;
        this.averageOrderValue = 0.0;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    
    public String getBankAccountName() { return bankAccountName; }
    public void setBankAccountName(String bankAccountName) { this.bankAccountName = bankAccountName; }
    
    public String getBankBranch() { return bankBranch; }
    public void setBankBranch(String bankBranch) { this.bankBranch = bankBranch; }
    
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { 
        this.totalRevenue = totalRevenue;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(double availableBalance) { 
        this.availableBalance = availableBalance;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getPendingBalance() { return pendingBalance; }
    public void setPendingBalance(double pendingBalance) { 
        this.pendingBalance = pendingBalance;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getWithdrawnAmount() { return withdrawnAmount; }
    public void setWithdrawnAmount(double withdrawnAmount) { 
        this.withdrawnAmount = withdrawnAmount;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }
    
    public double getTotalCommission() { return totalCommission; }
    public void setTotalCommission(double totalCommission) { this.totalCommission = totalCommission; }
    
    public double getMinWithdrawalAmount() { return minWithdrawalAmount; }
    public void setMinWithdrawalAmount(double minWithdrawalAmount) { this.minWithdrawalAmount = minWithdrawalAmount; }
    
    public int getWithdrawalDays() { return withdrawalDays; }
    public void setWithdrawalDays(int withdrawalDays) { this.withdrawalDays = withdrawalDays; }
    
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    
    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    
    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }
    
    public double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastWithdrawalAt() { return lastWithdrawalAt; }
    public void setLastWithdrawalAt(LocalDateTime lastWithdrawalAt) { this.lastWithdrawalAt = lastWithdrawalAt; }
}

