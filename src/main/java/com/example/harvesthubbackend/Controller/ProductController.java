package com.example.harvesthubbackend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Service.ProductService;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.File;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import com.example.harvesthubbackend.Utils.PageResponse;
import com.example.harvesthubbackend.Utils.PaginationUtils;
import com.example.harvesthubbackend.Utils.ImageUrlUtils;

@RestController
@RequestMapping({"/api/products", "/api/v1/products"})
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "API endpoints for product management")
public class ProductController {
    @Autowired
    private ProductService productService;

    // Giới hạn tối đa số ảnh cho mỗi request
    private static final int MAX_IMAGES = 10;

    @Operation(summary = "Get all products", description = "Retrieve all products with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping
    public Object getAll(
        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") String page,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") String size,
        @Parameter(description = "Return as array (no pagination)", example = "false") @RequestParam(defaultValue = "false") boolean asArray) {
        List<Product> allProducts = productService.getAll();
        
        // Normalize URL ảnh để hoạt động với mọi IP/hostname
        ImageUrlUtils.normalizeProducts(allProducts);
        
        // Nếu frontend yêu cầu array trực tiếp (cho admin dashboard)
        if (asArray) {
            return ResponseEntity.ok(allProducts);
        }
        
        // Mặc định trả về PageResponse (có pagination)
        int pageNum = PaginationUtils.parsePage(page);
        int pageSize = PaginationUtils.parseSize(size);
        return PaginationUtils.paginate(allProducts, pageNum, pageSize);
    }

