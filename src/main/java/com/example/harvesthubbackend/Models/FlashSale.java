package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "flashsales")
public class FlashSale {
    @Id
    private String id;
    
    @Field("name")
    private String name;
    
    @Field("description")
    private String description;
    
    @Field("banner")
    private String banner;
    
    @Field("startTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime startTime;
    
    @Field("endTime")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime endTime;
    
    @Field("status")
    private String status; // upcoming, active, ended, cancelled
    
    @Field("products")
    private List<FlashSaleProduct> products;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public FlashSale() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "upcoming";
    }
    
    // Inner class for FlashSaleProduct
    public static class FlashSaleProduct {
        private String productId;
        private String productName;
        private String productImage;
        private double originalPrice;
        private double flashSalePrice;
        private int flashSaleStock;
        private int soldCount;
        private int maxQuantityPerUser;
        
        // Getters and Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        
        public double getOriginalPrice() { return originalPrice; }
        public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
        
        public double getFlashSalePrice() { return flashSalePrice; }
        public void setFlashSalePrice(double flashSalePrice) { this.flashSalePrice = flashSalePrice; }
        
        public int getFlashSaleStock() { return flashSaleStock; }
        public void setFlashSaleStock(int flashSaleStock) { this.flashSaleStock = flashSaleStock; }
        
        public int getSoldCount() { return soldCount; }
        public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
        
        public int getMaxQuantityPerUser() { return maxQuantityPerUser; }
        public void setMaxQuantityPerUser(int maxQuantityPerUser) { this.maxQuantityPerUser = maxQuantityPerUser; }
        
        public double getDiscountPercentage() {
            if (originalPrice <= 0) return 0;
            return ((originalPrice - flashSalePrice) / originalPrice) * 100;
        }
        
        public int getRemainingStock() {
            return flashSaleStock - soldCount;
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<FlashSaleProduct> getProducts() { return products; }
    public void setProducts(List<FlashSaleProduct> products) { this.products = products; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status.equals("active") && 
               now.isAfter(startTime) && 
               now.isBefore(endTime);
    }
    
    public boolean isUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return status.equals("upcoming") && now.isBefore(startTime);
    }
    
    public boolean isEnded() {
        LocalDateTime now = LocalDateTime.now();
        return status.equals("ended") || now.isAfter(endTime);
    }
    
    public long getRemainingTimeInSeconds() {
        if (isEnded()) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return java.time.Duration.between(now, startTime).getSeconds();
        } else if (now.isBefore(endTime)) {
            return java.time.Duration.between(now, endTime).getSeconds();
        }
        return 0;
    }
}
