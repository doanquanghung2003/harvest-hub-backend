package com.example.harvesthubbackend.Utils;

import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Models.Review;
import com.example.harvesthubbackend.Models.Category;
import com.example.harvesthubbackend.Models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để normalize URL ảnh
 * Chuyển đổi URL tuyệt đối với localhost thành đường dẫn tương đối
 * để hoạt động với mọi IP/hostname khi truy cập từ mạng
 */
public class ImageUrlUtils {
    
    /**
     * Normalize một URL ảnh đơn lẻ
     * Chuyển http://localhost:8081/uploads/... thành /uploads/...
     * Chuyển http://127.0.0.1:8081/uploads/... thành /uploads/...
     * Giữ nguyên đường dẫn tương đối nếu đã là tương đối
     * 
     * @param imageUrl URL ảnh cần normalize
     * @return URL đã được normalize (đường dẫn tương đối)
     */
    public static String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return imageUrl;
        }
        
        String url = imageUrl.trim();
        
        // Nếu đã là đường dẫn tương đối (bắt đầu bằng /), giữ nguyên
        if (url.startsWith("/")) {
            return url;
        }
        
        // Chuyển đổi URL localhost thành đường dẫn tương đối
        // http://localhost:8081/uploads/... -> /uploads/...
        // http://127.0.0.1:8081/uploads/... -> /uploads/...
        // https://localhost:8081/uploads/... -> /uploads/...
        // https://127.0.0.1:8081/uploads/... -> /uploads/...
        
        String[] patterns = {
            "http://localhost:8081",
            "https://localhost:8081",
            "http://127.0.0.1:8081",
            "https://127.0.0.1:8081",
            "http://localhost:8080",
            "https://localhost:8080",
            "http://127.0.0.1:8080",
            "https://127.0.0.1:8080"
        };
        
        for (String pattern : patterns) {
            if (url.startsWith(pattern)) {
                String path = url.substring(pattern.length());
                // Đảm bảo path bắt đầu bằng /
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                return path;
            }
        }
        
        // Nếu là URL tuyệt đối khác (không phải localhost), giữ nguyên
        // hoặc nếu không match pattern nào, giữ nguyên
        return url;
    }
    
    /**
     * Normalize danh sách URL ảnh
     * 
     * @param imageUrls Danh sách URL ảnh
     * @return Danh sách URL đã được normalize
     */
    public static List<String> normalizeImageUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return null;
        }
        
        List<String> normalized = new ArrayList<>();
        for (String url : imageUrls) {
            normalized.add(normalizeImageUrl(url));
        }
        return normalized;
    }
    
    /**
     * Normalize URL ảnh trong Product object
     * 
     * @param product Product object cần normalize
     * @return Product đã được normalize (cùng instance)
     */
    public static Product normalizeProduct(Product product) {
        if (product == null) {
            return null;
        }
        
        if (product.getImages() != null) {
            product.setImages(normalizeImageUrls(product.getImages()));
        }
        
        if (product.getDetailImages() != null) {
            product.setDetailImages(normalizeImageUrls(product.getDetailImages()));
        }
        
        return product;
    }
    
    /**
     * Normalize URL ảnh trong danh sách Product
     * 
     * @param products Danh sách Product
     * @return Danh sách Product đã được normalize
     */
    public static List<Product> normalizeProducts(List<Product> products) {
        if (products == null) {
            return null;
        }
        
        for (Product product : products) {
            normalizeProduct(product);
        }
        
        return products;
    }
    
    /**
     * Normalize URL ảnh trong Review object
     * 
     * @param review Review object cần normalize
     * @return Review đã được normalize (cùng instance)
     */
    public static Review normalizeReview(Review review) {
        if (review == null) {
            return null;
        }
        
        if (review.getImages() != null) {
            review.setImages(normalizeImageUrls(review.getImages()));
        }
        
        if (review.getUserAvatar() != null) {
            review.setUserAvatar(normalizeImageUrl(review.getUserAvatar()));
        }
        
        return review;
    }
    
    /**
     * Normalize URL ảnh trong danh sách Review
     * 
     * @param reviews Danh sách Review
     * @return Danh sách Review đã được normalize
     */
    public static List<Review> normalizeReviews(List<Review> reviews) {
        if (reviews == null) {
            return null;
        }
        
        for (Review review : reviews) {
            normalizeReview(review);
        }
        
        return reviews;
    }
    
    /**
     * Normalize URL ảnh trong Category object
     * 
     * @param category Category object cần normalize
     * @return Category đã được normalize (cùng instance)
     */
    public static Category normalizeCategory(Category category) {
        if (category == null) {
            return null;
        }
        
        if (category.getImage() != null) {
            category.setImage(normalizeImageUrl(category.getImage()));
        }
        
        if (category.getIcon() != null) {
            category.setIcon(normalizeImageUrl(category.getIcon()));
        }
        
        return category;
    }
    
    /**
     * Normalize URL ảnh trong danh sách Category
     * 
     * @param categories Danh sách Category
     * @return Danh sách Category đã được normalize
     */
    public static List<Category> normalizeCategories(List<Category> categories) {
        if (categories == null) {
            return null;
        }
        
        for (Category category : categories) {
            normalizeCategory(category);
        }
        
        return categories;
    }
    
    /**
     * Normalize URL ảnh trong User object
     * 
     * @param user User object cần normalize
     * @return User đã được normalize (cùng instance)
     */
    public static User normalizeUser(User user) {
        if (user == null) {
            return null;
        }
        
        if (user.getAvatar() != null) {
            user.setAvatar(normalizeImageUrl(user.getAvatar()));
        }
        
        return user;
    }
    
    /**
     * Normalize URL ảnh trong danh sách User
     * 
     * @param users Danh sách User
     * @return Danh sách User đã được normalize
     */
    public static List<User> normalizeUsers(List<User> users) {
        if (users == null) {
            return null;
        }
        
        for (User user : users) {
            normalizeUser(user);
        }
        
        return users;
    }
}

