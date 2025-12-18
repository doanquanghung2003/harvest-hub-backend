package com.example.harvesthubbackend.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateReviewDTO {
    @NotBlank(message = "Mã sản phẩm không được bỏ trống")
    private String productId;

    @Min(value = 1, message = "Đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa là 5 sao")
    private int rating;

    @Size(max = 1000, message = "Nội dung đánh giá không được vượt quá 1000 ký tự")
    private String comment;

    private List<String> images;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}

