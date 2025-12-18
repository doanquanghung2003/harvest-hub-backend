package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "shippings")
public class Shipping {
    @Id
    private String id;
    
    // Thông tin đơn hàng
    private String orderId;
    private String userId;
    private String sellerId;
    
    // Thông tin vận chuyển
    private String shippingMethod; // standard, express, same_day
    private String shippingProvider; // GHN, GHTK, ViettelPost, custom
    private String trackingNumber;
    private String trackingUrl;
    
    // Địa chỉ giao hàng
    private String recipientName;
    private String recipientPhone;
    private String address;
    private String ward;
    private String district;
    private String city;
    private String province;
    private String postalCode;
    private double latitude;
    private double longitude;
    
    // Phí vận chuyển
    private double shippingFee;
    private double insuranceFee;
    private double codFee; // Phí thu hộ (nếu có)
    private double totalFee;
    private String currency;
    
    // Trạng thái vận chuyển
    private String status; // pending, picked_up, in_transit, out_for_delivery, delivered, failed, returned
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    
    // Lịch sử tracking
    private List<TrackingEvent> trackingHistory;
    
    // Thông tin người giao hàng
    private String deliveryPersonName;
    private String deliveryPersonPhone;
    
    // Ghi chú
    private String notes;
    private String deliveryNotes;
    
    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Inner class cho tracking events
    public static class TrackingEvent {
        private String status;
        private String description;
        private String location;
        private LocalDateTime timestamp;
        
        public TrackingEvent() {}
        
        public TrackingEvent(String status, String description, String location) {
            this.status = status;
            this.description = description;
            this.location = location;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
    
    public Shipping() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "pending";
        this.currency = "VND";
        this.shippingFee = 0.0;
        this.insuranceFee = 0.0;
        this.codFee = 0.0;
        this.totalFee = 0.0;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    
    public String getShippingProvider() { return shippingProvider; }
    public void setShippingProvider(String shippingProvider) { this.shippingProvider = shippingProvider; }
    
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    
    public String getTrackingUrl() { return trackingUrl; }
    public void setTrackingUrl(String trackingUrl) { this.trackingUrl = trackingUrl; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }
    
    public double getInsuranceFee() { return insuranceFee; }
    public void setInsuranceFee(double insuranceFee) { this.insuranceFee = insuranceFee; }
    
    public double getCodFee() { return codFee; }
    public void setCodFee(double codFee) { this.codFee = codFee; }
    
    public double getTotalFee() { return totalFee; }
    public void setTotalFee(double totalFee) { this.totalFee = totalFee; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    
    public LocalDateTime getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(LocalDateTime actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
    
    public List<TrackingEvent> getTrackingHistory() { return trackingHistory; }
    public void setTrackingHistory(List<TrackingEvent> trackingHistory) { this.trackingHistory = trackingHistory; }
    
    public String getDeliveryPersonName() { return deliveryPersonName; }
    public void setDeliveryPersonName(String deliveryPersonName) { this.deliveryPersonName = deliveryPersonName; }
    
    public String getDeliveryPersonPhone() { return deliveryPersonPhone; }
    public void setDeliveryPersonPhone(String deliveryPersonPhone) { this.deliveryPersonPhone = deliveryPersonPhone; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

