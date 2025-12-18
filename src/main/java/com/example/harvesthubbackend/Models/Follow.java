package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "follows")
public class Follow {
    @Id
    private String id;
    
    @Field("userId")
    private String userId; // Người theo dõi
    
    @Field("shopId")
    private String shopId; // Shop được theo dõi
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    public Follow() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Follow(String userId, String shopId) {
        this.userId = userId;
        this.shopId = shopId;
        this.createdAt = LocalDateTime.now();
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
    
    public String getShopId() {
        return shopId;
    }
    
    public void setShopId(String shopId) {
        this.shopId = shopId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
