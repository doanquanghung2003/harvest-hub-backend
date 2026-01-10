package com.example.harvesthubbackend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.example.harvesthubbackend.Service.SellerService;
import com.example.harvesthubbackend.Service.UserService;
import com.example.harvesthubbackend.Models.Seller;
import com.example.harvesthubbackend.Models.User;

@RestController
@RequestMapping({"/api/products", "/api/v1/products"})
@CrossOrigin(origins = "*")
@Tag(name = "Products", description = "API endpoints for product management")
public class ProductController {
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SellerService sellerService;
    
    @Autowired
    private UserService userService;

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
        @Parameter(description = "Return as array (no pagination)", example = "false") @RequestParam(defaultValue = "false") boolean asArray,
        @Parameter(description = "Filter by category", example = "Trái cây") @RequestParam(required = false) String category) {
        List<Product> allProducts;
        if (category != null && !category.trim().isEmpty()) {
            allProducts = productService.getByCategory(category.trim());
            System.out.println("=== ProductController.getAll() with category filter ===");
            System.out.println("Category filter: " + category);
            System.out.println("Filtered products found: " + (allProducts != null ? allProducts.size() : 0));
        } else {
            allProducts = productService.getAll();
        }
        
        System.out.println("=== ProductController.getAll() ===");
        System.out.println("Total products found: " + (allProducts != null ? allProducts.size() : 0));
        if (allProducts != null && !allProducts.isEmpty()) {
            System.out.println("First product: " + allProducts.get(0).getName() + " (ID: " + allProducts.get(0).getId() + ", Status: " + allProducts.get(0).getStatus() + ")");
        }
        
        // Normalize URL ảnh để hoạt động với mọi IP/hostname
        ImageUrlUtils.normalizeProducts(allProducts);
        
        // Nếu frontend yêu cầu array trực tiếp (cho admin dashboard)
        if (asArray) {
            return ResponseEntity.ok(allProducts);
        }
        
        // Mặc định trả về PageResponse (có pagination)
        int pageNum = PaginationUtils.parsePage(page);
        int pageSize = PaginationUtils.parseSize(size);
        Object result = PaginationUtils.paginate(allProducts, pageNum, pageSize);
        System.out.println("Returning paginated result with " + (allProducts != null ? allProducts.size() : 0) + " total products");
        return result;
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
        @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") String size) {
        System.out.println("=== ProductController.getProductsBySeller() ===");
        System.out.println("Request sellerId: " + sellerId);
        
        List<Product> sellerProducts = productService.getBySellerId(sellerId);
        
        System.out.println("Found " + (sellerProducts != null ? sellerProducts.size() : 0) + " products for sellerId: " + sellerId);
        if (sellerProducts != null && !sellerProducts.isEmpty()) {
            System.out.println("Sample product sellerIds:");
            sellerProducts.stream().limit(3).forEach(p -> {
                System.out.println("  - Product: " + p.getName() + " (sellerId: " + p.getSellerId() + ")");
            });
        }
        
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
                                   @RequestParam(value = "weight", required = false) String weight,
                                   @RequestParam(value = "dimensions", required = false) String dimensions,
                                   @RequestParam(value = "specifications", required = false) String specifications,
                                   @RequestParam(value = "origin", required = false) String origin,
                                   @RequestParam(value = "unit", required = false) String unit,
                                   @RequestParam(value = "expiryDate", required = false) String expiryDate,
                                   @RequestParam(value = "storageInstructions", required = false) String storageInstructions,
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
            
            // Xử lý weight (String)
            if (weight != null && !weight.trim().isEmpty()) {
                product.setWeight(weight.trim());
                System.out.println("✅ Đã set weight: " + weight);
            }
            
            // Xử lý origin
            if (origin != null && !origin.trim().isEmpty()) {
                product.setOrigin(origin.trim());
                System.out.println("✅ Đã set origin: " + origin);
            }
            
            // Xử lý unit
            if (unit != null && !unit.trim().isEmpty()) {
                product.setUnit(unit.trim());
                System.out.println("✅ Đã set unit: " + unit);
            }
            
            // Xử lý expiryDate
            if (expiryDate != null && !expiryDate.trim().isEmpty()) {
                product.setExpiryDate(expiryDate.trim());
                System.out.println("✅ Đã set expiryDate: " + expiryDate);
            }
            
            // Xử lý storageInstructions
            if (storageInstructions != null && !storageInstructions.trim().isEmpty()) {
                product.setStorageInstructions(storageInstructions.trim());
                System.out.println("✅ Đã set storageInstructions: " + storageInstructions);
            }
            
            // Xử lý specifications (parse từ JSON string)
            if (specifications != null && !specifications.trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> specsMap = objectMapper.readValue(specifications, 
                        new TypeReference<Map<String, String>>() {});
                    product.setSpecifications(specsMap);
                    System.out.println("✅ Đã set specifications: " + specsMap);
                } catch (Exception e) {
                    System.err.println("⚠️ Lỗi khi parse specifications JSON: " + e.getMessage());
                    e.printStackTrace();
                    // Nếu parse lỗi, vẫn tiếp tục (không bắt buộc)
                }
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
            // Kiểm tra authentication
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Bạn cần đăng nhập để thực hiện thao tác này",
                    "error", Map.of("code", 1001, "message", "Unauthorized")
                ));
            }
            
            // Kiểm tra sản phẩm có tồn tại không
            Product existingProduct = productService.getById(id);
            if (existingProduct == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Kiểm tra quyền: ADMIN hoặc seller sở hữu sản phẩm
            String username = authentication.getName();
            User currentUser = userService.getByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy thông tin người dùng",
                    "error", Map.of("code", 1001, "message", "User not found")
                ));
            }
            
            // Kiểm tra nếu là ADMIN thì cho phép
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                // Kiểm tra nếu user là seller của sản phẩm này
                Seller seller = sellerService.getSellerByUserId(currentUser.getId());
                if (seller == null || !seller.getId().equals(existingProduct.getSellerId())) {
                    return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền truy cập tài nguyên này",
                        "error", Map.of("code", 1004, "message", "Bạn không có quyền truy cập tài nguyên này")
                    ));
                }
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
            // Kiểm tra authentication
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Bạn cần đăng nhập để thực hiện thao tác này",
                    "error", Map.of("code", 1001, "message", "Unauthorized")
                ));
            }
            
            Product existing = productService.getById(id);
            if (existing == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Kiểm tra quyền: ADMIN hoặc seller sở hữu sản phẩm
            String username = authentication.getName();
            User currentUser = userService.getByUsername(username);
            
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Không tìm thấy thông tin người dùng",
                    "error", Map.of("code", 1001, "message", "User not found")
                ));
            }
            
            // Kiểm tra nếu là ADMIN thì cho phép
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                // Kiểm tra nếu user là seller của sản phẩm này
                Seller seller = sellerService.getSellerByUserId(currentUser.getId());
                if (seller == null || !seller.getId().equals(existing.getSellerId())) {
                    return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Bạn không có quyền truy cập tài nguyên này",
                        "error", Map.of("code", 1004, "message", "Bạn không có quyền truy cập tài nguyên này")
                    ));
                }
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

    // Endpoint test để thêm sản phẩm mẫu (chỉ dùng cho development)
    @PostMapping("/test-add-sample")
    public ResponseEntity<?> addSampleProduct(@RequestParam(value = "username", required = false) String username) {
        try {
            System.out.println("=== TẠO SẢN PHẨM MẪU ===");
            
            // Lấy sellerId từ username
            String sellerId = null;
            if (username != null && !username.trim().isEmpty()) {
                User user = userService.getByUsername(username.trim());
                if (user != null) {
                    Seller seller = sellerService.getSellerByUserId(user.getId());
                    if (seller != null) {
                        sellerId = seller.getId();
                        System.out.println("✅ Tìm thấy sellerId: " + sellerId + " cho username: " + username);
                    } else {
                        System.out.println("⚠️ Không tìm thấy seller cho username: " + username);
                    }
                } else {
                    System.out.println("⚠️ Không tìm thấy user với username: " + username);
                }
            }
            
            if (sellerId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không tìm thấy seller",
                    "message", "Vui lòng cung cấp username hợp lệ hoặc đảm bảo user đã đăng ký làm seller"
                ));
            }
            
            Product product = new Product();
            product.setName("Bông cải xanh tươi");
            product.setShortDescription("Súp lơ xanh tươi ngon");
            product.setDescription("Bông cải xanh là một loại rau thuộc họ cải, được sử dụng làm thực phẩm, thường được luộc hoặc hấp, nhưng cũng có thể ăn sống trong salad.");
            product.setCategory("Rau Củ");
            product.setPrice(20000.0);
            product.setStock(123);
            product.setWeight("1");
            product.setUnit("kg");
            product.setOrigin("VN");
            product.setExpiryDate("1 tháng");
            product.setStorageInstructions("Bảo quản nơi khô ráo và thoáng mát");
            product.setStatus("active");
            
            // Tạo specifications
            Map<String, String> specs = new java.util.HashMap<>();
            specs.put("Kích thước", "10x10");
            specs.put("Thành phần", "Rau xanh");
            specs.put("Thương hiệu", "Việt Grap");
            product.setSpecifications(specs);
            
            // Set sellerId đúng
            product.setSellerId(sellerId);
            
            // Set thời gian
            product.setCreatedAt(java.time.LocalDateTime.now());
            product.setUpdatedAt(java.time.LocalDateTime.now());
            
            // Lưu sản phẩm
            Product savedProduct = productService.create(product);
            
            System.out.println("✅ Sản phẩm mẫu đã được tạo với ID: " + savedProduct.getId() + " cho sellerId: " + sellerId);
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Sản phẩm mẫu đã được tạo thành công");
            response.put("product", savedProduct);
            response.put("sellerId", sellerId);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo sản phẩm mẫu: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm mẫu");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Endpoint để thêm nhiều sản phẩm rau củ Việt Nam
    @PostMapping("/test-add-vietnam-vegetables")
    public ResponseEntity<?> addVietnamVegetables(@RequestParam(value = "username", required = false) String username) {
        try {
            System.out.println("=== TẠO NHIỀU SẢN PHẨM RAU CỦ VIỆT NAM ===");
            
            // Lấy sellerId từ username
            String sellerId = null;
            if (username != null && !username.trim().isEmpty()) {
                User user = userService.getByUsername(username.trim());
                if (user != null) {
                    Seller seller = sellerService.getSellerByUserId(user.getId());
                    if (seller != null) {
                        sellerId = seller.getId();
                        System.out.println("✅ Tìm thấy sellerId: " + sellerId + " cho username: " + username);
                    } else {
                        System.out.println("⚠️ Không tìm thấy seller cho username: " + username);
                    }
                } else {
                    System.out.println("⚠️ Không tìm thấy user với username: " + username);
                }
            }
            
            if (sellerId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không tìm thấy seller",
                    "message", "Vui lòng cung cấp username hợp lệ (ví dụ: ?username=hungbanhang)"
                ));
            }
            
            // Danh sách 50 loại rau củ phổ biến ở Việt Nam
            List<Map<String, Object>> vegetables = new ArrayList<>();
            
            // Rau lá
            vegetables.add(Map.of("name", "Rau muống", "shortDescription", "Rau muống tươi xanh", "description", "Rau muống là loại rau phổ biến ở Việt Nam, thường được luộc, xào tỏi hoặc nấu canh chua.", "price", 10000.0, "stock", 300, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau cải ngọt", "shortDescription", "Rau cải ngọt tươi xanh", "description", "Rau cải ngọt có vị ngọt tự nhiên, thường được luộc, xào hoặc nấu canh. Giàu vitamin và khoáng chất.", "price", 12000.0, "stock", 250, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau cải xanh", "shortDescription", "Rau cải xanh tươi", "description", "Rau cải xanh giàu vitamin K, thường được xào hoặc nấu canh.", "price", 11000.0, "stock", 280, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau cải thìa", "shortDescription", "Rau cải thìa tươi", "description", "Rau cải thìa có vị ngọt, giòn, thường được xào hoặc luộc.", "price", 13000.0, "stock", 200, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau mồng tơi", "shortDescription", "Rau mồng tơi tươi", "description", "Rau mồng tơi có tính mát, thường được nấu canh với cua hoặc tôm.", "price", 12000.0, "stock", 150, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau dền", "shortDescription", "Rau dền tươi", "description", "Rau dền giàu sắt và canxi, thường được nấu canh.", "price", 10000.0, "stock", 180, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau lang", "shortDescription", "Rau lang tươi", "description", "Lá khoai lang non, thường được xào hoặc luộc.", "price", 8000.0, "stock", 200, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau đay", "shortDescription", "Rau đay tươi", "description", "Rau đay có tính mát, thường nấu canh cua.", "price", 10000.0, "stock", 120, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau ngót", "shortDescription", "Rau ngót tươi", "description", "Rau ngót giàu protein, thường nấu canh với thịt băm.", "price", 15000.0, "stock", 100, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau má", "shortDescription", "Rau má tươi", "description", "Rau má có tính mát, thường làm nước ép hoặc nấu canh.", "price", 20000.0, "stock", 80, "weight", "0.5", "unit", "kg"));
            
            // Cải bắp và họ cải
            vegetables.add(Map.of("name", "Cải bắp", "shortDescription", "Bắp cải tươi giòn", "description", "Bắp cải là loại rau giàu vitamin K và C. Có thể làm salad, nấu canh, xào hoặc muối chua.", "price", 15000.0, "stock", 120, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Bông cải xanh", "shortDescription", "Súp lơ xanh tươi ngon", "description", "Bông cải xanh là một loại rau thuộc họ cải, được sử dụng làm thực phẩm, thường được luộc hoặc hấp.", "price", 20000.0, "stock", 123, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Súp lơ trắng", "shortDescription", "Súp lơ trắng tươi", "description", "Súp lơ trắng giàu vitamin C, thường được luộc hoặc xào.", "price", 18000.0, "stock", 100, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải thảo", "shortDescription", "Cải thảo tươi", "description", "Cải thảo giòn ngọt, thường được nấu canh hoặc muối chua.", "price", 16000.0, "stock", 90, "weight", "1", "unit", "kg"));
            
            // Cà và ớt
            vegetables.add(Map.of("name", "Cà chua", "shortDescription", "Cà chua chín đỏ, tươi ngon", "description", "Cà chua là loại quả giàu lycopene và vitamin C. Dùng để nấu canh, làm salad, hoặc ăn sống.", "price", 18000.0, "stock", 150, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Cà tím", "shortDescription", "Cà tím tươi ngon", "description", "Cà tím giàu chất xơ và chất chống oxy hóa. Thường được nướng, xào hoặc nấu canh.", "price", 20000.0, "stock", 80, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Cà pháo", "shortDescription", "Cà pháo tươi", "description", "Cà pháo thường được muối chua hoặc nấu canh.", "price", 15000.0, "stock", 100, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Ớt chuông", "shortDescription", "Ớt chuông đỏ, vàng, xanh", "description", "Ớt chuông giàu vitamin C và chất chống oxy hóa. Có thể xào, nướng hoặc ăn sống trong salad.", "price", 35000.0, "stock", 60, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Ớt chỉ thiên", "shortDescription", "Ớt chỉ thiên cay", "description", "Ớt chỉ thiên cay nồng, dùng làm gia vị.", "price", 40000.0, "stock", 50, "weight", "0.2", "unit", "kg"));
            vegetables.add(Map.of("name", "Ớt sừng", "shortDescription", "Ớt sừng tươi", "description", "Ớt sừng vừa cay, thường dùng xào hoặc làm gia vị.", "price", 30000.0, "stock", 70, "weight", "0.3", "unit", "kg"));
            
            // Củ và rễ
            vegetables.add(Map.of("name", "Cà rốt", "shortDescription", "Cà rốt tươi ngon, giàu vitamin A", "description", "Cà rốt là loại củ giàu beta-carotene, tốt cho mắt và sức khỏe. Có thể ăn sống, luộc, xào hoặc làm nước ép.", "price", 15000.0, "stock", 200, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ cải trắng", "shortDescription", "Củ cải trắng tươi", "description", "Củ cải trắng có vị ngọt, thường nấu canh hoặc muối chua.", "price", 12000.0, "stock", 150, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ cải đỏ", "shortDescription", "Củ cải đỏ tươi", "description", "Củ cải đỏ giàu chất chống oxy hóa, có thể ăn sống hoặc nấu.", "price", 18000.0, "stock", 100, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ đậu", "shortDescription", "Củ đậu tươi giòn", "description", "Củ đậu mát ngọt, có thể ăn sống hoặc nấu canh.", "price", 10000.0, "stock", 120, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ hành tây", "shortDescription", "Hành tây tươi", "description", "Hành tây là gia vị quan trọng trong nấu ăn, có thể xào, nướng hoặc ăn sống. Giàu chất chống oxy hóa.", "price", 18000.0, "stock", 200, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ tỏi", "shortDescription", "Tỏi tươi, thơm", "description", "Tỏi là gia vị không thể thiếu trong ẩm thực Việt Nam. Có tác dụng kháng khuẩn và tăng cường miễn dịch.", "price", 40000.0, "stock", 150, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ gừng", "shortDescription", "Gừng tươi", "description", "Gừng có tính ấm, dùng làm gia vị và thuốc.", "price", 50000.0, "stock", 100, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ nghệ", "shortDescription", "Nghệ tươi", "description", "Nghệ giàu curcumin, có tác dụng chống viêm.", "price", 60000.0, "stock", 80, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Củ riềng", "shortDescription", "Riềng tươi", "description", "Riềng có mùi thơm đặc trưng, dùng làm gia vị.", "price", 45000.0, "stock", 90, "weight", "0.5", "unit", "kg"));
            
            // Khoai và bí
            vegetables.add(Map.of("name", "Khoai tây", "shortDescription", "Khoai tây tươi", "description", "Khoai tây giàu tinh bột và kali. Có thể luộc, chiên, nướng hoặc làm khoai tây nghiền.", "price", 22000.0, "stock", 100, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Khoai lang", "shortDescription", "Khoai lang tươi ngon", "description", "Khoai lang giàu beta-carotene và chất xơ. Có thể luộc, nướng hoặc làm chè.", "price", 15000.0, "stock", 120, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Khoai môn", "shortDescription", "Khoai môn tươi", "description", "Khoai môn béo ngậy, thường nấu canh hoặc làm chè.", "price", 20000.0, "stock", 80, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Khoai sọ", "shortDescription", "Khoai sọ tươi", "description", "Khoai sọ mềm dẻo, thường nấu canh.", "price", 18000.0, "stock", 70, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Bí đỏ", "shortDescription", "Bí đỏ tươi ngon", "description", "Bí đỏ giàu beta-carotene và vitamin A. Thường được nấu canh, hầm hoặc làm chè.", "price", 18000.0, "stock", 90, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Bí xanh", "shortDescription", "Bí xanh tươi", "description", "Bí xanh mát lành, thường nấu canh hoặc xào.", "price", 12000.0, "stock", 110, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Bí đao", "shortDescription", "Bí đao tươi", "description", "Bí đao có tính mát, thường nấu canh.", "price", 10000.0, "stock", 130, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Mướp", "shortDescription", "Mướp tươi", "description", "Mướp non thường nấu canh hoặc xào.", "price", 15000.0, "stock", 100, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Bầu", "shortDescription", "Bầu tươi", "description", "Bầu non thường nấu canh.", "price", 12000.0, "stock", 90, "weight", "1", "unit", "kg"));
            
            // Đậu và đỗ
            vegetables.add(Map.of("name", "Đậu cô ve", "shortDescription", "Đậu cô ve tươi xanh", "description", "Đậu cô ve là loại đậu non, mềm, thường được xào hoặc luộc. Giàu protein và chất xơ.", "price", 25000.0, "stock", 100, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Đậu đũa", "shortDescription", "Đậu đũa tươi", "description", "Đậu đũa dài, thường xào hoặc luộc.", "price", 20000.0, "stock", 120, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Đậu bắp", "shortDescription", "Đậu bắp tươi", "description", "Đậu bắp có chất nhầy, thường luộc hoặc nấu canh chua.", "price", 18000.0, "stock", 80, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Đậu rồng", "shortDescription", "Đậu rồng tươi", "description", "Đậu rồng có hình dạng đặc biệt, thường xào hoặc luộc.", "price", 30000.0, "stock", 60, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Đậu Hà Lan", "shortDescription", "Đậu Hà Lan tươi", "description", "Đậu Hà Lan ngọt, thường xào hoặc nấu canh.", "price", 35000.0, "stock", 70, "weight", "0.5", "unit", "kg"));
            
            // Rau gia vị và thảo mộc
            vegetables.add(Map.of("name", "Hành lá", "shortDescription", "Hành lá tươi", "description", "Hành lá dùng làm gia vị, trang trí món ăn.", "price", 15000.0, "stock", 200, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Ngò rí", "shortDescription", "Ngò rí tươi", "description", "Ngò rí thơm, dùng làm gia vị.", "price", 12000.0, "stock", 150, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Rau mùi", "shortDescription", "Rau mùi tươi", "description", "Rau mùi thơm đặc trưng, dùng làm gia vị.", "price", 10000.0, "stock", 180, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Rau thơm", "shortDescription", "Rau thơm tươi", "description", "Hỗn hợp các loại rau thơm, dùng ăn kèm.", "price", 15000.0, "stock", 100, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Tía tô", "shortDescription", "Tía tô tươi", "description", "Lá tía tô có mùi thơm, dùng ăn kèm hoặc nấu canh.", "price", 18000.0, "stock", 90, "weight", "0.2", "unit", "bó"));
            
            // Các loại khác
            vegetables.add(Map.of("name", "Dưa chuột", "shortDescription", "Dưa chuột tươi giòn, mát lạnh", "description", "Dưa chuột có tính mát, giàu nước và vitamin. Thường dùng để ăn sống, làm salad hoặc muối chua.", "price", 12000.0, "stock", 180, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Khổ qua", "shortDescription", "Khổ qua tươi", "description", "Khổ qua có vị đắng, thường xào hoặc nấu canh.", "price", 20000.0, "stock", 80, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Su su", "shortDescription", "Su su tươi", "description", "Su su giòn ngọt, thường xào hoặc nấu canh.", "price", 15000.0, "stock", 100, "weight", "1", "unit", "kg"));
            vegetables.add(Map.of("name", "Cà pháo muối", "shortDescription", "Cà pháo muối chua", "description", "Cà pháo đã được muối chua, ăn kèm với các món ăn.", "price", 25000.0, "stock", 50, "weight", "0.5", "unit", "kg"));
            
            // Thêm các loại rau củ khác để đủ 50 sản phẩm
            vegetables.add(Map.of("name", "Rau cần", "shortDescription", "Rau cần tươi", "description", "Rau cần giòn ngọt, thường xào hoặc nấu canh.", "price", 14000.0, "stock", 110, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau diếp cá", "shortDescription", "Rau diếp cá tươi", "description", "Rau diếp cá có mùi đặc trưng, dùng ăn kèm.", "price", 16000.0, "stock", 95, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Rau răm", "shortDescription", "Rau răm tươi", "description", "Rau răm thơm, dùng ăn kèm với các món ăn.", "price", 12000.0, "stock", 85, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Rau kinh giới", "shortDescription", "Rau kinh giới tươi", "description", "Rau kinh giới thơm, dùng ăn kèm.", "price", 13000.0, "stock", 75, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Rau húng quế", "shortDescription", "Rau húng quế tươi", "description", "Rau húng quế thơm, dùng làm gia vị.", "price", 11000.0, "stock", 90, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Rau húng lủi", "shortDescription", "Rau húng lủi tươi", "description", "Rau húng lủi thơm mát, dùng ăn kèm.", "price", 14000.0, "stock", 80, "weight", "0.2", "unit", "bó"));
            vegetables.add(Map.of("name", "Rau đắng", "shortDescription", "Rau đắng tươi", "description", "Rau đắng có vị đắng, thường nấu canh.", "price", 15000.0, "stock", 70, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau sam", "shortDescription", "Rau sam tươi", "description", "Rau sam mát, có thể xào hoặc nấu canh.", "price", 12000.0, "stock", 60, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau mầm", "shortDescription", "Rau mầm tươi", "description", "Rau mầm non, giàu dinh dưỡng, thường ăn sống.", "price", 30000.0, "stock", 40, "weight", "0.2", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau chân vịt", "shortDescription", "Rau chân vịt tươi", "description", "Rau chân vịt giàu sắt, thường xào hoặc nấu canh.", "price", 20000.0, "stock", 85, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau xà lách", "shortDescription", "Rau xà lách tươi", "description", "Rau xà lách giòn, thường làm salad.", "price", 18000.0, "stock", 95, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau cải xoong", "shortDescription", "Rau cải xoong tươi", "description", "Rau cải xoong cay nhẹ, thường ăn sống hoặc nấu canh.", "price", 22000.0, "stock", 75, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau cải cúc", "shortDescription", "Rau cải cúc tươi", "description", "Rau cải cúc thơm, thường nấu canh.", "price", 16000.0, "stock", 80, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Rau tần ô", "shortDescription", "Rau tần ô tươi", "description", "Rau tần ô thơm, thường nấu canh.", "price", 17000.0, "stock", 70, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bó xôi", "shortDescription", "Cải bó xôi tươi", "description", "Cải bó xôi giàu sắt, thường xào hoặc nấu canh.", "price", 25000.0, "stock", 65, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải thìa", "shortDescription", "Cải thìa tươi", "description", "Cải thìa giòn ngọt, thường xào hoặc luộc.", "price", 14000.0, "stock", 100, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải xoăn", "shortDescription", "Cải xoăn tươi", "description", "Cải xoăn giàu vitamin, thường xào hoặc nấu canh.", "price", 20000.0, "stock", 75, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải chip", "shortDescription", "Cải chip tươi", "description", "Cải chip giòn, thường xào hoặc luộc.", "price", 18000.0, "stock", 85, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ xanh", "shortDescription", "Cải bẹ xanh tươi", "description", "Cải bẹ xanh cay nhẹ, thường nấu canh.", "price", 12000.0, "stock", 90, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ trắng", "shortDescription", "Cải bẹ trắng tươi", "description", "Cải bẹ trắng thường muối chua hoặc nấu canh.", "price", 11000.0, "stock", 95, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ đỏ", "shortDescription", "Cải bẹ đỏ tươi", "description", "Cải bẹ đỏ có màu đỏ đẹp, thường nấu canh.", "price", 13000.0, "stock", 88, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải rổ", "shortDescription", "Cải rổ tươi", "description", "Cải rổ giòn, thường xào hoặc luộc.", "price", 15000.0, "stock", 82, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ dưa", "shortDescription", "Cải bẹ dưa tươi", "description", "Cải bẹ dưa thường muối chua.", "price", 10000.0, "stock", 100, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải cầu vồng", "shortDescription", "Cải cầu vồng tươi", "description", "Cải cầu vồng có nhiều màu sắc, thường xào hoặc nấu canh.", "price", 22000.0, "stock", 68, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ mèo", "shortDescription", "Cải bẹ mèo tươi", "description", "Cải bẹ mèo thơm, thường nấu canh.", "price", 14000.0, "stock", 78, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ xôi", "shortDescription", "Cải bẹ xôi tươi", "description", "Cải bẹ xôi thường nấu canh hoặc xào.", "price", 13000.0, "stock", 85, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ vàng", "shortDescription", "Cải bẹ vàng tươi", "description", "Cải bẹ vàng có màu vàng, thường nấu canh.", "price", 15000.0, "stock", 72, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ tím", "shortDescription", "Cải bẹ tím tươi", "description", "Cải bẹ tím có màu tím, thường nấu canh.", "price", 16000.0, "stock", 70, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ xanh lá", "shortDescription", "Cải bẹ xanh lá tươi", "description", "Cải bẹ xanh lá thường nấu canh.", "price", 12000.0, "stock", 88, "weight", "0.5", "unit", "kg"));
            vegetables.add(Map.of("name", "Cải bẹ xanh thân", "shortDescription", "Cải bẹ xanh thân tươi", "description", "Cải bẹ xanh thân thường nấu canh hoặc xào.", "price", 11000.0, "stock", 92, "weight", "0.5", "unit", "kg"));
            
            List<Product> createdProducts = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> veg : vegetables) {
                try {
                    String productName = (String) veg.get("name");
                    
                    // Kiểm tra xem sản phẩm đã tồn tại chưa (theo tên và sellerId)
                    List<Product> existingProducts = productService.getBySellerId(sellerId);
                    boolean exists = false;
                    for (Product existing : existingProducts) {
                        if (existing.getName() != null && existing.getName().equals(productName)) {
                            exists = true;
                            System.out.println("⚠️ Sản phẩm '" + productName + "' đã tồn tại, bỏ qua...");
                            break;
                        }
                    }
                    
                    if (exists) {
                        failCount++;
                        continue;
                    }
                    
                    Product product = new Product();
                    product.setName(productName);
                    product.setShortDescription((String) veg.get("shortDescription"));
                    product.setDescription((String) veg.get("description"));
                    product.setCategory("Rau Củ");
                    product.setPrice(((Number) veg.get("price")).doubleValue());
                    product.setStock(((Number) veg.get("stock")).intValue());
                    product.setWeight((String) veg.get("weight"));
                    product.setUnit((String) veg.get("unit"));
                    product.setOrigin("VN");
                    product.setExpiryDate("3-7 ngày");
                    product.setStorageInstructions("Bảo quản nơi khô ráo và thoáng mát, tránh ánh nắng trực tiếp");
                    product.setStatus("active");
                    
                    // Tạo specifications
                    Map<String, String> specs = new java.util.HashMap<>();
                    specs.put("Xuất xứ", "Việt Nam");
                    specs.put("Loại", "Rau củ tươi");
                    product.setSpecifications(specs);
                    
                    // Set sellerId
                    product.setSellerId(sellerId);
                    
                    // Set thời gian
                    product.setCreatedAt(java.time.LocalDateTime.now());
                    product.setUpdatedAt(java.time.LocalDateTime.now());
                    
                    // Lưu sản phẩm
                    Product savedProduct = productService.create(product);
                    createdProducts.add(savedProduct);
                    successCount++;
                    System.out.println("✅ Đã tạo: " + product.getName() + " (ID: " + savedProduct.getId() + ")");
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Lỗi khi tạo " + veg.get("name") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Đã tạo " + successCount + " sản phẩm thành công");
            response.put("total", vegetables.size());
            response.put("success", successCount);
            response.put("failed", failCount);
            response.put("sellerId", sellerId);
            response.put("products", createdProducts);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            System.out.println("✅ Hoàn thành! Đã tạo " + successCount + "/" + vegetables.size() + " sản phẩm");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo sản phẩm: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Endpoint để thêm nhiều sản phẩm trái cây Việt Nam
    @PostMapping("/test-add-vietnam-fruits")
    public ResponseEntity<?> addVietnamFruits(@RequestParam(value = "username", required = false) String username) {
        try {
            System.out.println("=== TẠO NHIỀU SẢN PHẨM TRÁI CÂY VIỆT NAM ===");
            
            // Lấy sellerId từ username
            String sellerId = null;
            if (username != null && !username.trim().isEmpty()) {
                User user = userService.getByUsername(username.trim());
                if (user != null) {
                    Seller seller = sellerService.getSellerByUserId(user.getId());
                    if (seller != null) {
                        sellerId = seller.getId();
                        System.out.println("✅ Tìm thấy sellerId: " + sellerId + " cho username: " + username);
                    } else {
                        System.out.println("⚠️ Không tìm thấy seller cho username: " + username);
                    }
                } else {
                    System.out.println("⚠️ Không tìm thấy user với username: " + username);
                }
            }
            
            if (sellerId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không tìm thấy seller",
                    "message", "Vui lòng cung cấp username hợp lệ (ví dụ: ?username=hungbanhang)"
                ));
            }
            
            // Danh sách 50 loại trái cây phổ biến ở Việt Nam
            List<Map<String, Object>> fruits = new ArrayList<>();
            
            // Trái cây nhiệt đới phổ biến
            fruits.add(Map.of("name", "Chuối", "shortDescription", "Chuối tươi ngon", "description", "Chuối là loại trái cây giàu kali và vitamin B6, rất tốt cho sức khỏe tim mạch và tiêu hóa.", "price", 15000.0, "stock", 200, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Xoài", "shortDescription", "Xoài chín vàng", "description", "Xoài giàu vitamin C và A, có vị ngọt thơm đặc trưng. Có thể ăn chín hoặc làm sinh tố.", "price", 25000.0, "stock", 150, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Cam", "shortDescription", "Cam tươi ngọt", "description", "Cam giàu vitamin C, tăng cường miễn dịch. Có thể ăn trực tiếp hoặc vắt nước.", "price", 20000.0, "stock", 180, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Bưởi", "shortDescription", "Bưởi tươi ngon", "description", "Bưởi giàu vitamin C và chất xơ, giúp giảm cân và tốt cho tim mạch.", "price", 30000.0, "stock", 100, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Ổi", "shortDescription", "Ổi tươi giòn", "description", "Ổi giàu vitamin C gấp 4 lần cam, có vị ngọt giòn, tốt cho tiêu hóa.", "price", 18000.0, "stock", 160, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Dứa", "shortDescription", "Dứa tươi ngọt", "description", "Dứa giàu bromelain, giúp tiêu hóa và chống viêm. Có thể ăn trực tiếp hoặc làm nước ép.", "price", 22000.0, "stock", 120, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Đu đủ", "shortDescription", "Đu đủ chín vàng", "description", "Đu đủ giàu papain, tốt cho tiêu hóa. Có thể ăn chín hoặc làm sinh tố.", "price", 20000.0, "stock", 140, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Thanh long", "shortDescription", "Thanh long tươi", "description", "Thanh long giàu chất chống oxy hóa và chất xơ, có vị ngọt nhẹ.", "price", 35000.0, "stock", 80, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Dưa hấu", "shortDescription", "Dưa hấu tươi mát", "description", "Dưa hấu giàu nước và lycopene, giải nhiệt tốt trong mùa hè.", "price", 15000.0, "stock", 200, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Mít", "shortDescription", "Mít chín thơm", "description", "Mít có mùi thơm đặc trưng, giàu vitamin C và chất xơ.", "price", 30000.0, "stock", 60, "weight", "1", "unit", "kg"));
            
            // Trái cây có múi
            fruits.add(Map.of("name", "Quýt", "shortDescription", "Quýt tươi ngọt", "description", "Quýt nhỏ gọn, dễ bóc, giàu vitamin C và chất chống oxy hóa.", "price", 25000.0, "stock", 130, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chanh", "shortDescription", "Chanh tươi", "description", "Chanh giàu vitamin C, dùng làm gia vị, nước chấm hoặc nước giải khát.", "price", 20000.0, "stock", 150, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chanh dây", "shortDescription", "Chanh dây tươi", "description", "Chanh dây có vị chua ngọt, giàu vitamin C và chất chống oxy hóa.", "price", 40000.0, "stock", 70, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Bưởi da xanh", "shortDescription", "Bưởi da xanh tươi", "description", "Bưởi da xanh ngọt, ít hạt, giàu vitamin C.", "price", 35000.0, "stock", 90, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Bưởi năm roi", "shortDescription", "Bưởi năm roi tươi", "description", "Bưởi năm roi ngọt, mọng nước, thơm.", "price", 32000.0, "stock", 85, "weight", "1", "unit", "kg"));
            
            // Trái cây nhiệt đới đặc sản
            fruits.add(Map.of("name", "Sầu riêng", "shortDescription", "Sầu riêng chín thơm", "description", "Sầu riêng có mùi thơm đặc trưng, giàu chất béo và năng lượng.", "price", 120000.0, "stock", 30, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Măng cụt", "shortDescription", "Măng cụt tươi", "description", "Măng cụt có vị ngọt thanh, giàu chất chống oxy hóa.", "price", 80000.0, "stock", 40, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Nhãn", "shortDescription", "Nhãn tươi ngọt", "description", "Nhãn có vị ngọt, mọng nước, giàu vitamin C và khoáng chất.", "price", 40000.0, "stock", 100, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Vải thiều", "shortDescription", "Vải thiều tươi", "description", "Vải thiều ngọt, thơm, giàu vitamin C và chất chống oxy hóa.", "price", 45000.0, "stock", 90, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chôm chôm", "shortDescription", "Chôm chôm tươi", "description", "Chôm chôm có vị ngọt, mọng nước, giàu vitamin C.", "price", 35000.0, "stock", 80, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Rambutan", "shortDescription", "Rambutan tươi", "description", "Rambutan (chôm chôm) có vị ngọt, mọng nước.", "price", 40000.0, "stock", 75, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Mận", "shortDescription", "Mận tươi", "description", "Mận có vị chua ngọt, giàu vitamin C và chất xơ.", "price", 30000.0, "stock", 110, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Mơ", "shortDescription", "Mơ tươi", "description", "Mơ có vị chua ngọt, thường dùng làm mứt hoặc ăn tươi.", "price", 50000.0, "stock", 60, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Mận hậu", "shortDescription", "Mận hậu tươi", "description", "Mận hậu có vị chua ngọt đặc trưng, giàu vitamin.", "price", 35000.0, "stock", 95, "weight", "1", "unit", "kg"));
            
            // Trái cây dân dã
            fruits.add(Map.of("name", "Cóc", "shortDescription", "Cóc tươi", "description", "Cóc có vị chua, thường chấm muối ớt, giàu vitamin C.", "price", 20000.0, "stock", 120, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Có múi", "shortDescription", "Có múi tươi", "description", "Có múi có vị chua ngọt, thường dùng làm nước giải khát.", "price", 18000.0, "stock", 130, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Me", "shortDescription", "Me tươi", "description", "Me có vị chua, dùng làm nước giải khát hoặc gia vị.", "price", 25000.0, "stock", 100, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Táo", "shortDescription", "Táo tươi giòn", "description", "Táo giàu chất xơ và vitamin C, tốt cho tiêu hóa.", "price", 40000.0, "stock", 90, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Lê", "shortDescription", "Lê tươi ngọt", "description", "Lê có vị ngọt thanh, mọng nước, giàu chất xơ.", "price", 35000.0, "stock", 85, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Nho", "shortDescription", "Nho tươi ngọt", "description", "Nho giàu chất chống oxy hóa và resveratrol, tốt cho tim mạch.", "price", 60000.0, "stock", 70, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Dâu tây", "shortDescription", "Dâu tây tươi", "description", "Dâu tây giàu vitamin C và chất chống oxy hóa, có vị ngọt chua.", "price", 80000.0, "stock", 50, "weight", "0.5", "unit", "kg"));
            fruits.add(Map.of("name", "Dâu tằm", "shortDescription", "Dâu tằm tươi", "description", "Dâu tằm có vị chua ngọt, giàu vitamin C và chất chống oxy hóa.", "price", 70000.0, "stock", 55, "weight", "0.5", "unit", "kg"));
            fruits.add(Map.of("name", "Dâu da", "shortDescription", "Dâu da tươi", "description", "Dâu da có vị chua ngọt, thường dùng làm nước giải khát.", "price", 30000.0, "stock", 100, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Dâu tây Đà Lạt", "shortDescription", "Dâu tây Đà Lạt tươi", "description", "Dâu tây Đà Lạt ngọt, thơm, giàu vitamin C.", "price", 90000.0, "stock", 45, "weight", "0.5", "unit", "kg"));
            
            // Trái cây miền Nam
            fruits.add(Map.of("name", "Sapoche", "shortDescription", "Sapoche tươi", "description", "Sapoche (hồng xiêm) có vị ngọt, mềm, giàu chất xơ.", "price", 30000.0, "stock", 80, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Mãng cầu", "shortDescription", "Mãng cầu tươi", "description", "Mãng cầu có vị ngọt, thơm, giàu vitamin C.", "price", 40000.0, "stock", 70, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Mãng cầu xiêm", "shortDescription", "Mãng cầu xiêm tươi", "description", "Mãng cầu xiêm có vị chua ngọt, thường làm sinh tố.", "price", 35000.0, "stock", 75, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Trái cóc", "shortDescription", "Trái cóc tươi", "description", "Trái cóc có vị chua, thường chấm muối ớt.", "price", 18000.0, "stock", 110, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Trái thơm", "shortDescription", "Trái thơm tươi", "description", "Trái thơm (dứa) ngọt, thơm, giàu bromelain.", "price", 22000.0, "stock", 120, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Trái bơ", "shortDescription", "Bơ tươi", "description", "Bơ giàu chất béo tốt, vitamin E, tốt cho tim mạch và da.", "price", 50000.0, "stock", 65, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Trái ổi", "shortDescription", "Ổi tươi", "description", "Ổi giàu vitamin C, có vị ngọt giòn.", "price", 18000.0, "stock", 160, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Trái dừa", "shortDescription", "Dừa tươi", "description", "Dừa có nước ngọt mát, cơm dừa béo ngậy, giàu chất điện giải.", "price", 15000.0, "stock", 200, "weight", "1", "unit", "quả"));
            fruits.add(Map.of("name", "Trái dừa xiêm", "shortDescription", "Dừa xiêm tươi", "description", "Dừa xiêm nhỏ, nhiều nước, ngọt mát.", "price", 12000.0, "stock", 180, "weight", "1", "unit", "quả"));
            fruits.add(Map.of("name", "Trái dừa dứa", "shortDescription", "Dừa dứa tươi", "description", "Dừa dứa có mùi thơm đặc trưng, nước ngọt.", "price", 18000.0, "stock", 150, "weight", "1", "unit", "quả"));
            
            // Trái cây miền Bắc
            fruits.add(Map.of("name", "Hồng", "shortDescription", "Hồng tươi", "description", "Hồng có vị ngọt, mềm, giàu vitamin A và C.", "price", 40000.0, "stock", 80, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Na", "shortDescription", "Na tươi", "description", "Na (mãng cầu ta) có vị ngọt, thơm, mềm.", "price", 35000.0, "stock", 90, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Vú sữa", "shortDescription", "Vú sữa tươi", "description", "Vú sữa có vị ngọt, mọng nước, thơm.", "price", 45000.0, "stock", 70, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Mít tố nữ", "shortDescription", "Mít tố nữ tươi", "description", "Mít tố nữ nhỏ, thơm, ngọt.", "price", 50000.0, "stock", 60, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chuối sứ", "shortDescription", "Chuối sứ tươi", "description", "Chuối sứ to, ngọt, thơm.", "price", 20000.0, "stock", 170, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chuối cau", "shortDescription", "Chuối cau tươi", "description", "Chuối cau nhỏ, ngọt, thơm.", "price", 18000.0, "stock", 180, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chuối tiêu", "shortDescription", "Chuối tiêu tươi", "description", "Chuối tiêu ngọt, mềm, dễ tiêu hóa.", "price", 16000.0, "stock", 190, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chuối già", "shortDescription", "Chuối già tươi", "description", "Chuối già to, ngọt, thơm.", "price", 17000.0, "stock", 185, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Chuối laba", "shortDescription", "Chuối laba tươi", "description", "Chuối laba dài, ngọt, thơm.", "price", 19000.0, "stock", 175, "weight", "1", "unit", "kg"));
            fruits.add(Map.of("name", "Xoài cát", "shortDescription", "Xoài cát tươi", "description", "Xoài cát ngọt, thơm, ít xơ.", "price", 30000.0, "stock", 120, "weight", "1", "unit", "kg"));
            
            List<Product> createdProducts = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> fruit : fruits) {
                try {
                    String productName = (String) fruit.get("name");
                    
                    // Kiểm tra xem sản phẩm đã tồn tại chưa (theo tên và sellerId)
                    List<Product> existingProducts = productService.getBySellerId(sellerId);
                    boolean exists = false;
                    for (Product existing : existingProducts) {
                        if (existing.getName() != null && existing.getName().equals(productName)) {
                            exists = true;
                            System.out.println("⚠️ Sản phẩm '" + productName + "' đã tồn tại, bỏ qua...");
                            break;
                        }
                    }
                    
                    if (exists) {
                        failCount++;
                        continue;
                    }
                    
                    Product product = new Product();
                    product.setName(productName);
                    product.setShortDescription((String) fruit.get("shortDescription"));
                    product.setDescription((String) fruit.get("description"));
                    product.setCategory("Trái Cây");
                    product.setPrice(((Number) fruit.get("price")).doubleValue());
                    product.setStock(((Number) fruit.get("stock")).intValue());
                    product.setWeight((String) fruit.get("weight"));
                    product.setUnit((String) fruit.get("unit"));
                    product.setOrigin("VN");
                    product.setExpiryDate("3-7 ngày");
                    product.setStorageInstructions("Bảo quản nơi khô ráo và thoáng mát, tránh ánh nắng trực tiếp");
                    product.setStatus("active");
                    
                    // Tạo specifications
                    Map<String, String> specs = new java.util.HashMap<>();
                    specs.put("Xuất xứ", "Việt Nam");
                    specs.put("Loại", "Trái cây tươi");
                    product.setSpecifications(specs);
                    
                    // Set sellerId
                    product.setSellerId(sellerId);
                    
                    // Set thời gian
                    product.setCreatedAt(java.time.LocalDateTime.now());
                    product.setUpdatedAt(java.time.LocalDateTime.now());
                    
                    // Lưu sản phẩm
                    Product savedProduct = productService.create(product);
                    createdProducts.add(savedProduct);
                    successCount++;
                    System.out.println("✅ Đã tạo: " + product.getName() + " (ID: " + savedProduct.getId() + ")");
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Lỗi khi tạo " + fruit.get("name") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Đã tạo " + successCount + " sản phẩm trái cây thành công");
            response.put("total", fruits.size());
            response.put("success", successCount);
            response.put("failed", failCount);
            response.put("sellerId", sellerId);
            response.put("products", createdProducts);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            System.out.println("✅ Hoàn thành! Đã tạo " + successCount + "/" + fruits.size() + " sản phẩm trái cây");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo sản phẩm trái cây: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm trái cây");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Endpoint để thêm nhiều sản phẩm hạt giống Việt Nam
    @PostMapping("/test-add-vietnam-seeds")
    public ResponseEntity<?> addVietnamSeeds(@RequestParam(value = "username", required = false) String username) {
        try {
            System.out.println("=== TẠO NHIỀU SẢN PHẨM HẠT GIỐNG VIỆT NAM ===");
            
            // Lấy sellerId từ username
            String sellerId = null;
            if (username != null && !username.trim().isEmpty()) {
                User user = userService.getByUsername(username.trim());
                if (user != null) {
                    Seller seller = sellerService.getSellerByUserId(user.getId());
                    if (seller != null) {
                        sellerId = seller.getId();
                        System.out.println("✅ Tìm thấy sellerId: " + sellerId + " cho username: " + username);
                    } else {
                        System.out.println("⚠️ Không tìm thấy seller cho username: " + username);
                    }
                } else {
                    System.out.println("⚠️ Không tìm thấy user với username: " + username);
                }
            }
            
            if (sellerId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không tìm thấy seller",
                    "message", "Vui lòng cung cấp username hợp lệ (ví dụ: ?username=hungbanhang)"
                ));
            }
            
            // Danh sách 50 loại hạt giống phổ biến ở Việt Nam
            List<Map<String, Object>> seeds = new ArrayList<>();
            
            // Hạt giống rau củ
            seeds.add(Map.of("name", "Hạt giống cà chua", "shortDescription", "Hạt giống cà chua F1", "description", "Hạt giống cà chua F1, năng suất cao, kháng bệnh tốt. Tỷ lệ nảy mầm trên 85%.", "price", 25000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống dưa chuột", "shortDescription", "Hạt giống dưa chuột F1", "description", "Hạt giống dưa chuột F1, quả dài, giòn ngọt. Tỷ lệ nảy mầm trên 85%.", "price", 20000.0, "stock", 600, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống cà rốt", "shortDescription", "Hạt giống cà rốt", "description", "Hạt giống cà rốt, củ to, màu cam đẹp. Tỷ lệ nảy mầm trên 80%.", "price", 18000.0, "stock", 550, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống bắp cải", "shortDescription", "Hạt giống bắp cải", "description", "Hạt giống bắp cải, bắp to, chắc. Tỷ lệ nảy mầm trên 85%.", "price", 22000.0, "stock", 480, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống cải ngọt", "shortDescription", "Hạt giống cải ngọt", "description", "Hạt giống cải ngọt, lá xanh, vị ngọt. Tỷ lệ nảy mầm trên 90%.", "price", 15000.0, "stock", 700, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống rau muống", "shortDescription", "Hạt giống rau muống", "description", "Hạt giống rau muống, mọc nhanh, năng suất cao. Tỷ lệ nảy mầm trên 85%.", "price", 12000.0, "stock", 800, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống đậu cô ve", "shortDescription", "Hạt giống đậu cô ve", "description", "Hạt giống đậu cô ve, quả dài, mềm. Tỷ lệ nảy mầm trên 85%.", "price", 25000.0, "stock", 450, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống ớt", "shortDescription", "Hạt giống ớt", "description", "Hạt giống ớt, quả to, cay vừa. Tỷ lệ nảy mầm trên 80%.", "price", 20000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống hành tây", "shortDescription", "Hạt giống hành tây", "description", "Hạt giống hành tây, củ to, thơm. Tỷ lệ nảy mầm trên 75%.", "price", 18000.0, "stock", 520, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống cà tím", "shortDescription", "Hạt giống cà tím", "description", "Hạt giống cà tím, quả dài, tím đẹp. Tỷ lệ nảy mầm trên 80%.", "price", 22000.0, "stock", 480, "weight", "0.01", "unit", "gói"));
            
            // Hạt giống rau thơm
            seeds.add(Map.of("name", "Hạt giống rau mùi", "shortDescription", "Hạt giống rau mùi", "description", "Hạt giống rau mùi, thơm đặc trưng. Tỷ lệ nảy mầm trên 85%.", "price", 15000.0, "stock", 600, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống ngò rí", "shortDescription", "Hạt giống ngò rí", "description", "Hạt giống ngò rí, thơm, dễ trồng. Tỷ lệ nảy mầm trên 90%.", "price", 12000.0, "stock", 650, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống hành lá", "shortDescription", "Hạt giống hành lá", "description", "Hạt giống hành lá, mọc nhanh. Tỷ lệ nảy mầm trên 85%.", "price", 10000.0, "stock", 700, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống tía tô", "shortDescription", "Hạt giống tía tô", "description", "Hạt giống tía tô, lá tím đẹp, thơm. Tỷ lệ nảy mầm trên 80%.", "price", 18000.0, "stock", 550, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống húng quế", "shortDescription", "Hạt giống húng quế", "description", "Hạt giống húng quế, thơm, cay nhẹ. Tỷ lệ nảy mầm trên 85%.", "price", 15000.0, "stock", 580, "weight", "0.01", "unit", "gói"));
            
            // Hạt giống trái cây
            seeds.add(Map.of("name", "Hạt giống dưa hấu", "shortDescription", "Hạt giống dưa hấu F1", "description", "Hạt giống dưa hấu F1, quả to, ngọt. Tỷ lệ nảy mầm trên 85%.", "price", 30000.0, "stock", 400, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống dưa lưới", "shortDescription", "Hạt giống dưa lưới F1", "description", "Hạt giống dưa lưới F1, quả to, ngọt, thơm. Tỷ lệ nảy mầm trên 80%.", "price", 35000.0, "stock", 350, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống bí đỏ", "shortDescription", "Hạt giống bí đỏ", "description", "Hạt giống bí đỏ, quả to, ngọt. Tỷ lệ nảy mầm trên 85%.", "price", 20000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống bí xanh", "shortDescription", "Hạt giống bí xanh", "description", "Hạt giống bí xanh, quả dài, mọng nước. Tỷ lệ nảy mầm trên 85%.", "price", 18000.0, "stock", 520, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống mướp", "shortDescription", "Hạt giống mướp", "description", "Hạt giống mướp, quả dài, non ngọt. Tỷ lệ nảy mầm trên 85%.", "price", 15000.0, "stock", 600, "weight", "0.01", "unit", "gói"));
            
            // Hạt giống đậu
            seeds.add(Map.of("name", "Hạt giống đậu xanh", "shortDescription", "Hạt giống đậu xanh", "description", "Hạt giống đậu xanh, năng suất cao. Tỷ lệ nảy mầm trên 90%.", "price", 25000.0, "stock", 450, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống đậu đen", "shortDescription", "Hạt giống đậu đen", "description", "Hạt giống đậu đen, hạt to, năng suất cao. Tỷ lệ nảy mầm trên 90%.", "price", 25000.0, "stock", 420, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống đậu đỏ", "shortDescription", "Hạt giống đậu đỏ", "description", "Hạt giống đậu đỏ, hạt to, năng suất cao. Tỷ lệ nảy mầm trên 90%.", "price", 25000.0, "stock", 430, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống đậu nành", "shortDescription", "Hạt giống đậu nành", "description", "Hạt giống đậu nành, hạt to, năng suất cao. Tỷ lệ nảy mầm trên 85%.", "price", 28000.0, "stock", 400, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống đậu phộng", "shortDescription", "Hạt giống đậu phộng", "description", "Hạt giống đậu phộng, hạt to, năng suất cao. Tỷ lệ nảy mầm trên 85%.", "price", 30000.0, "stock", 380, "weight", "0.01", "unit", "gói"));
            
            // Hạt giống ngũ cốc
            seeds.add(Map.of("name", "Hạt giống ngô", "shortDescription", "Hạt giống ngô", "description", "Hạt giống ngô, bắp to, ngọt. Tỷ lệ nảy mầm trên 90%.", "price", 25000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống lúa", "shortDescription", "Hạt giống lúa", "description", "Hạt giống lúa, năng suất cao, chất lượng tốt. Tỷ lệ nảy mầm trên 95%.", "price", 20000.0, "stock", 600, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống mè", "shortDescription", "Hạt giống mè", "description", "Hạt giống mè, hạt to, năng suất cao. Tỷ lệ nảy mầm trên 85%.", "price", 35000.0, "stock", 350, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống đậu tương", "shortDescription", "Hạt giống đậu tương", "description", "Hạt giống đậu tương, năng suất cao. Tỷ lệ nảy mầm trên 85%.", "price", 28000.0, "stock", 400, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống kê", "shortDescription", "Hạt giống kê", "description", "Hạt giống kê, năng suất cao. Tỷ lệ nảy mầm trên 90%.", "price", 30000.0, "stock", 380, "weight", "0.01", "unit", "gói"));
            
            // Hạt giống hoa
            seeds.add(Map.of("name", "Hạt giống hoa hướng dương", "shortDescription", "Hạt giống hoa hướng dương", "description", "Hạt giống hoa hướng dương, hoa to, đẹp. Tỷ lệ nảy mầm trên 85%.", "price", 25000.0, "stock", 400, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống hoa cúc", "shortDescription", "Hạt giống hoa cúc", "description", "Hạt giống hoa cúc, nhiều màu sắc. Tỷ lệ nảy mầm trên 85%.", "price", 20000.0, "stock", 450, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống hoa mười giờ", "shortDescription", "Hạt giống hoa mười giờ", "description", "Hạt giống hoa mười giờ, dễ trồng, nhiều màu. Tỷ lệ nảy mầm trên 90%.", "price", 15000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống hoa dừa cạn", "shortDescription", "Hạt giống hoa dừa cạn", "description", "Hạt giống hoa dừa cạn, nhiều màu sắc. Tỷ lệ nảy mầm trên 85%.", "price", 18000.0, "stock", 480, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống hoa cẩm chướng", "shortDescription", "Hạt giống hoa cẩm chướng", "description", "Hạt giống hoa cẩm chướng, hoa đẹp, thơm. Tỷ lệ nảy mầm trên 80%.", "price", 22000.0, "stock", 420, "weight", "0.01", "unit", "gói"));
            
            // Hạt giống cây ăn quả
            seeds.add(Map.of("name", "Hạt giống chanh", "shortDescription", "Hạt giống chanh", "description", "Hạt giống chanh, cây khỏe, nhiều quả. Tỷ lệ nảy mầm trên 75%.", "price", 20000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống ổi", "shortDescription", "Hạt giống ổi", "description", "Hạt giống ổi, cây nhanh lớn, nhiều quả. Tỷ lệ nảy mầm trên 80%.", "price", 25000.0, "stock", 450, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống xoài", "shortDescription", "Hạt giống xoài", "description", "Hạt giống xoài, cây khỏe, quả ngọt. Tỷ lệ nảy mầm trên 75%.", "price", 30000.0, "stock", 400, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống mít", "shortDescription", "Hạt giống mít", "description", "Hạt giống mít, cây khỏe, quả to. Tỷ lệ nảy mầm trên 70%.", "price", 35000.0, "stock", 350, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống đu đủ", "shortDescription", "Hạt giống đu đủ", "description", "Hạt giống đu đủ, cây nhanh lớn, nhiều quả. Tỷ lệ nảy mầm trên 80%.", "price", 20000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            
            // Hạt giống khác
            seeds.add(Map.of("name", "Hạt giống cải bẹ xanh", "shortDescription", "Hạt giống cải bẹ xanh", "description", "Hạt giống cải bẹ xanh, lá xanh, vị ngọt. Tỷ lệ nảy mầm trên 90%.", "price", 15000.0, "stock", 650, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống cải thìa", "shortDescription", "Hạt giống cải thìa", "description", "Hạt giống cải thìa, giòn ngọt. Tỷ lệ nảy mầm trên 90%.", "price", 16000.0, "stock", 620, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống cải xoăn", "shortDescription", "Hạt giống cải xoăn", "description", "Hạt giống cải xoăn, lá xoăn đẹp. Tỷ lệ nảy mầm trên 85%.", "price", 20000.0, "stock", 480, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống cải chip", "shortDescription", "Hạt giống cải chip", "description", "Hạt giống cải chip, giòn ngọt. Tỷ lệ nảy mầm trên 90%.", "price", 18000.0, "stock", 550, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống cải thảo", "shortDescription", "Hạt giống cải thảo", "description", "Hạt giống cải thảo, bắp to, giòn. Tỷ lệ nảy mầm trên 85%.", "price", 22000.0, "stock", 500, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống khổ qua", "shortDescription", "Hạt giống khổ qua", "description", "Hạt giống khổ qua, quả dài, đắng. Tỷ lệ nảy mầm trên 80%.", "price", 20000.0, "stock", 520, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống su su", "shortDescription", "Hạt giống su su", "description", "Hạt giống su su, quả giòn ngọt. Tỷ lệ nảy mầm trên 85%.", "price", 18000.0, "stock", 580, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống bầu", "shortDescription", "Hạt giống bầu", "description", "Hạt giống bầu, quả to, mọng nước. Tỷ lệ nảy mầm trên 85%.", "price", 15000.0, "stock", 600, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống khoai lang", "shortDescription", "Hạt giống khoai lang", "description", "Hạt giống khoai lang, củ to, ngọt. Tỷ lệ nảy mầm trên 90%.", "price", 25000.0, "stock", 450, "weight", "0.01", "unit", "gói"));
            seeds.add(Map.of("name", "Hạt giống khoai tây", "shortDescription", "Hạt giống khoai tây", "description", "Hạt giống khoai tây, củ to, năng suất cao. Tỷ lệ nảy mầm trên 85%.", "price", 28000.0, "stock", 420, "weight", "0.01", "unit", "gói"));
            
            List<Product> createdProducts = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> seed : seeds) {
                try {
                    String productName = (String) seed.get("name");
                    
                    // Kiểm tra xem sản phẩm đã tồn tại chưa
                    List<Product> existingProducts = productService.getBySellerId(sellerId);
                    boolean exists = false;
                    for (Product existing : existingProducts) {
                        if (existing.getName() != null && existing.getName().equals(productName)) {
                            exists = true;
                            System.out.println("⚠️ Sản phẩm '" + productName + "' đã tồn tại, bỏ qua...");
                            break;
                        }
                    }
                    
                    if (exists) {
                        failCount++;
                        continue;
                    }
                    
                    Product product = new Product();
                    product.setName(productName);
                    product.setShortDescription((String) seed.get("shortDescription"));
                    product.setDescription((String) seed.get("description"));
                    product.setCategory("Hạt Giống");
                    product.setPrice(((Number) seed.get("price")).doubleValue());
                    product.setStock(((Number) seed.get("stock")).intValue());
                    product.setWeight((String) seed.get("weight"));
                    product.setUnit((String) seed.get("unit"));
                    product.setOrigin("VN");
                    product.setExpiryDate("12-24 tháng");
                    product.setStorageInstructions("Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp và độ ẩm cao");
                    product.setStatus("active");
                    
                    // Tạo specifications
                    Map<String, String> specs = new java.util.HashMap<>();
                    specs.put("Xuất xứ", "Việt Nam");
                    specs.put("Loại", "Hạt giống");
                    product.setSpecifications(specs);
                    
                    // Set sellerId
                    product.setSellerId(sellerId);
                    
                    // Set thời gian
                    product.setCreatedAt(java.time.LocalDateTime.now());
                    product.setUpdatedAt(java.time.LocalDateTime.now());
                    
                    // Lưu sản phẩm
                    Product savedProduct = productService.create(product);
                    createdProducts.add(savedProduct);
                    successCount++;
                    System.out.println("✅ Đã tạo: " + product.getName() + " (ID: " + savedProduct.getId() + ")");
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Lỗi khi tạo " + seed.get("name") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Đã tạo " + successCount + " sản phẩm hạt giống thành công");
            response.put("total", seeds.size());
            response.put("success", successCount);
            response.put("failed", failCount);
            response.put("sellerId", sellerId);
            response.put("products", createdProducts);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            System.out.println("✅ Hoàn thành! Đã tạo " + successCount + "/" + seeds.size() + " sản phẩm hạt giống");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo sản phẩm hạt giống: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm hạt giống");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Endpoint để thêm nhiều sản phẩm dụng cụ nông nghiệp Việt Nam
    @PostMapping("/test-add-vietnam-tools")
    public ResponseEntity<?> addVietnamTools(@RequestParam(value = "username", required = false) String username) {
        try {
            System.out.println("=== TẠO NHIỀU SẢN PHẨM DỤNG CỤ NÔNG NGHIỆP VIỆT NAM ===");
            
            // Lấy sellerId từ username
            String sellerId = null;
            if (username != null && !username.trim().isEmpty()) {
                User user = userService.getByUsername(username.trim());
                if (user != null) {
                    Seller seller = sellerService.getSellerByUserId(user.getId());
                    if (seller != null) {
                        sellerId = seller.getId();
                        System.out.println("✅ Tìm thấy sellerId: " + sellerId + " cho username: " + username);
                    } else {
                        System.out.println("⚠️ Không tìm thấy seller cho username: " + username);
                    }
                } else {
                    System.out.println("⚠️ Không tìm thấy user với username: " + username);
                }
            }
            
            if (sellerId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Không tìm thấy seller",
                    "message", "Vui lòng cung cấp username hợp lệ (ví dụ: ?username=hungbanhang)"
                ));
            }
            
            // Danh sách 50 loại dụng cụ nông nghiệp phổ biến ở Việt Nam
            List<Map<String, Object>> tools = new ArrayList<>();
            
            // Dụng cụ làm đất
            tools.add(Map.of("name", "Cuốc", "shortDescription", "Cuốc làm đất", "description", "Cuốc làm đất bằng thép, cán gỗ chắc chắn. Dùng để cuốc đất, đào hố trồng cây.", "price", 80000.0, "stock", 100, "weight", "1.5", "unit", "cái"));
            tools.add(Map.of("name", "Xẻng", "shortDescription", "Xẻng đào đất", "description", "Xẻng đào đất bằng thép, cán dài. Dùng để đào đất, xúc đất.", "price", 70000.0, "stock", 120, "weight", "1.2", "unit", "cái"));
            tools.add(Map.of("name", "Cào", "shortDescription", "Cào làm đất", "description", "Cào làm đất bằng thép, răng nhọn. Dùng để cào đất, làm phẳng mặt đất.", "price", 60000.0, "stock", 130, "weight", "1.0", "unit", "cái"));
            tools.add(Map.of("name", "Chĩa ba", "shortDescription", "Chĩa ba làm đất", "description", "Chĩa ba làm đất, 3 răng nhọn. Dùng để xới đất, làm tơi đất.", "price", 65000.0, "stock", 125, "weight", "1.1", "unit", "cái"));
            tools.add(Map.of("name", "Bừa", "shortDescription", "Bừa làm đất", "description", "Bừa làm đất, răng nhọn. Dùng để bừa đất, làm nhỏ đất.", "price", 75000.0, "stock", 110, "weight", "1.3", "unit", "cái"));
            
            // Dụng cụ trồng trọt
            tools.add(Map.of("name", "Bay làm vườn", "shortDescription", "Bay làm vườn", "description", "Bay làm vườn nhỏ gọn, dùng để trồng cây, xới đất trong chậu.", "price", 35000.0, "stock", 200, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Kéo cắt cành", "shortDescription", "Kéo cắt cành", "description", "Kéo cắt cành sắc bén, dùng để cắt tỉa cành cây, lá cây.", "price", 120000.0, "stock", 80, "weight", "0.4", "unit", "cái"));
            tools.add(Map.of("name", "Kéo cắt cỏ", "shortDescription", "Kéo cắt cỏ", "description", "Kéo cắt cỏ, lưỡi dài, sắc. Dùng để cắt cỏ, tỉa hàng rào.", "price", 100000.0, "stock", 90, "weight", "0.5", "unit", "cái"));
            tools.add(Map.of("name", "Cưa cắt cành", "shortDescription", "Cưa cắt cành", "description", "Cưa cắt cành, lưỡi sắc. Dùng để cắt cành cây lớn.", "price", 150000.0, "stock", 70, "weight", "0.6", "unit", "cái"));
            tools.add(Map.of("name", "Tỉa cây", "shortDescription", "Tỉa cây", "description", "Dụng cụ tỉa cây nhỏ gọn, dùng để tỉa lá, cành nhỏ.", "price", 40000.0, "stock", 180, "weight", "0.2", "unit", "cái"));
            
            // Dụng cụ tưới nước
            tools.add(Map.of("name", "Vòi tưới nước", "shortDescription", "Vòi tưới nước", "description", "Vòi tưới nước dài 20m, có đầu phun. Dùng để tưới cây, vườn.", "price", 80000.0, "stock", 150, "weight", "1.0", "unit", "cuộn"));
            tools.add(Map.of("name", "Bình tưới nước", "shortDescription", "Bình tưới nước", "description", "Bình tưới nước 5 lít, có vòi phun. Dùng để tưới cây nhỏ, chậu cảnh.", "price", 60000.0, "stock", 200, "weight", "0.5", "unit", "cái"));
            tools.add(Map.of("name", "Béc phun nước", "shortDescription", "Béc phun nước", "description", "Béc phun nước tự động, phun sương. Dùng cho hệ thống tưới tự động.", "price", 25000.0, "stock", 300, "weight", "0.1", "unit", "cái"));
            tools.add(Map.of("name", "Ống tưới nhỏ giọt", "shortDescription", "Ống tưới nhỏ giọt", "description", "Ống tưới nhỏ giọt, tiết kiệm nước. Dùng cho hệ thống tưới tự động.", "price", 50000.0, "stock", 250, "weight", "0.8", "unit", "cuộn"));
            tools.add(Map.of("name", "Bơm nước mini", "shortDescription", "Bơm nước mini", "description", "Bơm nước mini, công suất nhỏ. Dùng để bơm nước từ giếng, ao.", "price", 200000.0, "stock", 50, "weight", "2.0", "unit", "cái"));
            
            // Dụng cụ bón phân
            tools.add(Map.of("name", "Xẻng bón phân", "shortDescription", "Xẻng bón phân", "description", "Xẻng bón phân nhỏ, dùng để bón phân cho cây.", "price", 45000.0, "stock", 180, "weight", "0.4", "unit", "cái"));
            tools.add(Map.of("name", "Rổ đựng phân", "shortDescription", "Rổ đựng phân", "description", "Rổ đựng phân bằng nhựa, có quai xách. Dùng để đựng phân bón.", "price", 30000.0, "stock", 200, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Cân phân", "shortDescription", "Cân phân", "description", "Cân phân điện tử, độ chính xác cao. Dùng để cân phân bón.", "price", 150000.0, "stock", 60, "weight", "0.5", "unit", "cái"));
            tools.add(Map.of("name", "Máy trộn phân", "shortDescription", "Máy trộn phân", "description", "Máy trộn phân nhỏ, dùng để trộn phân bón.", "price", 300000.0, "stock", 40, "weight", "3.0", "unit", "cái"));
            tools.add(Map.of("name", "Bình phun phân", "shortDescription", "Bình phun phân", "description", "Bình phun phân, có bơm tay. Dùng để phun phân bón lá.", "price", 120000.0, "stock", 80, "weight", "1.5", "unit", "cái"));
            
            // Dụng cụ thu hoạch
            tools.add(Map.of("name", "Rổ thu hoạch", "shortDescription", "Rổ thu hoạch", "description", "Rổ thu hoạch bằng nhựa, có quai. Dùng để đựng rau củ khi thu hoạch.", "price", 40000.0, "stock", 150, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Giỏ thu hoạch", "shortDescription", "Giỏ thu hoạch", "description", "Giỏ thu hoạch bằng mây, có quai. Dùng để đựng trái cây khi thu hoạch.", "price", 50000.0, "stock", 120, "weight", "0.5", "unit", "cái"));
            tools.add(Map.of("name", "Kéo hái quả", "shortDescription", "Kéo hái quả", "description", "Kéo hái quả có lưỡi cong, dùng để hái quả trên cao.", "price", 80000.0, "stock", 100, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Sào hái quả", "shortDescription", "Sào hái quả", "description", "Sào hái quả dài 3m, có túi lưới. Dùng để hái quả trên cao.", "price", 150000.0, "stock", 70, "weight", "1.0", "unit", "cái"));
            tools.add(Map.of("name", "Thùng đựng rau", "shortDescription", "Thùng đựng rau", "description", "Thùng đựng rau bằng nhựa, có nắp. Dùng để đựng rau củ sau thu hoạch.", "price", 60000.0, "stock", 130, "weight", "0.8", "unit", "cái"));
            
            // Dụng cụ bảo vệ
            tools.add(Map.of("name", "Lưới che nắng", "shortDescription", "Lưới che nắng", "description", "Lưới che nắng 70%, dài 50m, rộng 2m. Dùng để che nắng cho cây trồng.", "price", 200000.0, "stock", 60, "weight", "2.0", "unit", "cuộn"));
            tools.add(Map.of("name", "Lưới chắn côn trùng", "shortDescription", "Lưới chắn côn trùng", "description", "Lưới chắn côn trùng, mắt lưới nhỏ. Dùng để bảo vệ cây khỏi côn trùng.", "price", 150000.0, "stock", 80, "weight", "1.5", "unit", "cuộn"));
            tools.add(Map.of("name", "Bẫy côn trùng", "shortDescription", "Bẫy côn trùng", "description", "Bẫy côn trùng bằng keo, màu vàng. Dùng để bẫy ruồi, bọ phấn.", "price", 30000.0, "stock", 200, "weight", "0.2", "unit", "cái"));
            tools.add(Map.of("name", "Bình xịt thuốc", "shortDescription", "Bình xịt thuốc", "description", "Bình xịt thuốc trừ sâu, có bơm tay. Dùng để phun thuốc bảo vệ thực vật.", "price", 180000.0, "stock", 70, "weight", "2.5", "unit", "cái"));
            tools.add(Map.of("name", "Máy phun thuốc", "shortDescription", "Máy phun thuốc", "description", "Máy phun thuốc điện, công suất lớn. Dùng để phun thuốc cho cây trồng.", "price", 800000.0, "stock", 30, "weight", "5.0", "unit", "cái"));
            
            // Dụng cụ chăm sóc
            tools.add(Map.of("name", "Găng tay làm vườn", "shortDescription", "Găng tay làm vườn", "description", "Găng tay làm vườn bằng cao su, chống nước. Bảo vệ tay khi làm vườn.", "price", 50000.0, "stock", 250, "weight", "0.2", "unit", "đôi"));
            tools.add(Map.of("name", "Ủng đi đồng", "shortDescription", "Ủng đi đồng", "description", "Ủng đi đồng bằng cao su, chống nước. Dùng để đi trong ruộng, vườn.", "price", 120000.0, "stock", 100, "weight", "1.0", "unit", "đôi"));
            tools.add(Map.of("name", "Nón lá", "shortDescription", "Nón lá", "description", "Nón lá che nắng, thông thoáng. Dùng để che nắng khi làm vườn.", "price", 40000.0, "stock", 200, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Áo mưa", "shortDescription", "Áo mưa", "description", "Áo mưa bằng nhựa, chống nước. Dùng để mặc khi làm vườn trời mưa.", "price", 80000.0, "stock", 150, "weight", "0.5", "unit", "cái"));
            tools.add(Map.of("name", "Băng keo dán cây", "shortDescription", "Băng keo dán cây", "description", "Băng keo dán cây, chống sâu bệnh. Dùng để quấn quanh thân cây.", "price", 25000.0, "stock", 300, "weight", "0.1", "unit", "cuộn"));
            
            // Dụng cụ đo lường
            tools.add(Map.of("name", "Máy đo độ ẩm đất", "shortDescription", "Máy đo độ ẩm đất", "description", "Máy đo độ ẩm đất điện tử, độ chính xác cao. Dùng để đo độ ẩm đất.", "price", 200000.0, "stock", 50, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Máy đo pH đất", "shortDescription", "Máy đo pH đất", "description", "Máy đo pH đất điện tử. Dùng để đo độ pH của đất.", "price", 250000.0, "stock", 40, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Nhiệt kế đất", "shortDescription", "Nhiệt kế đất", "description", "Nhiệt kế đất, đo nhiệt độ đất. Dùng để theo dõi nhiệt độ đất.", "price", 80000.0, "stock", 100, "weight", "0.2", "unit", "cái"));
            tools.add(Map.of("name", "Thước đo cây", "shortDescription", "Thước đo cây", "description", "Thước đo cây, dài 2m. Dùng để đo chiều cao cây.", "price", 30000.0, "stock", 180, "weight", "0.3", "unit", "cái"));
            tools.add(Map.of("name", "Cân điện tử", "shortDescription", "Cân điện tử", "description", "Cân điện tử, tải trọng 50kg. Dùng để cân sản phẩm nông nghiệp.", "price", 300000.0, "stock", 35, "weight", "2.0", "unit", "cái"));
            
            // Dụng cụ đóng gói
            tools.add(Map.of("name", "Túi đóng gói rau", "shortDescription", "Túi đóng gói rau", "description", "Túi đóng gói rau bằng nhựa, kích thước 30x40cm. Dùng để đóng gói rau củ.", "price", 20000.0, "stock", 500, "weight", "0.1", "unit", "túi"));
            tools.add(Map.of("name", "Hộp đựng trái cây", "shortDescription", "Hộp đựng trái cây", "description", "Hộp đựng trái cây bằng nhựa, có nắp. Dùng để đựng trái cây.", "price", 15000.0, "stock", 400, "weight", "0.2", "unit", "hộp"));
            tools.add(Map.of("name", "Máy đóng gói", "shortDescription", "Máy đóng gói", "description", "Máy đóng gói tự động, dùng để đóng gói sản phẩm nông nghiệp.", "price", 2000000.0, "stock", 10, "weight", "50.0", "unit", "cái"));
            tools.add(Map.of("name", "Máy dán nhãn", "shortDescription", "Máy dán nhãn", "description", "Máy dán nhãn tự động, dùng để dán nhãn lên sản phẩm.", "price", 1500000.0, "stock", 15, "weight", "30.0", "unit", "cái"));
            tools.add(Map.of("name", "Băng keo đóng gói", "shortDescription", "Băng keo đóng gói", "description", "Băng keo đóng gói, dài 50m. Dùng để dán thùng, hộp.", "price", 25000.0, "stock", 300, "weight", "0.2", "unit", "cuộn"));
            
            // Dụng cụ khác
            tools.add(Map.of("name", "Xe đẩy nông nghiệp", "shortDescription", "Xe đẩy nông nghiệp", "description", "Xe đẩy nông nghiệp, tải trọng 100kg. Dùng để vận chuyển sản phẩm.", "price", 500000.0, "stock", 25, "weight", "15.0", "unit", "cái"));
            tools.add(Map.of("name", "Giàn leo", "shortDescription", "Giàn leo", "description", "Giàn leo bằng tre, cao 2m. Dùng để làm giàn cho cây leo.", "price", 100000.0, "stock", 80, "weight", "3.0", "unit", "bộ"));
            tools.add(Map.of("name", "Cọc tre", "shortDescription", "Cọc tre", "description", "Cọc tre, dài 1.5m. Dùng để làm giàn, cắm cọc cho cây.", "price", 15000.0, "stock", 500, "weight", "0.5", "unit", "cọc"));
            tools.add(Map.of("name", "Dây buộc cây", "shortDescription", "Dây buộc cây", "description", "Dây buộc cây bằng nhựa, dài 50m. Dùng để buộc cây vào cọc.", "price", 30000.0, "stock", 200, "weight", "0.3", "unit", "cuộn"));
            tools.add(Map.of("name", "Chậu trồng cây", "shortDescription", "Chậu trồng cây", "description", "Chậu trồng cây bằng nhựa, kích thước 30cm. Dùng để trồng cây trong chậu.", "price", 50000.0, "stock", 150, "weight", "0.5", "unit", "cái"));
            
            List<Product> createdProducts = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (Map<String, Object> tool : tools) {
                try {
                    String productName = (String) tool.get("name");
                    
                    // Kiểm tra xem sản phẩm đã tồn tại chưa
                    List<Product> existingProducts = productService.getBySellerId(sellerId);
                    boolean exists = false;
                    for (Product existing : existingProducts) {
                        if (existing.getName() != null && existing.getName().equals(productName)) {
                            exists = true;
                            System.out.println("⚠️ Sản phẩm '" + productName + "' đã tồn tại, bỏ qua...");
                            break;
                        }
                    }
                    
                    if (exists) {
                        failCount++;
                        continue;
                    }
                    
                    Product product = new Product();
                    product.setName(productName);
                    product.setShortDescription((String) tool.get("shortDescription"));
                    product.setDescription((String) tool.get("description"));
                    product.setCategory("Dụng Cụ Nông Nghiệp");
                    product.setPrice(((Number) tool.get("price")).doubleValue());
                    product.setStock(((Number) tool.get("stock")).intValue());
                    product.setWeight((String) tool.get("weight"));
                    product.setUnit((String) tool.get("unit"));
                    product.setOrigin("VN");
                    product.setExpiryDate("Không có hạn sử dụng");
                    product.setStorageInstructions("Bảo quản nơi khô ráo, thoáng mát, tránh ẩm ướt và gỉ sét");
                    product.setStatus("active");
                    
                    // Tạo specifications
                    Map<String, String> specs = new java.util.HashMap<>();
                    specs.put("Xuất xứ", "Việt Nam");
                    specs.put("Loại", "Dụng cụ nông nghiệp");
                    product.setSpecifications(specs);
                    
                    // Set sellerId
                    product.setSellerId(sellerId);
                    
                    // Set thời gian
                    product.setCreatedAt(java.time.LocalDateTime.now());
                    product.setUpdatedAt(java.time.LocalDateTime.now());
                    
                    // Lưu sản phẩm
                    Product savedProduct = productService.create(product);
                    createdProducts.add(savedProduct);
                    successCount++;
                    System.out.println("✅ Đã tạo: " + product.getName() + " (ID: " + savedProduct.getId() + ")");
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Lỗi khi tạo " + tool.get("name") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "Đã tạo " + successCount + " sản phẩm dụng cụ thành công");
            response.put("total", tools.size());
            response.put("success", successCount);
            response.put("failed", failCount);
            response.put("sellerId", sellerId);
            response.put("products", createdProducts);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            System.out.println("✅ Hoàn thành! Đã tạo " + successCount + "/" + tools.size() + " sản phẩm dụng cụ");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tạo sản phẩm dụng cụ: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "Lỗi khi tạo sản phẩm dụng cụ");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
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
