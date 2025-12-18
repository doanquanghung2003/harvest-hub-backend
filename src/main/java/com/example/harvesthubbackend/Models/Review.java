package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    
    @Field("productId")
    private String productId;
    
    @Field("orderId")
    private String orderId;
    
    @Field("userId")
    private String userId;
    
    @Field("userName")
    private String userName;
    
    @Field("userAvatar")
    private String userAvatar;
    
    @Field("rating")
    private int rating; // 1-5 stars
    
    @Field("title")
    private String title;
    
    @Field("comment")
    private String comment;
    
    @Field("images")
    private List<String> images;
    
    @Field("videos")
    private List<String> videos;
    
    @Field("isVerified")
    private boolean isVerified; // Verified purchase
    
    @Field("isHelpful")
    private boolean isHelpful; // Marked as helpful by others
    
    @Field("helpfulCount")
    private int helpfulCount;
    
    @Field("reply")
    private ReviewReply reply;
    
    @Field("status")
    private String status; // pending, approved, rejected, hidden
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public Review() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "pending";
        this.isVerified = false;
        this.isHelpful = false;
        this.helpfulCount = 0;
    }
    
    // Inner class for ReviewReply
    public static class ReviewReply {
        private String id;
        private String userId;
        private String userName;
        private String userAvatar;
        private String comment;
        private LocalDateTime createdAt;
        private String status; // pending, approved, rejected
        
        public ReviewReply() {
            this.createdAt = LocalDateTime.now();
            this.status = "pending";
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        
        public String getUserAvatar() { return userAvatar; }
        public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    
    public List<String> getVideos() { return videos; }
    public void setVideos(List<String> videos) { this.videos = videos; }
    
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    
    public boolean isHelpful() { return isHelpful; }
    public void setHelpful(boolean helpful) { isHelpful = helpful; }
    
    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }
    
    public ReviewReply getReply() { return reply; }
    public void setReply(ReviewReply reply) { this.reply = reply; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isApproved() {
        return "approved".equals(status);
    }
    
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    public boolean isRejected() {
        return "rejected".equals(status);
    }
    
    public boolean isHidden() {
        return "hidden".equals(status);
    }
    
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
    
    public boolean hasVideos() {
        return videos != null && !videos.isEmpty();
    }
    
    public boolean hasReply() {
        return reply != null;
    }
    
    public void incrementHelpfulCount() {
        this.helpfulCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void decrementHelpfulCount() {
        if (this.helpfulCount > 0) {
            this.helpfulCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }
}
