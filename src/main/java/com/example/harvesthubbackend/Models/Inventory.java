package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "inventories")
public class Inventory {
    @Id
    private String id;
    
    // Thông tin sản phẩm
    private String productId;
    private String sellerId;
    
    // Tồn kho
    private int currentStock; // Số lượng hiện tại
    private int reservedStock; // Số lượng đã được đặt (trong giỏ hàng, đơn hàng chưa xác nhận)
    private int availableStock; // Số lượng có thể bán = currentStock - reservedStock
    private int minStockLevel; // Mức tồn kho tối thiểu (cảnh báo)
    private int maxStockLevel; // Mức tồn kho tối đa
    
    // Đơn vị
    private String unit; // kg, gram, piece, box, etc.
    
    // Thông tin kho hàng
    private String warehouseId;
    private String warehouseName;
    private String location; // Vị trí trong kho
    
    // Trạng thái
    private String status; // in_stock, low_stock, out_of_stock, discontinued
    private boolean isActive;
    
    // Cảnh báo
    private boolean lowStockAlert; // Đã gửi cảnh báo hết hàng chưa
    private LocalDateTime lastRestockedAt; // Lần nhập hàng cuối
    private LocalDateTime lastSoldAt; // Lần bán cuối
    
    // Thống kê
    private int totalIn; // Tổng số lượng đã nhập
    private int totalOut; // Tổng số lượng đã xuất
    private int totalSold; // Tổng số lượng đã bán
    
    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Inventory() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "in_stock";
        this.isActive = true;
        this.currentStock = 0;
        this.reservedStock = 0;
        this.availableStock = 0;
        this.minStockLevel = 10;
        this.maxStockLevel = 1000;
        this.lowStockAlert = false;
        this.totalIn = 0;
        this.totalOut = 0;
        this.totalSold = 0;
    }
    
    // Tính toán available stock
    public void calculateAvailableStock() {
        this.availableStock = Math.max(0, this.currentStock - this.reservedStock);
        updateStatus();
    }
    
    // Cập nhật trạng thái dựa trên stock
    private void updateStatus() {
        if (this.availableStock <= 0) {
            this.status = "out_of_stock";
        } else if (this.availableStock <= this.minStockLevel) {
            this.status = "low_stock";
            this.lowStockAlert = true;
        } else {
            this.status = "in_stock";
            this.lowStockAlert = false;
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    
    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { 
        this.currentStock = currentStock;
        calculateAvailableStock();
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getReservedStock() { return reservedStock; }
    public void setReservedStock(int reservedStock) { 
        this.reservedStock = reservedStock;
        calculateAvailableStock();
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }
    
    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { 
        this.minStockLevel = minStockLevel;
        updateStatus();
    }
    
    public int getMaxStockLevel() { return maxStockLevel; }
    public void setMaxStockLevel(int maxStockLevel) { this.maxStockLevel = maxStockLevel; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public boolean isLowStockAlert() { return lowStockAlert; }
    public void setLowStockAlert(boolean lowStockAlert) { this.lowStockAlert = lowStockAlert; }
    
    public LocalDateTime getLastRestockedAt() { return lastRestockedAt; }
    public void setLastRestockedAt(LocalDateTime lastRestockedAt) { this.lastRestockedAt = lastRestockedAt; }
    
    public LocalDateTime getLastSoldAt() { return lastSoldAt; }
    public void setLastSoldAt(LocalDateTime lastSoldAt) { this.lastSoldAt = lastSoldAt; }
    
    public int getTotalIn() { return totalIn; }
    public void setTotalIn(int totalIn) { this.totalIn = totalIn; }
    
    public int getTotalOut() { return totalOut; }
    public void setTotalOut(int totalOut) { this.totalOut = totalOut; }
    
    public int getTotalSold() { return totalSold; }
    public void setTotalSold(int totalSold) { this.totalSold = totalSold; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

