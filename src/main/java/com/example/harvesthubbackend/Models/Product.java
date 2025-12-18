package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "products")
public class Product {
    @Id
    private String id;
    
    // Thông tin cơ bản
    @Field("name")
    private String name;
    
    @Field("description")
    private String description;
    
    @Field("shortDescription")
    private String shortDescription;
    
    @Field("category")
    private String category;
    
    @Field("tags")
    private List<String> tags;
    
    // Giá cả và khuyến mãi
    @Field("price")
    private Double price;
    
    @Field("originalPrice")
    private Double originalPrice;
    
    @Field("discountPercentage")
    private Integer discountPercentage;
    
    @Field("currency")
    private String currency;
    
    // Hình ảnh
    @Field("images")
    private List<String> images;
    
    // Ảnh chi tiết
    @Field("detailImages")
    private List<String> detailImages;
    
    @Field("mainImage")
    private String mainImage;
    
    @Field("thumbnail")
    private String thumbnail;
    
    // Thông tin người bán
    @Field("sellerId")
    private String sellerId;
    
    @Field("sellerName")
    private String sellerName;
    
    @Field("sellerLocation")
    private String sellerLocation;
    
    @Field("sellerAddress")
    private String sellerAddress;
    
    @Field("sellerPhone")
    private String sellerPhone;
    
    @Field("sellerEmail")
    private String sellerEmail;
    
    // Thông tin sản phẩm
    @Field("origin")
    private String origin;
    
    @Field("weight")
    private String weight;
    
    @Field("unit")
    private String unit;
    
    @Field("stock")
    private Integer stock;
    
    @Field("minOrderQuantity")
    private int minOrderQuantity;
    
    @Field("maxOrderQuantity")
    private int maxOrderQuantity;
    
    @Field("inStock")
    private boolean inStock;
    
    @Field("isOrganic")
    private boolean isOrganic;
    
    @Field("isFeatured")
    private boolean isFeatured;
    
    @Field("isBestSeller")
    private boolean isBestSeller;
    
    // Đánh giá và nhận xét
    @Field("rating")
    private double rating;
    
    @Field("reviewCount")
    private int reviewCount;
    
    @Field("reviews")
    private List<Review> reviews;
    
    // Thông tin bảo quản và vận chuyển
    @Field("storageInstructions")
    private String storageInstructions;
    
    @Field("expiryDate")
    private String expiryDate;
    
    @Field("shippingInfo")
    private String shippingInfo;
    
    @Field("shippingCost")
    private double shippingCost;
    
    @Field("estimatedDeliveryDays")
    private int estimatedDeliveryDays;
    
    // Thuộc tính động (specifications)
    @Field("specifications")
    private Map<String, String> specifications;
    
    // Trạng thái và thời gian
    @Field("status")
    private String status; // active, inactive, out_of_stock, deleted
    
    // Approval status
    @Field("approvalStatus")
    private String approvalStatus; // pending, approved, rejected
    
    // Approval information
    @Field("rejectionReason")
    private String rejectionReason; // Lý do từ chối sản phẩm
    
    @Field("approvedBy")
    private String approvedBy; // Admin ID đã duyệt
    
    @Field("approvedAt")
    private LocalDateTime approvedAt; // Thời điểm duyệt
    
    @Field("rejectedBy")
    private String rejectedBy; // Admin ID đã từ chối
    
    @Field("rejectedAt")
    private LocalDateTime rejectedAt; // Thời điểm từ chối
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    @Field("publishedAt")
    private LocalDateTime publishedAt;
    
    // Thống kê
    @Field("viewCount")
    private int viewCount;
    
    @Field("favoriteCount")
    private int favoriteCount;
    
    @Field("soldCount")
    private int soldCount;
    
    // Constructor
    public Product() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "active";
        this.approvalStatus = "pending"; // Mặc định chờ duyệt
        this.inStock = true;
        this.rating = 0.0;
        this.reviewCount = 0;
        this.viewCount = 0;
        this.favoriteCount = 0;
        this.soldCount = 0;
        this.currency = "VND";
        this.stock = 0; // Giá trị mặc định cho stock
        this.minOrderQuantity = 1; // Giá trị mặc định
        this.maxOrderQuantity = 999; // Giá trị mặc định
        this.shippingCost = 0.0; // Giá trị mặc định
        this.estimatedDeliveryDays = 3; // Giá trị mặc định
        
        // Debug logging
        System.out.println("Product constructor called");
        System.out.println("Created product with default values:");
        System.out.println("  - status: " + this.status);
        System.out.println("  - stock: " + this.stock);
        System.out.println("  - createdAt: " + this.createdAt);
        System.out.println("  - updatedAt: " + this.updatedAt);
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(Integer discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getDetailImages() {
        return detailImages;
    }

    public void setDetailImages(List<String> detailImages) {
        this.detailImages = detailImages;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerLocation() {
        return sellerLocation;
    }

    public void setSellerLocation(String sellerLocation) {
        this.sellerLocation = sellerLocation;
    }

    public String getSellerAddress() {
        return sellerAddress;
    }

    public void setSellerAddress(String sellerAddress) {
        this.sellerAddress = sellerAddress;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public int getMinOrderQuantity() {
        return minOrderQuantity;
    }

    public void setMinOrderQuantity(int minOrderQuantity) {
        this.minOrderQuantity = minOrderQuantity;
    }

    public int getMaxOrderQuantity() {
        return maxOrderQuantity;
    }

    public void setMaxOrderQuantity(int maxOrderQuantity) {
        this.maxOrderQuantity = maxOrderQuantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public boolean isOrganic() {
        return isOrganic;
    }

    public void setOrganic(boolean organic) {
        isOrganic = organic;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public boolean isBestSeller() {
        return isBestSeller;
    }

    public void setBestSeller(boolean bestSeller) {
        isBestSeller = bestSeller;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public String getStorageInstructions() {
        return storageInstructions;
    }

    public void setStorageInstructions(String storageInstructions) {
        this.storageInstructions = storageInstructions;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getShippingInfo() {
        return shippingInfo;
    }

    public void setShippingInfo(String shippingInfo) {
        this.shippingInfo = shippingInfo;
    }

    public double getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(double shippingCost) {
        this.shippingCost = shippingCost;
    }

    public int getEstimatedDeliveryDays() {
        return estimatedDeliveryDays;
    }

    public void setEstimatedDeliveryDays(int estimatedDeliveryDays) {
        this.estimatedDeliveryDays = estimatedDeliveryDays;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    // Getters and setters for approval status
    public String getApprovalStatus() {
        return approvalStatus != null ? approvalStatus : "pending";
    }
    
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getApprovedBy() {
        return approvedBy;
    }
    
    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
    
    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
    
    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
    
    public String getRejectedBy() {
        return rejectedBy;
    }
    
    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }
    
    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }
    
    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", shortDescription='" + shortDescription + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", originalPrice=" + originalPrice +
                ", stock=" + stock +
                ", status='" + status + '\'' +
                ", images=" + images +
                ", detailImages=" + detailImages +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    // Inner class for Review
    public static class Review {
        private String id;
        private String userId;
        private String userName;
        private String userAvatar;
        private double rating;
        private String comment;
        private LocalDateTime createdAt;
        private List<String> images;
        
        public Review() {
            this.createdAt = LocalDateTime.now();
        }
        
        // Getters and Setters for Review
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

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserAvatar() {
            return userAvatar;
        }

        public void setUserAvatar(String userAvatar) {
            this.userAvatar = userAvatar;
        }

        public double getRating() {
            return rating;
        }

        public void setRating(double rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }
    }
}