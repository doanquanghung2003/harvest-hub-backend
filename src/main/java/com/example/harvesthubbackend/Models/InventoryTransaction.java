package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "inventory_transactions")
public class InventoryTransaction {
    @Id
    private String id;
    
    // Thông tin kho hàng
    private String inventoryId;
    private String productId;
    private String sellerId;
    
    // Loại giao dịch
    private String type; // in, out, adjustment, return, damage, expired
    private String reason; // purchase, sale, return, adjustment, damage, expired
    
    // Số lượng
    private int quantity; // Số lượng dương cho "in", số lượng âm cho "out"
    private int quantityBefore; // Số lượng trước giao dịch
    private int quantityAfter; // Số lượng sau giao dịch
    
    // Thông tin liên quan
    private String orderId; // Nếu liên quan đến đơn hàng
    private String referenceNumber; // Số tham chiếu (hóa đơn, phiếu nhập/xuất)
    
    // Ghi chú
    private String notes;
    private String createdBy; // User ID hoặc system
    
    // Thời gian
    private LocalDateTime createdAt;
    
    public InventoryTransaction() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getInventoryId() { return inventoryId; }
    public void setInventoryId(String inventoryId) { this.inventoryId = inventoryId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public int getQuantityBefore() { return quantityBefore; }
    public void setQuantityBefore(int quantityBefore) { this.quantityBefore = quantityBefore; }
    
    public int getQuantityAfter() { return quantityAfter; }
    public void setQuantityAfter(int quantityAfter) { this.quantityAfter = quantityAfter; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

