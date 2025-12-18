package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "sellers")
public class Seller {
    @Id
    private String id;
    
    // Thông tin cơ bản
    private String userId; // Reference to User model
    private String businessName;
    private String businessType; // individual, company, cooperative
    private String businessLicense;
    private String taxCode;
    
    // Thông tin liên hệ
    private String contactPerson;
    private String phone;
    private String email;
    private String website;
    
    // Địa chỉ
    private String address;
    private String city;
    private String province;
    private String district;
    private String ward;
    private String postalCode;
    private double latitude;
    private double longitude;
    
    // Thông tin nông nghiệp
    private String farmType; // organic, conventional, mixed
    private List<String> certifications; // VietGAP, GlobalGAP, Organic, etc.
    private String farmingMethod;
    private String irrigationSystem;
    private String soilType;
    
    // Hình ảnh
    private String logo;
    private String coverImage;
    private List<String> farmImages;
    private List<String> certificateImages;
    
    // Thống kê
    private int totalProducts;
    private int activeProducts;
    private int totalOrders;
    private int completedOrders;
    private double totalRevenue;
    private double averageRating;
    private int reviewCount;
    
    // Trạng thái và xác minh
    private String status; // pending, active, suspended, banned
    private boolean isVerified;
    private boolean isFeatured;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    
    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActiveAt;
    
    // Mô tả và giới thiệu
    private String description;
    private String story;
    private List<String> specialties;
    private List<String> achievements;
    
    // Chính sách
    private String returnPolicy;
    private String shippingPolicy;
    private String warrantyPolicy;
    
    // Social media
    private String facebook;
    private String instagram;
    private String youtube;
    private String tiktok;
    
    public Seller() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.status = "pending";
        this.isVerified = false;
        this.isFeatured = false;
        this.totalProducts = 0;
        this.activeProducts = 0;
        this.totalOrders = 0;
        this.completedOrders = 0;
        this.totalRevenue = 0.0;
        this.averageRating = 0.0;
        this.reviewCount = 0;
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getFarmType() {
        return farmType;
    }

    public void setFarmType(String farmType) {
        this.farmType = farmType;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications;
    }

    public String getFarmingMethod() {
        return farmingMethod;
    }

    public void setFarmingMethod(String farmingMethod) {
        this.farmingMethod = farmingMethod;
    }

    public String getIrrigationSystem() {
        return irrigationSystem;
    }

    public void setIrrigationSystem(String irrigationSystem) {
        this.irrigationSystem = irrigationSystem;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public List<String> getFarmImages() {
        return farmImages;
    }

    public void setFarmImages(List<String> farmImages) {
        this.farmImages = farmImages;
    }

    public List<String> getCertificateImages() {
        return certificateImages;
    }

    public void setCertificateImages(List<String> certificateImages) {
        this.certificateImages = certificateImages;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(int activeProducts) {
        this.activeProducts = activeProducts;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(int completedOrders) {
        this.completedOrders = completedOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
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

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    public String getReturnPolicy() {
        return returnPolicy;
    }

    public void setReturnPolicy(String returnPolicy) {
        this.returnPolicy = returnPolicy;
    }

    public String getShippingPolicy() {
        return shippingPolicy;
    }

    public void setShippingPolicy(String shippingPolicy) {
        this.shippingPolicy = shippingPolicy;
    }

    public String getWarrantyPolicy() {
        return warrantyPolicy;
    }

    public void setWarrantyPolicy(String warrantyPolicy) {
        this.warrantyPolicy = warrantyPolicy;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }

    public String getTiktok() {
        return tiktok;
    }

    public void setTiktok(String tiktok) {
        this.tiktok = tiktok;
    }
}
