package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "shops")
public class Shop {
    @Id
    private String id;
    
    @Field("name")
    private String name;
    
    @Field("description")
    private String description;
    
    @Field("logo")
    private String logo;
    
    @Field("banner")
    private String banner;
    
    @Field("ownerId")
    private String ownerId;
    
    @Field("ownerName")
    private String ownerName;
    
    @Field("contactInfo")
    private ContactInfo contactInfo;
    
    @Field("address")
    private Address address;
    
    @Field("businessInfo")
    private BusinessInfo businessInfo;
    
    @Field("stats")
    private ShopStats stats;
    
    @Field("settings")
    private ShopSettings settings;
    
    @Field("status")
    private String status; // active, inactive, suspended
    
    @Field("createdAt")
    private LocalDateTime createdAt;
    
    @Field("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructor
    public Shop() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "active";
        this.stats = new ShopStats();
        this.settings = new ShopSettings();
    }
    
    // Inner classes
    public static class ContactInfo {
        private String phone;
        private String email;
        private String website;
        private String facebook;
        private String instagram;
        
        // Getters and Setters
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        public String getFacebook() { return facebook; }
        public void setFacebook(String facebook) { this.facebook = facebook; }
        public String getInstagram() { return instagram; }
        public void setInstagram(String instagram) { this.instagram = instagram; }
    }
    
    public static class Address {
        private String street;
        private String ward;
        private String district;
        private String city;
        private String province;
        private String country;
        private String postalCode;
        private double latitude;
        private double longitude;
        
        // Getters and Setters
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
    }
    
    public static class BusinessInfo {
        private String businessLicense;
        private String taxCode;
        private String businessType;
        private String businessCategory;
        private LocalDateTime establishedDate;
        private String bankAccount;
        private String bankName;
        
        // Getters and Setters
        public String getBusinessLicense() { return businessLicense; }
        public void setBusinessLicense(String businessLicense) { this.businessLicense = businessLicense; }
        public String getTaxCode() { return taxCode; }
        public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public String getBusinessCategory() { return businessCategory; }
        public void setBusinessCategory(String businessCategory) { this.businessCategory = businessCategory; }
        public LocalDateTime getEstablishedDate() { return establishedDate; }
        public void setEstablishedDate(LocalDateTime establishedDate) { this.establishedDate = establishedDate; }
        public String getBankAccount() { return bankAccount; }
        public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
    }
    
    public static class ShopStats {
        private int totalProducts;
        private int totalOrders;
        private int totalCustomers;
        private double totalRevenue;
        private double averageRating;
        private int totalReviews;
    private int followers;
    private int following;
    private double responseRate; // Tỉ lệ phản hồi (%)
    private String responseTime; // Thời gian phản hồi trung bình
    private String joinedAt; // Thời gian tham gia (format: "X tuần trước")
        
        public ShopStats() {
            this.totalProducts = 0;
            this.totalOrders = 0;
            this.totalCustomers = 0;
            this.totalRevenue = 0.0;
            this.averageRating = 0.0;
            this.totalReviews = 0;
            this.followers = 0;
            this.following = 0;
            this.responseRate = 0.0;
            this.responseTime = "Đang cập nhật";
            this.joinedAt = "Đang cập nhật";
        }
        
        // Getters and Setters
        public int getTotalProducts() { return totalProducts; }
        public void setTotalProducts(int totalProducts) { this.totalProducts = totalProducts; }
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        public int getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }
        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
        public int getTotalReviews() { return totalReviews; }
        public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
        public int getFollowers() { return followers; }
        public void setFollowers(int followers) { this.followers = followers; }
        public int getFollowing() { return following; }
        public void setFollowing(int following) { this.following = following; }
        public double getResponseRate() { return responseRate; }
        public void setResponseRate(double responseRate) { this.responseRate = responseRate; }
        public String getResponseTime() { return responseTime; }
        public void setResponseTime(String responseTime) { this.responseTime = responseTime; }
        public String getJoinedAt() { return joinedAt; }
        public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }
    }
    
    public static class ShopSettings {
        private boolean allowChat;
        private boolean allowReviews;
        private boolean autoAcceptOrders;
        private String currency;
        private String language;
        private Map<String, Object> customSettings;
        
        public ShopSettings() {
            this.allowChat = true;
            this.allowReviews = true;
            this.autoAcceptOrders = false;
            this.currency = "VND";
            this.language = "vi";
        }
        
        // Getters and Setters
        public boolean isAllowChat() { return allowChat; }
        public void setAllowChat(boolean allowChat) { this.allowChat = allowChat; }
        public boolean isAllowReviews() { return allowReviews; }
        public void setAllowReviews(boolean allowReviews) { this.allowReviews = allowReviews; }
        public boolean isAutoAcceptOrders() { return autoAcceptOrders; }
        public void setAutoAcceptOrders(boolean autoAcceptOrders) { this.autoAcceptOrders = autoAcceptOrders; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public Map<String, Object> getCustomSettings() { return customSettings; }
        public void setCustomSettings(Map<String, Object> customSettings) { this.customSettings = customSettings; }
    }
    
    // Getters and Setters for main class
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    
    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }
    
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public ContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfo contactInfo) { this.contactInfo = contactInfo; }
    
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    
    public BusinessInfo getBusinessInfo() { return businessInfo; }
    public void setBusinessInfo(BusinessInfo businessInfo) { this.businessInfo = businessInfo; }
    
    public ShopStats getStats() { return stats; }
    public void setStats(ShopStats stats) { this.stats = stats; }
    
    public ShopSettings getSettings() { return settings; }
    public void setSettings(ShopSettings settings) { this.settings = settings; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
