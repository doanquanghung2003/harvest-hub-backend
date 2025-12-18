package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String userId;
    private String status; // pending, processing, shipping, delivered, cancelled
    private double totalPrice;
    private long createdAt;
    private long updatedAt;

    // Shipping & payment
    private String paymentMethod; // cod, banking, vnpay, wallet
    private String paymentStatus; // unpaid, paid, failed, refunded
    private double shippingFee;
    private String shippingAddress; // simple string for now
    
    // Voucher
    private String voucherCode; // Mã voucher đã áp dụng
    private String voucherId; // ID voucher đã áp dụng
    private double discountAmount; // Số tiền giảm từ voucher
    private double subtotal; // Tổng tiền trước giảm giá
    
    // Cancellation & return
    private String cancellationReason; // Lý do hủy đơn hàng
    private String cancelledBy; // User ID của người hủy (buyer, seller, admin)
    private Long cancelledAt; // Timestamp khi hủy
    private String returnReason; // Lý do trả hàng
    private String refundReason; // Lý do hoàn tiền

    // Snapshot items
    private java.util.List<OrderItem> items;

    // Constructors
    public Order() {
        this.status = "processing";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.paymentStatus = "unpaid";
        this.shippingFee = 0;
        this.items = new java.util.ArrayList<>();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public java.util.List<OrderItem> getItems() { return items; }
    public void setItems(java.util.List<OrderItem> items) { this.items = items; }
    
    // Getters and setters for cancellation & return
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    
    public Long getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(Long cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    
    // Getters and setters for voucher
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    
    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public static class OrderItem {
        private String productId;
        private String nameSnapshot;
        private String imageSnapshot;
        private int quantity;
        private double unitPrice;
        private boolean reviewed;
        private String reviewId;
        private Long reviewedAt;

        public OrderItem() {}

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public String getNameSnapshot() { return nameSnapshot; }
        public void setNameSnapshot(String nameSnapshot) { this.nameSnapshot = nameSnapshot; }
        public String getImageSnapshot() { return imageSnapshot; }
        public void setImageSnapshot(String imageSnapshot) { this.imageSnapshot = imageSnapshot; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
        public boolean isReviewed() { return reviewed; }
        public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }
        public String getReviewId() { return reviewId; }
        public void setReviewId(String reviewId) { this.reviewId = reviewId; }
        public Long getReviewedAt() { return reviewedAt; }
        public void setReviewedAt(Long reviewedAt) { this.reviewedAt = reviewedAt; }
    }
}