package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "banners")
public class Banner {
    @Id
    private String id;
    
    @Field("title")
    private String title;
    
    @Field("description")
    private String description;
    
    @Field("image")
    private String image;
    
    @Field("link")
    private String link;
    
    @Field("linkType")
    private String linkType; // product, category, shop, external
    
    @Field("targetId")
    private String targetId; // ID of product/category/shop if linkType is not external
    
    @Field("position")
    private String position; // homepage_top, homepage_middle, category_top, etc.
    
    @Field("priority")
    private int priority; // Higher number = higher priority
    
    @Field("startDate")
    private LocalDateTime startDate;
    
    @Field("endDate")
    private LocalDateTime endDate;
    
    @Field("status")
    private String status; // active, inactive, expired
    
    @Field("clickCount")
    private int clickCount;
    
    @Field("viewCount")
    private int viewCount;
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public Banner() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "active";
        this.clickCount = 0;
        this.viewCount = 0;
        this.priority = 0;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    
    public String getLinkType() { return linkType; }
    public void setLinkType(String linkType) { this.linkType = linkType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getClickCount() { return clickCount; }
    public void setClickCount(int clickCount) { this.clickCount = clickCount; }
    
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status.equals("active") && 
               now.isAfter(startDate) && 
               now.isBefore(endDate);
    }
    
    public void incrementClickCount() {
        this.clickCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }
}
