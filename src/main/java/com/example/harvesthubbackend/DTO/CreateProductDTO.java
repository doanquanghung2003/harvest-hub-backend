package com.example.harvesthubbackend.DTO;

import jakarta.validation.constraints.*;
import java.util.List;

public class CreateProductDTO {
    @NotBlank(message = "Tên sản phẩm là bắt buộc")
    @Size(min = 3, max = 200, message = "Tên sản phẩm phải từ 3 đến 200 ký tự")
    private String name;
    
    @NotBlank(message = "Mô tả sản phẩm là bắt buộc")
    @Size(min = 10, max = 5000, message = "Mô tả sản phẩm phải từ 10 đến 5000 ký tự")
    private String description;
    
    @Size(max = 500, message = "Mô tả ngắn không được vượt quá 500 ký tự")
    private String shortDescription;
    
    @NotBlank(message = "Danh mục sản phẩm là bắt buộc")
    private String category;
    
    @NotNull(message = "Giá sản phẩm là bắt buộc")
    @DecimalMin(value = "0.01", message = "Giá sản phẩm phải lớn hơn 0")
    @DecimalMax(value = "999999999.99", message = "Giá sản phẩm quá lớn")
    private Double price;
    
    @DecimalMin(value = "0.01", message = "Giá gốc phải lớn hơn 0")
    private Double originalPrice;
    
    @NotNull(message = "Số lượng sản phẩm là bắt buộc")
    @Min(value = 0, message = "Số lượng sản phẩm không được âm")
    @Max(value = 999999, message = "Số lượng sản phẩm quá lớn")
    private Integer stock;
    
    private String status;
    
    private List<String> tags;
    
    @Size(max = 50, message = "Trọng lượng không được vượt quá 50 ký tự")
    private String weight;
    
    private String specifications;
    
    private String sellerId;
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
}

