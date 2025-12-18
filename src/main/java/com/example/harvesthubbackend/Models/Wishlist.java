package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "wishlists")
public class Wishlist {
    @Id
    private String id;
    private String userId;
    private List<String> productIds = new ArrayList<>();
    private long createdAt;
    private long updatedAt;

    public Wishlist() {}

    public Wishlist(String id, String userId, List<String> productIds, long createdAt, long updatedAt) {
        this.id = id;
        this.userId = userId;
        if (productIds != null) {
            this.productIds = productIds;
        }
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}


