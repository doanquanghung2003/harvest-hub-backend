package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "carts")
public class Cart {
    @Id
    private String id;
    private String userId;
    private List<CartItem> items = new ArrayList<>();
    private double totalPrice;
    private long createdAt;
    private long updatedAt;
    
    // Voucher
    private String voucherCode; // Mã voucher đang áp dụng
    private double discountAmount; // Số tiền giảm từ voucher
    private double subtotal; // Tổng tiền trước giảm giá

    public Cart() {}

    public Cart(String id, String userId, List<CartItem> items, double totalPrice, long createdAt, long updatedAt) {
        this.id = id;
        this.userId = userId;
        if (items != null) {
            this.items = items;
        }
        this.totalPrice = totalPrice;
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

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
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
    
    // Getters and setters for voucher
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    
    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public static class CartItem {
        private String productId;
        private int quantity;
        private double price;

        public CartItem() {}

        public CartItem(String productId, int quantity, double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}


