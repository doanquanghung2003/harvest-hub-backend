package com.example.harvesthubbackend.Models;

import java.util.List;

public class ChatResponse {
    private String text;
    private List<ProductInfo> products;
    
    public ChatResponse() {
    }
    
    public ChatResponse(String text) {
        this.text = text;
    }
    
    public ChatResponse(String text, List<ProductInfo> products) {
        this.text = text;
        this.products = products;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public List<ProductInfo> getProducts() {
        return products;
    }
    
    public void setProducts(List<ProductInfo> products) {
        this.products = products;
    }
    
    // Inner class để chứa thông tin sản phẩm cho chat
    public static class ProductInfo {
        private String id;
        private String name;
        private String image;
        private Double price;
        private Integer stock;
        private String category;
        private String link;
        
        public ProductInfo() {
        }
        
        public ProductInfo(String id, String name, String image, Double price, Integer stock, String category) {
            this.id = id;
            this.name = name;
            this.image = image;
            this.price = price;
            this.stock = stock;
            this.category = category;
            this.link = "/product/" + id; // Tạo link đến trang chi tiết
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
            this.link = "/product/" + id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getImage() {
            return image;
        }
        
        public void setImage(String image) {
            this.image = image;
        }
        
        public Double getPrice() {
            return price;
        }
        
        public void setPrice(Double price) {
            this.price = price;
        }
        
        public Integer getStock() {
            return stock;
        }
        
        public void setStock(Integer stock) {
            this.stock = stock;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getLink() {
            return link;
        }
        
        public void setLink(String link) {
            this.link = link;
        }
    }
}

