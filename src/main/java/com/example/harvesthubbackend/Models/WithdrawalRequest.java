package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "withdrawal_requests")
public class WithdrawalRequest {
    @Id
    private String id;
    
    // Thông tin seller
    private String sellerId;
    private String sellerFinancialId;
    
    // Thông tin rút tiền
    private double amount;
    private String currency;
    private String status; // pending, processing, completed, rejected, cancelled
    
    // Thông tin ngân hàng
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankBranch;
    
    // Xử lý
    private String processedBy; // Admin ID
    private LocalDateTime processedAt;
    private String rejectionReason;
    private String transactionReference; // Mã giao dịch chuyển khoản
    
    // Ghi chú
    private String notes;
    
    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public WithdrawalRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "pending";
        this.currency = "VND";
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    
    public String getSellerFinancialId() { return sellerFinancialId; }
    public void setSellerFinancialId(String sellerFinancialId) { this.sellerFinancialId = sellerFinancialId; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    
    public String getBankAccountName() { return bankAccountName; }
    public void setBankAccountName(String bankAccountName) { this.bankAccountName = bankAccountName; }
    
    public String getBankBranch() { return bankBranch; }
    public void setBankBranch(String bankBranch) { this.bankBranch = bankBranch; }
    
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