    // Endpoint trả về array trực tiếp (không pagination) cho admin dashboard
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAll();
        // Normalize URL ảnh để hoạt động với mọi IP/hostname
        ImageUrlUtils.normalizeProducts(products);
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Get products by seller", description = "Retrieve all products for a specific seller with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping("/seller/{sellerId}")
    public PageResponse<Product> getProductsBySeller(
        @Parameter(description = "Seller ID") @PathVariable String sellerId,
        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") String page,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") String size) {
        List<Product> sellerProducts = productService.getBySellerId(sellerId);
        // Normalize URL ảnh để hoạt động với mọi IP/hostname
        ImageUrlUtils.normalizeProducts(sellerProducts);
        int pageNum = PaginationUtils.parsePage(page);
        int pageSize = PaginationUtils.parseSize(size);
        return PaginationUtils.paginate(sellerProducts, pageNum, pageSize);
    }

    // Endpoint POST riêng cho multipart upload
    // Hỗ trợ cả multipart/form-data và application/x-www-form-urlencoded cho mobile compatibility
    @PostMapping(value = "/upload", consumes = {
        org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE,
        org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
    })
    public ResponseEntity<?> createProductMultipart(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "description", required = false) String description,
                                   @RequestParam(value = "shortDescription", required = false) String shortDescription,
                                   @RequestParam(value = "category", required = false) String category,
                                   @RequestParam(value = "price", required = false) Double price,
                                   @RequestParam(value = "originalPrice", required = false) Double originalPrice,
                                   @RequestParam(value = "stock", required = false) Integer stock,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "tags", required = false) String tags,
                                   @RequestParam(value = "weight", required = false) Double weight,
                                   @RequestParam(value = "dimensions", required = false) String dimensions,
                                   @RequestParam(value = "specifications", required = false) String specifications,
                                   @RequestParam(value = "sellerId", required = false) String sellerId,
                                   @RequestParam(value = "images", required = false) MultipartFile[] images,
                                   @RequestParam(value = "detailImages", required = false) MultipartFile[] detailImages) {
        
        System.out.println("=== ENDPOINT POST /api/products/upload (multipart) ===");
        System.out.println("Name: " + name);
        System.out.println("Category: " + category);
        System.out.println("Price: " + price);
        System.out.println("Stock: " + stock);
        System.out.println("Weight: " + weight);
        System.out.println("SellerId: " + sellerId);
        System.out.println("Images count: " + (images != null ? images.length : 0));
        System.out.println("Detail images count: " + (detailImages != null ? detailImages.length : 0));
        
        // Log thêm thông tin để debug mobile upload
        if (images != null) {
            for (int i = 0; i < images.length; i++) {
                if (images[i] != null) {
                    System.out.println("Image " + i + ": " + images[i].getOriginalFilename() + 
                        ", Size: " + images[i].getSize() + 
                        ", ContentType: " + images[i].getContentType());
                }
            }
        }
        
        // Log chi tiết về request
        System.out.println("=== REQUEST DETAILS ===");
        System.out.println("Request method: POST");
        System.out.println("Content-Type: multipart/form-data");
        System.out.println("Request parameters received successfully");
        
        if (images != null) {
            for (int i = 0; i < images.length; i++) {
                MultipartFile img = images[i];
                System.out.println("Image " + i + ": " + (img != null ? img.getOriginalFilename() : "null"));
                if (img != null) {
                    System.out.println("  - Size: " + img.getSize() + " bytes");
                    System.out.println("  - Content-Type: " + img.getContentType());
                    System.out.println("  - Is empty: " + img.isEmpty());
                }
            }
        }
        
        if (detailImages != null) {
            for (int i = 0; i < detailImages.length; i++) {
                MultipartFile img = detailImages[i];
                System.out.println("Detail Image " + i + ": " + (img != null ? img.getOriginalFilename() : "null"));
                if (img != null) {
                    System.out.println("  - Size: " + img.getSize() + " bytes");
                    System.out.println("  - Content-Type: " + img.getContentType());
                    System.out.println("  - Is empty: " + img.isEmpty());
                }
            }
        }
        
        try {
            // Tạo sản phẩm từ các tham số
            Product product = new Product();
            
            // Validation cơ bản
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên sản phẩm là bắt buộc"));
            }
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mô tả sản phẩm là bắt buộc"));
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Danh mục sản phẩm là bắt buộc"));
            }
            if (price == null || price <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giá sản phẩm hợp lệ là bắt buộc"));
            }
            if (stock == null || stock < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Số lượng sản phẩm hợp lệ là bắt buộc"));
            }
            
            product.setName(name.trim());
            product.setDescription(description.trim());
            if (shortDescription != null && !shortDescription.trim().isEmpty()) product.setShortDescription(shortDescription.trim());
            product.setCategory(category.trim());
            product.setPrice(price.doubleValue());
            if (originalPrice != null && originalPrice > 0) product.setOriginalPrice(originalPrice);
            product.setStock(stock.intValue());
            product.setStatus(status != null ? status : "active");
            
            // Set sellerId nếu có
            if (sellerId != null && !sellerId.trim().isEmpty()) {
                product.setSellerId(sellerId.trim());
                System.out.println("✅ Đã set sellerId: " + sellerId);
            } else {
                System.out.println("⚠️ sellerId không được cung cấp hoặc rỗng");
            }
            
            // Xử lý weight
            if (weight != null) {
                product.setWeight(weight.toString());
            }
            
            // Xử lý ảnh nếu có
            if (images != null && images.length > 0) {
                System.out.println("=== XỬ LÝ ẢNH CHÍNH ===");
                System.out.println("Đang xử lý " + images.length + " ảnh chính...");
                List<String> imageUrls = new ArrayList<>();
                
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    System.out.println("Xử lý ảnh " + (i+1) + "/" + images.length);
                    
                    if (image != null && !image.isEmpty()) {
                        System.out.println("  - Tên file: " + image.getOriginalFilename());
                        System.out.println("  - Kích thước: " + image.getSize() + " bytes");
                        System.out.println("  - Content-Type: " + image.getContentType());
                        
                        try {
                            String imageUrl = saveImage(image);
                            if (imageUrl != null) {
                                // Sử dụng đường dẫn tương đối để hoạt động với mọi IP/hostname
                                imageUrls.add(imageUrl);
                                System.out.println("  ✅ Ảnh đã được lưu: " + imageUrl);
                            } else {
                                System.err.println("  ❌ Lỗi: saveImage() trả về null cho ảnh: " + image.getOriginalFilename());
                            }
                        } catch (Exception e) {
                            System.err.println("  ❌ Lỗi khi lưu ảnh " + image.getOriginalFilename() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("  ⚠️ Ảnh " + (i+1) + " bị bỏ qua (null hoặc empty)");
                    }
                }
                
                if (!imageUrls.isEmpty()) {
                    product.setImages(imageUrls);
                    System.out.println("✅ Tổng cộng " + imageUrls.size() + " ảnh đã được lưu và gán vào product");
                } else {
                    System.err.println("❌ Không có ảnh nào được lưu thành công!");
                }
            } else {
                System.out.println("Không có ảnh nào để xử lý");
            }
            
            // Xử lý ảnh chi tiết nếu có
            if (detailImages != null && detailImages.length > 0) {
                System.out.println("Đang xử lý " + detailImages.length + " ảnh chi tiết...");
                List<String> detailImageUrls = new ArrayList<>();
                
                for (int i = 0; i < detailImages.length; i++) {
                    MultipartFile image = detailImages[i];
                    System.out.println("Xử lý ảnh chi tiết " + (i+1) + "/" + detailImages.length);
                    
                    if (image != null && !image.isEmpty()) {
                        System.out.println("  - Tên file: " + image.getOriginalFilename());
                        System.out.println("  - Kích thước: " + image.getSize() + " bytes");
                        System.out.println("  - Content-Type: " + image.getContentType());
                        
                        try {
                            String imageUrl = saveImage(image);
                            if (imageUrl != null) {
                                // Sử dụng đường dẫn tương đối để hoạt động với mọi IP/hostname
                                detailImageUrls.add(imageUrl);
                                System.out.println("  ✅ Ảnh chi tiết đã được lưu: " + imageUrl);
                            } else {
                                System.err.println("  ❌ Lỗi: saveImage() trả về null cho ảnh chi tiết: " + image.getOriginalFilename());
                            }
                        } catch (Exception e) {
                            System.err.println("  ❌ Lỗi khi lưu ảnh chi tiết " + image.getOriginalFilename() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("  ⚠️ Ảnh chi tiết " + (i+1) + " bị bỏ qua (null hoặc empty)");
                    }
                }
                
                if (!detailImageUrls.isEmpty()) {
                    product.setDetailImages(detailImageUrls);
                    System.out.println("✅ Tổng cộng " + detailImageUrls.size() + " ảnh chi tiết đã được lưu và gán vào product");
                } else {
                    System.err.println("❌ Không có ảnh chi tiết nào được lưu thành công!");
                }
            } else {
                System.out.println("Không có ảnh chi tiết nào để xử lý");
            }
            
            // Đặt thời gian
            product.setCreatedAt(java.time.LocalDateTime.now());
            product.setUpdatedAt(java.time.LocalDateTime.now());
            
            // Lưu sản phẩm vào database
            System.out.println("=== LƯU SẢN PHẨM VÀO DATABASE ===");
            System.out.println("Đang lưu sản phẩm vào database...");
            System.out.println("Product trước khi lưu:");
            System.out.println("  - Name: " + product.getName());
            System.out.println("  - Category: " + product.getCategory());
            System.out.println("  - Price: " + product.getPrice());
            System.out.println("  - Images count: " + (product.getImages() != null ? product.getImages().size() : 0));
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                System.out.println("  - Images URLs:");
                for (int i = 0; i < product.getImages().size(); i++) {
                    System.out.println("    " + (i+1) + ": " + product.getImages().get(i));
                }
            }
            
            Product savedProduct = productService.create(product);
            System.out.println("✅ Sản phẩm đã được lưu thành công với ID: " + savedProduct.getId());
            
            System.out.println("Product sau khi lưu:");
            System.out.println("  - ID: " + savedProduct.getId());
            System.out.println("  - Name: " + savedProduct.getName());
            System.out.println("  - Images count: " + (savedProduct.getImages() != null ? savedProduct.getImages().size() : 0));
            if (savedProduct.getImages() != null && !savedProduct.getImages().isEmpty()) {
                System.out.println("  - Images URLs:");
                for (int i = 0; i < savedProduct.getImages().size(); i++) {
                    System.out.println("    " + (i+1) + ": " + savedProduct.getImages().get(i));
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Sản phẩm đã được tạo thành công");
            response.put("product", savedProduct);
            response.put("imagesUploaded", images != null ? images.length : 0);
            response.put("detailImagesUploaded", detailImages != null ? detailImages.length : 0);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== LỖI KHI TẠO SẢN PHẨM ===");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
            System.err.println("Stack trace:");
            e.printStackTrace();
            
            // Log thêm thông tin về request
            System.err.println("=== REQUEST INFO WHEN ERROR ===");
            System.err.println("Name: " + name);
            System.err.println("Category: " + category);
            System.err.println("Images count: " + (images != null ? images.length : 0));
            System.err.println("Detail images count: " + (detailImages != null ? detailImages.length : 0));
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("exceptionType", e.getClass().getSimpleName());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("count", productService.countAll());
        stats.put("recent", productService.getRecent());
        stats.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(stats);
    }
    
    
    
    @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public Product getById(@Parameter(description = "Product ID") @PathVariable String id) {
        Product product = productService.getById(id);
        // Normalize URL ảnh để hoạt động với mọi IP/hostname
        ImageUrlUtils.normalizeProduct(product);
        return product;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        try {
            Product product = productService.getById(id);
            if (product == null) {
                return ResponseEntity.notFound().build();
            }
            
            productService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Sản phẩm đã được xóa thành công"));
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa sản phẩm: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi khi xóa sản phẩm", "message", e.getMessage()));
        }
    }

    // ===== ENDPOINT UPDATE SẢN PHẨM =====
    
    // Endpoint để update ảnh sản phẩm
    @PostMapping(value = "/{id}/update-images", consumes = {
        org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE,
        org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
    })
    @PreAuthorize("hasRole('ADMIN')") // Bật lại authentication
    public ResponseEntity<?> updateProductImages(
            @PathVariable String id,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "detailImages", required = false) MultipartFile[] detailImages) {
        
        System.out.println("=== ENDPOINT UPDATE ẢNH SẢN PHẨM ===");
        System.out.println("ID sản phẩm: " + id);
        System.out.println("Số lượng ảnh: " + (images != null ? images.length : 0));
        System.out.println("Số lượng ảnh chi tiết: " + (detailImages != null ? detailImages.length : 0));
        
        // Validate tổng số ảnh không vượt quá MAX_IMAGES
        int totalUpdateImages = (images != null ? images.length : 0) + (detailImages != null ? detailImages.length : 0);
        if (totalUpdateImages > MAX_IMAGES) {
                return ResponseEntity.badRequest().body(Map.of(
                "error", "Tối đa " + MAX_IMAGES + " ảnh cho mỗi lần cập nhật",
                "max", MAX_IMAGES,
                "provided", totalUpdateImages
            ));
        }
        
        try {
            // Kiểm tra sản phẩm có tồn tại không
            Product existingProduct = productService.getById(id);
            if (existingProduct == null) {
                return ResponseEntity.notFound().build();
            }
            
                List<String> newImageUrls = new ArrayList<>();
                
            // Xử lý ảnh chính
            if (images != null && images.length > 0) {
                for (MultipartFile image : images) {
                    if (image != null && !image.isEmpty()) {
                        String imageUrl = saveImage(image);
                        if (imageUrl != null) {
                            // Sử dụng đường dẫn tương đối để hoạt động với mọi IP/hostname
                            newImageUrls.add(imageUrl);
                            System.out.println("Đã lưu ảnh: " + imageUrl);
                        }
                    }
                }
            }
            
            // Xử lý ảnh chi tiết (nếu có)
            List<String> newDetailImageUrls = new ArrayList<>();
            if (detailImages != null && detailImages.length > 0) {
                for (MultipartFile image : detailImages) {
                    if (image != null && !image.isEmpty()) {
                        String imageUrl = saveImage(image);
                        if (imageUrl != null) {
                            // Sử dụng đường dẫn tương đối để hoạt động với mọi IP/hostname
                            newDetailImageUrls.add(imageUrl);
                            System.out.println("Đã lưu ảnh chi tiết: " + imageUrl);
                        }
                    }
                }
            }
            
            // Cập nhật sản phẩm với ảnh mới
            if (!newImageUrls.isEmpty()) {
                existingProduct.setImages(newImageUrls);
            }
            
            if (!newDetailImageUrls.isEmpty()) {
                existingProduct.setDetailImages(newDetailImageUrls);
            }
            
            // Cập nhật thời gian
            existingProduct.setUpdatedAt(java.time.LocalDateTime.now());
            
            // Lưu sản phẩm đã cập nhật
            Product updatedProduct = productService.update(id, existingProduct);
            
            System.out.println("Cập nhật sản phẩm thành công: " + updatedProduct.getId());
            
            return ResponseEntity.ok(updatedProduct);
            
        } catch (Exception e) {
            System.err.println("Lỗi khi cập nhật ảnh sản phẩm: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Lỗi khi cập nhật ảnh sản phẩm",
                "message", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    // Cập nhật sản phẩm (JSON, không bao gồm upload ảnh)
    @PutMapping(value = "/{id}/json", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProductJsonEndpoint(@PathVariable String id, @RequestBody Product patch) {
        try {
            Product existing = productService.getById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            // Merge các trường có giá trị
            if (patch.getName() != null) existing.setName(patch.getName());
            if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
            if (patch.getShortDescription() != null) existing.setShortDescription(patch.getShortDescription());
            if (patch.getCategory() != null) existing.setCategory(patch.getCategory());
            if (patch.getPrice() != null) existing.setPrice(patch.getPrice());
            if (patch.getOriginalPrice() != null) existing.setOriginalPrice(patch.getOriginalPrice());
            if (patch.getStock() != null) existing.setStock(patch.getStock());
            if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
            if (patch.getTags() != null) existing.setTags(patch.getTags());
            if (patch.getWeight() != null) existing.setWeight(patch.getWeight());
            // Thông tin kỹ thuật
            if (patch.getOrigin() != null) existing.setOrigin(patch.getOrigin());
            if (patch.getUnit() != null) existing.setUnit(patch.getUnit());
            if (patch.getExpiryDate() != null) existing.setExpiryDate(patch.getExpiryDate());
            if (patch.getStorageInstructions() != null) existing.setStorageInstructions(patch.getStorageInstructions());
            // Model Product không có trường dimensions, ingredients, brand – lưu vào specifications
            if (patch.getSpecifications() != null) {
                existing.setSpecifications(patch.getSpecifications());
            } else {
                // Nếu có các trường dimensions, ingredients, brand trong request body nhưng không có specifications
                // thì tạo specifications object mới hoặc merge vào existing
                java.util.Map<String, String> specs = existing.getSpecifications() != null 
                    ? new java.util.HashMap<>(existing.getSpecifications()) 
                    : new java.util.HashMap<>();
                // Lưu dimensions, ingredients, brand vào specifications nếu có
                // (Cần kiểm tra xem patch có các trường này không - nhưng Product model không có)
                // Vì vậy cần gửi trong specifications object từ frontend
                if (!specs.isEmpty()) {
                    existing.setSpecifications(specs);
                }
            }

            existing.setUpdatedAt(java.time.LocalDateTime.now());

            Product saved = productService.update(id, existing);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("Lỗi update JSON: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(java.util.Map.of(
                "error", "Lỗi khi cập nhật sản phẩm",
                "message", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }

    // ===== ENDPOINT CHÍNH ĐỂ TẠO SẢN PHẨM =====
    
    // Endpoint chính để tạo sản phẩm với multipart form data (chỉ dùng /create)
    @PostMapping(value = "/create", consumes = {
        org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE,
        org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
    })
    // @PreAuthorize("hasRole('ADMIN')") // Tạm thởi tắt để test
    public ResponseEntity<?> createProduct(@RequestParam(value = "name", required = false) String name,
                                   @RequestParam(value = "description", required = false) String description,
                                   @RequestParam(value = "shortDescription", required = false) String shortDescription,
                                   @RequestParam(value = "category", required = false) String category,
                                   @RequestParam(value = "price", required = false) Double price,
                                   @RequestParam(value = "originalPrice", required = false) Double originalPrice,
                                   @RequestParam(value = "stock", required = false) Integer stock,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "tags", required = false) String tags,
                                   @RequestParam(value = "weight", required = false) Double weight,
                                   @RequestParam(value = "dimensions", required = false) String dimensions,
                                   @RequestParam(value = "specifications", required = false) String specifications,
                                   @RequestParam(value = "images", required = false) MultipartFile[] images,
                                           @RequestParam(value = "detailImages", required = false) MultipartFile[] detailImages,
                                           @org.springframework.web.bind.annotation.RequestPart(value = "product", required = false) Product productBody) {
        
        System.out.println("=== ENDPOINT TẠO SẢN PHẨM CHÍNH THỨC ===");
        System.out.println("Yêu cầu nhận được lúc: " + java.time.LocalDateTime.now());
        System.out.println("Phương thức yêu cầu: POST");
        System.out.println("Loại nội dung: multipart/form-data");
        
        // Debug các trường có vấn đề
        System.out.println("=== DEBUG CÁC TRƯỜNG CÓ VẤN ĐỀ ===");
        System.out.println("shortDescription: '" + shortDescription + "' (null: " + (shortDescription == null) + ")");
        System.out.println("originalPrice: " + originalPrice + " (null: " + (originalPrice == null) + ")");
        System.out.println("weight: " + weight + " (null: " + (weight == null) + ")");
        
        try {
            System.out.println("=== Xử lý yêu cầu multipart ===");
            System.out.println("Tên: " + name);
            System.out.println("Danh mục: " + category);
            System.out.println("Số lượng ảnh: " + (images != null ? images.length : 0));
            if (images != null && images.length > 0) {
                for (int i = 0; i < images.length; i++) {
                    MultipartFile img = images[i];
                    System.out.println("  Ảnh " + i + ": " + (img != null ? img.getOriginalFilename() : "null"));
                    System.out.println("  Kích thước ảnh " + i + ": " + (img != null ? img.getSize() : "null"));
                    System.out.println("  Loại nội dung ảnh " + i + ": " + (img != null ? img.getContentType() : "null"));
                }
            }
            System.out.println("Số lượng ảnh chi tiết: " + (detailImages != null ? detailImages.length : 0));
            if (detailImages != null && detailImages.length > 0) {
                for (int i = 0; i < detailImages.length; i++) {
                    MultipartFile img = detailImages[i];
                    System.out.println("  Ảnh chi tiết " + i + ": " + (img != null ? img.getOriginalFilename() : "null"));
                    System.out.println("  Kích thước ảnh chi tiết " + i + ": " + (img != null ? img.getSize() : "null"));
                    System.out.println("  Loại nội dung ảnh chi tiết " + i + ": " + (img != null ? img.getContentType() : "null"));
                }
            }
            System.out.println("ProductBody: " + (productBody != null ? "có" : "không"));

            // Validate tổng số ảnh không vượt quá MAX_IMAGES để tránh lỗi multipart
            int totalImages = (images != null ? images.length : 0) + (detailImages != null ? detailImages.length : 0);
            if (totalImages > MAX_IMAGES) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Tối đa " + MAX_IMAGES + " ảnh cho mỗi lần tạo sản phẩm",
                    "max", MAX_IMAGES,
                    "provided", totalImages
                ));
            }
            
            Product product;
            
            // Nếu có productBody (JSON), sử dụng nó
            if (productBody != null) {
                System.out.println("Sử dụng productBody từ JSON");
                product = productBody;
            } else {
                // Nếu không có, tạo từ các tham số
                System.out.println("Tạo sản phẩm từ các tham số");
                product = new Product();
                
                // Validation cơ bản
                if (name == null || name.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Tên sản phẩm là bắt buộc"));
                }
                if (description == null || description.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Mô tả sản phẩm là bắt buộc"));
                }
                if (category == null || category.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Danh mục sản phẩm là bắt buộc"));
                }
                if (price == null || price <= 0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Giá sản phẩm hợp lệ là bắt buộc"));
                }
                if (stock == null || stock < 0) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Số lượng sản phẩm hợp lệ là bắt buộc"));
                }
                
                product.setName(name.trim());
                product.setDescription(description.trim());
                if (shortDescription != null && !shortDescription.trim().isEmpty()) product.setShortDescription(shortDescription.trim());
                product.setCategory(category.trim());
                product.setPrice(price.doubleValue());
                if (originalPrice != null && originalPrice > 0) product.setOriginalPrice(originalPrice);
                product.setStock(stock.intValue());
                product.setStatus(status != null ? status : "active");
                
                // Xử lý tags nếu có
                if (tags != null && !tags.isEmpty()) {
                    try {
                        // Parse JSON tags string
                        // product.setTags(tags);
                    } catch (Exception e) {
                        System.err.println("Lỗi khi parse tags: " + e.getMessage());
                    }
                }
                
                // Xử lý các trường khác
            if (weight != null) {
                    product.setWeight(weight.toString());
                    System.out.println("DEBUG: Đã set weight = " + weight.toString());
                } else {
                    System.out.println("DEBUG: weight là null, không set");
                }
                // if (dimensions != null) product.setDimensions(dimensions); // Không có field này
                // if (specifications != null) product.setSpecifications(specifications); // Cần Map<String, String>
                
                // Debug sau khi xử lý
                System.out.println("=== DEBUG SAU KHI XỬ LÝ ===");
                System.out.println("Product shortDescription: '" + product.getShortDescription() + "'");
                System.out.println("Product originalPrice: " + product.getOriginalPrice());
                System.out.println("Product weight: '" + product.getWeight() + "'");
            }
            
            // Xử lý ảnh nếu có
            if (images != null && images.length > 0) {
                System.out.println("=== XỬ LÝ ẢNH CHÍNH ===");
                System.out.println("Đang xử lý " + images.length + " ảnh chính...");
                List<String> imageUrls = new ArrayList<>();
                
                for (int i = 0; i < images.length; i++) {
                    MultipartFile image = images[i];
                    System.out.println("Xử lý ảnh " + (i+1) + "/" + images.length);
                    
                    if (image != null && !image.isEmpty()) {
                        System.out.println("  - Tên file: " + image.getOriginalFilename());
                        System.out.println("  - Kích thước: " + image.getSize() + " bytes");
                        System.out.println("  - Content-Type: " + image.getContentType());
                        
                        try {
                            String imageUrl = saveImage(image);
                            if (imageUrl != null) {
                                // Sử dụng đường dẫn tương đối để hoạt động với mọi IP/hostname
                                imageUrls.add(imageUrl);
                                System.out.println("  ✅ Ảnh đã được lưu: " + imageUrl);
                            } else {
                                System.err.println("  ❌ Lỗi: saveImage() trả về null cho ảnh: " + image.getOriginalFilename());
                            }
                        } catch (Exception e) {
                            System.err.println("  ❌ Lỗi khi lưu ảnh " + image.getOriginalFilename() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("  ⚠️ Ảnh " + (i+1) + " bị bỏ qua (null hoặc empty)");
                    }
                }
                
                if (!imageUrls.isEmpty()) {
                    product.setImages(imageUrls);
                    System.out.println("✅ Tổng cộng " + imageUrls.size() + " ảnh đã được lưu và gán vào product");
                } else {
                    System.err.println("❌ Không có ảnh nào được lưu thành công!");
                }
            } else {
                System.out.println("Không có ảnh nào để xử lý");
            }
            
            // Xử lý ảnh chi tiết nếu có
            if (detailImages != null && detailImages.length > 0) {
                System.out.println("=== XỬ LÝ ẢNH CHI TIẾT ===");
                System.out.println("Đang xử lý " + detailImages.length + " ảnh chi tiết...");
                List<String> detailImageUrls = new ArrayList<>();
                
                for (int i = 0; i < detailImages.length; i++) {
                    MultipartFile image = detailImages[i];
                    System.out.println("Xử lý ảnh chi tiết " + (i+1) + "/" + detailImages.length);
                    
                    if (image != null && !image.isEmpty()) {
                        System.out.println("  - Tên file: " + image.getOriginalFilename());
                        System.out.println("  - Kích thước: " + image.getSize() + " bytes");
                        System.out.println("  - Content-Type: " + image.getContentType());
                        
                        try {
                            String imageUrl = saveImage(image);
                            if (imageUrl != null) {
                                // Sử dụng đường dẫn tương đối để hoạt động với mọi IP/hostname
                                detailImageUrls.add(imageUrl);
                                System.out.println("  ✅ Ảnh chi tiết đã được lưu: " + imageUrl);
                            } else {
                                System.err.println("  ❌ Lỗi: saveImage() trả về null cho ảnh chi tiết: " + image.getOriginalFilename());
                            }
                        } catch (Exception e) {
                            System.err.println("  ❌ Lỗi khi lưu ảnh chi tiết " + image.getOriginalFilename() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("  ⚠️ Ảnh chi tiết " + (i+1) + " bị bỏ qua (null hoặc empty)");
                    }
                }
                
                if (!detailImageUrls.isEmpty()) {
                    product.setDetailImages(detailImageUrls);
                    System.out.println("✅ Tổng cộng " + detailImageUrls.size() + " ảnh chi tiết đã được lưu và gán vào product");
                } else {
                    System.err.println("❌ Không có ảnh chi tiết nào được lưu thành công!");
                }
            } else {
                System.out.println("Không có ảnh chi tiết nào để xử lý");
            }
            
            // Đặt thời gian tạo và cập nhật
            if (product.getCreatedAt() == null) {
                product.setCreatedAt(java.time.LocalDateTime.now());
            }
            product.setUpdatedAt(java.time.LocalDateTime.now());
            
            // Lưu sản phẩm vào database
            System.out.println("=== DEBUG TRƯỚC KHI LƯU VÀO DATABASE ===");
            System.out.println("Product shortDescription: '" + product.getShortDescription() + "'");
            System.out.println("Product originalPrice: " + product.getOriginalPrice());
            System.out.println("Product weight: '" + product.getWeight() + "'");
            System.out.println("Đang lưu sản phẩm vào database...");
            
            Product savedProduct = productService.create(product);
            System.out.println("Sản phẩm đã được lưu thành công với ID: " + savedProduct.getId());
            
            System.out.println("=== DEBUG SAU KHI LƯU VÀO DATABASE ===");
            System.out.println("Saved product shortDescription: '" + savedProduct.getShortDescription() + "'");
            System.out.println("Saved product originalPrice: " + savedProduct.getOriginalPrice());
            System.out.println("Saved product weight: '" + savedProduct.getWeight() + "'");
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Sản phẩm đã được tạo thành công");
            response.put("product", savedProduct);
            response.put("imagesUploaded", images != null ? images.length : 0);
            response.put("detailImagesUploaded", detailImages != null ? detailImages.length : 0);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== LỖI KHI TẠO SẢN PHẨM ===");
            System.err.println("Lỗi: " + e.getMessage());
            System.err.println("Loại lỗi: " + e.getClass().getSimpleName());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("errorType", e.getClass().getSimpleName());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }



    // Endpoint để tạo sản phẩm từ JSON (không có ảnh)
    @PostMapping(value = "/create-json", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasRole('ADMIN')") // Tạm thởi tắt để test
    public ResponseEntity<?> createProductFromJson(@RequestBody Product product) {
        try {
            System.out.println("=== Tạo sản phẩm từ JSON ===");
            System.out.println("Sản phẩm nhận được: " + product);
            
            // Validation cơ bản
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên sản phẩm là bắt buộc"));
            }
            if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mô tả sản phẩm là bắt buộc"));
            }
            if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Danh mục sản phẩm là bắt buộc"));
            }
            if (product.getPrice() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Giá sản phẩm hợp lệ là bắt buộc"));
            }
            if (product.getStock() < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Số lượng sản phẩm hợp lệ là bắt buộc"));
            }
            
            // Đặt các giá trị mặc định
            if (product.getStatus() == null) {
                product.setStatus("active");
            }
            if (product.getCreatedAt() == null) {
                product.setCreatedAt(java.time.LocalDateTime.now());
            }
            product.setUpdatedAt(java.time.LocalDateTime.now());
            
            // Lưu sản phẩm vào database
            Product savedProduct = productService.create(product);
            System.out.println("Sản phẩm đã được lưu thành công với ID: " + savedProduct.getId());
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Sản phẩm đã được tạo thành công từ JSON");
            response.put("product", savedProduct);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo sản phẩm từ JSON: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm từ JSON");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    

    // Phương thức để lưu ảnh
    private String saveImage(MultipartFile image) {
        try {
            System.out.println("=== DEBUG SAVE IMAGE ===");
            System.out.println("Bắt đầu saveImage()");
            
            if (image == null || image.isEmpty()) {
                System.out.println("saveImage: Ảnh rỗng hoặc null");
                return null;
            }
            
            // Kiểm tra kích thước file
            if (image.getSize() > 50 * 1024 * 1024) { // 50MB
                System.err.println("saveImage: File quá lớn (" + image.getSize() + " bytes)");
                return null;
            }
            
            // Kiểm tra loại file (nới lỏng) - nếu không xác định vẫn cho phép tiếp tục
            String contentType = image.getContentType();
            if (contentType == null) {
                System.out.println("saveImage: contentType null, tiếp tục lưu dựa trên phần mở rộng tên file");
            } else if (!contentType.startsWith("image/")) {
                System.err.println("saveImage: contentType không phải ảnh (" + contentType + "), vẫn thử lưu do một số trình duyệt không set đúng");
            }
            
            System.out.println("Ảnh hợp lệ, tiếp tục xử lý...");
            System.out.println("  - Tên file: " + image.getOriginalFilename());
            System.out.println("  - Kích thước: " + image.getSize() + " bytes");
            System.out.println("  - Content-Type: " + contentType);
            
            // Tạo thư mục uploads theo đường dẫn tuyệt đối đồng bộ với WebConfig/SecurityConfig
            File uploadsDir = new File("D:/harvest-hub-backend/uploads");
            if (!uploadsDir.exists()) {
                boolean created = uploadsDir.mkdirs();
                if (created) {
                    System.out.println("Đã tạo thư mục uploads tại: " + uploadsDir.getAbsolutePath());
                } else {
                    System.err.println("Không thể tạo thư mục uploads!");
                    return null;
                }
            } else {
                System.out.println("Thư mục uploads đã tồn tại tại: " + uploadsDir.getAbsolutePath());
            }
            
            File productsDir = new File(uploadsDir, "products");
            if (!productsDir.exists()) {
                boolean created = productsDir.mkdirs();
                if (created) {
                    System.out.println("Đã tạo thư mục uploads/products tại: " + productsDir.getAbsolutePath());
                } else {
                    System.err.println("Không thể tạo thư mục uploads/products!");
                    return null;
                }
            } else {
                System.out.println("Thư mục uploads/products đã tồn tại tại: " + productsDir.getAbsolutePath());
            }
            
            // Tạo tên file duy nhất
            String originalFilename = image.getOriginalFilename();
            System.out.println("Tên file gốc: " + originalFilename);
            
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                System.out.println("Extension: " + fileExtension);
            }
            if (fileExtension.isEmpty()) {
                if (contentType != null) {
                    if (contentType.equals("image/jpeg")) fileExtension = ".jpg";
                    else if (contentType.equals("image/png")) fileExtension = ".png";
                    else if (contentType.equals("image/gif")) fileExtension = ".gif";
                    else if (contentType.equals("image/webp")) fileExtension = ".webp";
                }
            }
            if (fileExtension.isEmpty()) {
                fileExtension = ".jpg";
            }
            
            String filename = "product_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000) + fileExtension;
            System.out.println("Tên file mới: " + filename);
            
            File file = new File(productsDir, filename);
            System.out.println("Đường dẫn file đầy đủ: " + file.getAbsolutePath());

            // Lưu file bằng NIO để tránh vấn đề với transferTo trên một số hệ thống
            System.out.println("Đang lưu file (NIO copy)...");
            try (java.io.InputStream in = image.getInputStream()) {
                java.nio.file.Files.copy(
                    in,
                    file.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
            System.out.println("File đã được lưu thành công (NIO)!");
            
            // Kiểm tra file có tồn tại không
            if (file.exists()) {
                System.out.println("File tồn tại sau khi lưu, kích thước: " + file.length() + " bytes");
            } else {
                System.err.println("CẢNH BÁO: File không tồn tại sau khi lưu!");
                return null;
            }
            
            // Trả về đường dẫn tương đối
            String relativePath = "/uploads/products/" + filename;
            System.out.println("Trả về đường dẫn: " + relativePath);
            return relativePath;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu ảnh: " + e.getMessage());
            System.err.println("Loại lỗi: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return null;
        }
    }

    // Endpoint để phục vụ ảnh
    @GetMapping("/uploads/products/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> serveImage(@PathVariable String filename) {
        try {
            File file = new File("D:/harvest-hub-backend/uploads/products/" + filename);
            System.out.println("Trying to serve image: " + file.getAbsolutePath());
            
            if (!file.exists()) {
                System.out.println("Image file not found: " + file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Image file found, serving: " + file.getAbsolutePath());
            org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);
            
        } catch (Exception e) {
            System.err.println("Error serving image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Health check for frontend connectivity testing
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "timestamp", java.time.LocalDateTime.now().toString()));
    }

    // ===== ENDPOINTS CHO VIỆC DUYỆT SẢN PHẨM =====
    
    // Lấy danh sách sản phẩm chờ duyệt
    @GetMapping("/pending")
    public ResponseEntity<List<Product>> getPendingProducts() {
        List<Product> pendingProducts = productService.getPendingProducts();
        // Normalize URL ảnh để hoạt động với mọi IP/hostname
        ImageUrlUtils.normalizeProducts(pendingProducts);
        return ResponseEntity.ok(pendingProducts);
    }

    // Duyệt sản phẩm
    @Operation(summary = "Approve product", description = "Approve a pending product (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product approved successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot approve product with current status")
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveProduct(
        @Parameter(description = "Product ID") @PathVariable String id, 
        @Parameter(description = "Admin ID") @RequestParam String adminId) {
        try {
            Product approvedProduct = productService.approveProduct(id, adminId);
            // Normalize URL ảnh để hoạt động với mọi IP/hostname
            ImageUrlUtils.normalizeProduct(approvedProduct);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã duyệt sản phẩm thành công",
                "product", approvedProduct
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Từ chối sản phẩm
    @Operation(summary = "Reject product", description = "Reject a pending product with reason (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product rejected successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot reject product or reason is missing")
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectProduct(
        @Parameter(description = "Product ID") @PathVariable String id, 
        @Parameter(description = "Admin ID") @RequestParam String adminId, 
        @Parameter(description = "Rejection reason (required)") @RequestParam(required = false) String reason) {
        try {
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Lý do từ chối là bắt buộc"
                ));
            }
            Product rejectedProduct = productService.rejectProduct(id, adminId, reason);
            // Normalize URL ảnh để hoạt động với mọi IP/hostname
            ImageUrlUtils.normalizeProduct(rejectedProduct);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã từ chối sản phẩm",
                "product", rejectedProduct
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
    
    // Resubmit sản phẩm đã bị từ chối
    @PutMapping("/{id}/resubmit")
    public ResponseEntity<Map<String, Object>> resubmitProduct(@PathVariable String id) {
        try {
            Product resubmittedProduct = productService.resubmitProduct(id);
            // Normalize URL ảnh để hoạt động với mọi IP/hostname
            ImageUrlUtils.normalizeProduct(resubmittedProduct);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã gửi lại sản phẩm để duyệt",
                "product", resubmittedProduct
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Lấy thống kê sản phẩm
    @GetMapping("/approval-stats")
    public ResponseEntity<Map<String, Object>> getProductApprovalStats() {
        Map<String, Object> stats = productService.getProductApprovalStats();
        return ResponseEntity.ok(stats);
    }
    
}
