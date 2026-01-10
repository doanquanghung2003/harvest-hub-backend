package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Repository.ProductRepository;
import com.example.harvesthubbackend.Exception.ApiException;
import com.example.harvesthubbackend.Exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

// Service
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAll() {
        // Trả về tất cả sản phẩm trong database
        List<Product> allProducts = productRepository.findAll();
        System.out.println("=== ProductService.getAll() ===");
        System.out.println("Total products in database: " + (allProducts != null ? allProducts.size() : 0));
        if (allProducts != null && !allProducts.isEmpty()) {
            System.out.println("Sample products:");
            for (int i = 0; i < Math.min(3, allProducts.size()); i++) {
                Product p = allProducts.get(i);
                System.out.println("  - " + p.getName() + " (ID: " + p.getId() + ", Status: " + p.getStatus() + ", SellerId: " + p.getSellerId() + ")");
            }
        }
        return allProducts;
    }

    public Product getById(String id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> getBySellerId(String sellerId) {
        System.out.println("ProductService.getBySellerId() - sellerId = " + sellerId);
        List<Product> products = productRepository.findBySellerId(sellerId);
        System.out.println("ProductService.getBySellerId() - found " + (products != null ? products.size() : 0) + " products");
        return products;
    }

    public List<Product> getByCategory(String category) {
        System.out.println("ProductService.getByCategory() - category = " + category);
        if (category == null || category.trim().isEmpty()) {
            return productRepository.findAll();
        }
        
        String normalizedCategory = category.trim();
        
        // Try exact match first
        List<Product> products = productRepository.findByCategory(normalizedCategory);
        
        // If no exact match, try case-insensitive search
        if (products == null || products.isEmpty()) {
            List<Product> allProducts = productRepository.findAll();
            products = allProducts.stream()
                .filter(p -> {
                    if (p.getCategory() == null) return false;
                    // Normalize both for comparison (trim and case-insensitive)
                    String dbCategory = p.getCategory().trim();
                    String searchCategory = normalizedCategory;
                    return dbCategory.equalsIgnoreCase(searchCategory);
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        System.out.println("ProductService.getByCategory() - found " + (products != null ? products.size() : 0) + " products");
        if (products != null && !products.isEmpty()) {
            System.out.println("Sample categories found: " + products.stream()
                .limit(3)
                .map(p -> p.getCategory())
                .collect(java.util.stream.Collectors.joining(", ")));
        }
        return products;
    }

    // Product creation method has been removed

    @Transactional
    public Product create(Product product) {
        try {
            System.out.println("ProductService.create() - Đang lưu sản phẩm: " + product);
            System.out.println("Tên sản phẩm: " + product.getName());
            System.out.println("Danh mục: " + product.getCategory());
            System.out.println("Giá: " + product.getPrice());
            System.out.println("Số lượng: " + product.getStock());
            System.out.println("Trạng thái: " + product.getStatus());
            System.out.println("Thời gian tạo: " + product.getCreatedAt());
            System.out.println("Thời gian cập nhật: " + product.getUpdatedAt());
            System.out.println("IMAGES: " + product.getImages());
            
            // Debug các trường có vấn đề
            System.out.println("=== DEBUG CÁC TRƯỜNG CÓ VẤN ĐỀ TRONG SERVICE ===");
            System.out.println("shortDescription: '" + product.getShortDescription() + "'");
            System.out.println("originalPrice: " + product.getOriginalPrice());
            System.out.println("weight: '" + product.getWeight() + "'");
            
            // Đảm bảo các trường bắt buộc có giá trị
            if (product.getStatus() == null) {
                product.setStatus("active");
            }
            if (product.getCreatedAt() == null) {
                product.setCreatedAt(java.time.LocalDateTime.now());
            }
            product.setUpdatedAt(java.time.LocalDateTime.now());
            
            return productRepository.save(product);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo sản phẩm: " + e.getMessage());
        }
    }

    @Transactional
    public Product createWithFiles(Product product, MultipartFile[] images, MultipartFile[] detailImages) {
        // In this simplified implementation, we don't persist files to disk.
        // If you already have file upload logic elsewhere, integrate it here to store and set URLs.
        try {
            List<String> main = new ArrayList<>();
            if (images != null) {
                for (MultipartFile f : images) {
                    if (f != null && !f.isEmpty()) {
                        // Placeholder: store file and obtain URL. For now, use original filename.
                        main.add("/uploads/" + f.getOriginalFilename());
                    }
                }
            }
            if (!main.isEmpty()) {
                product.setImages(main);
                if (product.getMainImage() == null) {
                    product.setMainImage(main.get(0));
                }
            }
            List<String> details = new ArrayList<>();
            if (detailImages != null) {
                for (MultipartFile f : detailImages) {
                    if (f != null && !f.isEmpty()) {
                        details.add("/uploads/" + f.getOriginalFilename());
                    }
                }
            }
            if (!details.isEmpty()) {
                product.setDetailImages(details);
            }
            return create(product);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo sản phẩm kèm ảnh: " + e.getMessage());
        }
    }

    public Product update(String id, Product product) {
        product.setId(id);
        return productRepository.save(product);
    }

    public void delete(String id) {
        productRepository.deleteById(id);
    }

    public Product updateStatus(String id, String status) {
        Product product = getById(id);
        if (product != null) {
            product.setStatus(status);
            return productRepository.save(product);
        }
        return null;
    }

    public long countAll() {
        return productRepository.count();
    }

    public List<Product> getRecent() {
        return productRepository.findTop10ByOrderByCreatedAtDesc();
    }

    // ===== PHƯƠNG THỨC CHO VIỆC DUYỆT SẢN PHẨM =====
    
    public List<Product> getPendingProducts() {
        return productRepository.findByApprovalStatus("pending");
    }

    public Product approveProduct(String id, String adminId) {
        Product product = getById(id);
        if (product == null) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        
        // Validate that product is in pending status
        if (!"pending".equalsIgnoreCase(product.getApprovalStatus())) {
            throw new ApiException(
                ErrorCode.PRODUCT_APPROVAL_INVALID,
                String.format("Cannot approve product with approval status '%s'. Only pending products can be approved.", 
                    product.getApprovalStatus())
            );
        }
        
        product.setApprovalStatus("approved");
        product.setStatus("active"); // Set status to active when approved
        product.setApprovedBy(adminId);
        product.setApprovedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        // Clear rejection info if any
        product.setRejectionReason(null);
        product.setRejectedBy(null);
        product.setRejectedAt(null);
        
        return productRepository.save(product);
    }

    public Product rejectProduct(String id, String adminId, String reason) {
        Product product = getById(id);
        if (product == null) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        
        // Validate that product is in pending status
        if (!"pending".equalsIgnoreCase(product.getApprovalStatus())) {
            throw new ApiException(
                ErrorCode.PRODUCT_APPROVAL_INVALID,
                String.format("Cannot reject product with approval status '%s'. Only pending products can be rejected.", 
                    product.getApprovalStatus())
            );
        }
        
        // Validate reason is provided
        if (reason == null || reason.trim().isEmpty()) {
            throw new ApiException(ErrorCode.PRODUCT_REJECTION_REASON_REQUIRED);
        }
        
        product.setApprovalStatus("rejected");
        product.setStatus("inactive"); // Set status to inactive when rejected
        product.setRejectionReason(reason.trim());
        product.setRejectedBy(adminId);
        product.setRejectedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        // Clear approval info if any
        product.setApprovedBy(null);
        product.setApprovedAt(null);
        
        return productRepository.save(product);
    }
    
    /**
     * Resubmit rejected product for approval
     */
    public Product resubmitProduct(String id) {
        Product product = getById(id);
        if (product == null) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        
        // Only rejected products can be resubmitted
        if (!"rejected".equalsIgnoreCase(product.getApprovalStatus())) {
            throw new ApiException(
                ErrorCode.PRODUCT_APPROVAL_INVALID,
                "Only rejected products can be resubmitted for approval"
            );
        }
        
        product.setApprovalStatus("pending");
        product.setUpdatedAt(LocalDateTime.now());
        
        // Clear rejection info
        product.setRejectionReason(null);
        product.setRejectedBy(null);
        product.setRejectedAt(null);
        
        return productRepository.save(product);
    }

    public Map<String, Object> getProductApprovalStats() {
        List<Product> allProducts = productRepository.findAll();
        List<Product> pendingProducts = productRepository.findByApprovalStatus("pending");
        List<Product> approvedProducts = productRepository.findByApprovalStatus("approved");
        List<Product> rejectedProducts = productRepository.findByApprovalStatus("rejected");
        
        // Tính toán thống kê tuần này
        long approvedThisWeek = approvedProducts.stream()
            .filter(product -> product.getApprovedAt() != null && 
                    product.getApprovedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
            .count();
            
        long rejectedThisWeek = rejectedProducts.stream()
            .filter(product -> product.getRejectedAt() != null && 
                    product.getRejectedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
            .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allProducts.size());
        stats.put("pending", pendingProducts.size());
        stats.put("approved", approvedProducts.size());
        stats.put("rejected", rejectedProducts.size());
        stats.put("approvedThisWeek", approvedThisWeek);
        stats.put("rejectedThisWeek", rejectedThisWeek);
        
        return stats;
    }
}