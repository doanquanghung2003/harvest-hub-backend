package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "vouchers")
public class Voucher {
    @Id
    private String id;
    
    @Field("code")
    private String code;
    
    @Field("name")
    private String name;
    
    @Field("description")
    private String description;
    
    @Field("type")
    private String type; // percentage, fixed_amount, free_shipping
    
    @Field("value")
    private double value; // percentage or fixed amount
    
    @Field("minOrderAmount")
    private double minOrderAmount;
    
    @Field("maxDiscountAmount")
    private double maxDiscountAmount;
    
    @Field("shopId")
    private String shopId; // null for platform-wide vouchers
    
    @Field("categoryIds")
    private List<String> categoryIds; // null for all categories
    
    @Field("productIds")
    private List<String> productIds; // null for all products
    
    @Field("userIds")
    private List<String> userIds; // null for all users
    
    @Field("usageLimit")
    private int usageLimit; // -1 for unlimited
    
    @Field("usedCount")
    private int usedCount;
    
    @Field("startDate")
    private LocalDateTime startDate;
    
    @Field("endDate")
    private LocalDateTime endDate;
    
    @Field("status")
    private String status; // active, inactive, expired
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("imageUrl")
    private String imageUrl; // Hình ảnh voucher
    
    @Field("maxUsagePerUser")
    private int maxUsagePerUser; // Số lần tối đa mỗi user có thể dùng (-1 = unlimited)
    
    @Field("isStackable")
    private boolean isStackable; // Có thể kết hợp với voucher khác không
    
    @Field("createdBy")
    private String createdBy; // User ID của người tạo (admin/seller)
    
    @Field("excludedProductIds")
    private List<String> excludedProductIds; // Sản phẩm không được áp dụng
    
    @Field("excludedCategoryIds")
    private List<String> excludedCategoryIds; // Danh mục không được áp dụng
    
    @Field("membershipType")
    private String membershipType; // VIP tier yêu cầu (null = tất cả)
    
    // Constructor
    public Voucher() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "active";
        this.usedCount = 0;
        this.usageLimit = -1; // unlimited by default
        this.maxUsagePerUser = -1; // unlimited by default
        this.isStackable = false; // not stackable by default
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    
    public double getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(double minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    
    public double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    
    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }
    
    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }
    
    public List<String> getProductIds() { return productIds; }
    public void setProductIds(List<String> productIds) { this.productIds = productIds; }
    
    public List<String> getUserIds() { return userIds; }
    public void setUserIds(List<String> userIds) { this.userIds = userIds; }
    
    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }
    
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public int getMaxUsagePerUser() { return maxUsagePerUser; }
    public void setMaxUsagePerUser(int maxUsagePerUser) { this.maxUsagePerUser = maxUsagePerUser; }
    
    public boolean isStackable() { return isStackable; }
    public void setStackable(boolean stackable) { isStackable = stackable; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public List<String> getExcludedProductIds() { return excludedProductIds; }
    public void setExcludedProductIds(List<String> excludedProductIds) { this.excludedProductIds = excludedProductIds; }
    
    public List<String> getExcludedCategoryIds() { return excludedCategoryIds; }
    public void setExcludedCategoryIds(List<String> excludedCategoryIds) { this.excludedCategoryIds = excludedCategoryIds; }
    
    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }
    
    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status.equals("active") && 
               now.isAfter(startDate) && 
               now.isBefore(endDate) &&
               (usageLimit == -1 || usedCount < usageLimit);
    }
    
    public double calculateDiscount(double orderAmount) {
        if (!isValid() || orderAmount < minOrderAmount) {
            return 0;
        }
        
        double discount = 0;
        if (type.equals("percentage")) {
            discount = orderAmount * (value / 100);
            if (maxDiscountAmount > 0 && discount > maxDiscountAmount) {
                discount = maxDiscountAmount;
            }
        } else if (type.equals("fixed_amount")) {
            discount = value;
        } else if (type.equals("free_shipping")) {
            // This would be handled separately in shipping calculation
            discount = 0;
        }
        
        return Math.min(discount, orderAmount);
    }
}
