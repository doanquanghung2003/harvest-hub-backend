package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Voucher;
import com.example.harvesthubbackend.Models.UserVoucher;
import com.example.harvesthubbackend.Service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*")
public class VoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    // Create a new voucher
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        Voucher createdVoucher = voucherService.createVoucher(voucher);
        return ResponseEntity.ok(createdVoucher);
    }
    
    // Get voucher by ID
    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getVoucherById(@PathVariable String id) {
        Optional<Voucher> voucher = voucherService.getVoucherById(id);
        return voucher.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // Get voucher by code
    @GetMapping("/code/{code}")
    public ResponseEntity<Voucher> getVoucherByCode(@PathVariable String code) {
        Optional<Voucher> voucher = voucherService.getVoucherByCode(code);
        return voucher.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // Get all vouchers
    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    // Get active vouchers
    @GetMapping("/active")
    public ResponseEntity<List<Voucher>> getActiveVouchers() {
        List<Voucher> vouchers = voucherService.getActiveVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers by shop
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Voucher>> getVouchersByShop(@PathVariable String shopId) {
        List<Voucher> vouchers = voucherService.getVouchersByShop(shopId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get platform-wide vouchers
    @GetMapping("/platform")
    public ResponseEntity<List<Voucher>> getPlatformVouchers() {
        List<Voucher> vouchers = voucherService.getPlatformVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Voucher>> getVouchersByType(@PathVariable String type) {
        List<Voucher> vouchers = voucherService.getVouchersByType(type);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Voucher>> getVouchersByStatus(@PathVariable String status) {
        List<Voucher> vouchers = voucherService.getVouchersByStatus(status);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers for specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Voucher>> getVouchersForUser(@PathVariable String userId) {
        List<Voucher> vouchers = voucherService.getVouchersForUser(userId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers for specific category
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Voucher>> getVouchersForCategory(@PathVariable String categoryId) {
        List<Voucher> vouchers = voucherService.getVouchersForCategory(categoryId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers for specific product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Voucher>> getVouchersForProduct(@PathVariable String productId) {
        List<Voucher> vouchers = voucherService.getVouchersForProduct(productId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get valid vouchers for order
    @GetMapping("/valid")
    public ResponseEntity<List<Voucher>> getValidVouchersForOrder(
            @RequestParam double orderAmount,
            @RequestParam String shopId) {
        List<Voucher> vouchers = voucherService.getValidVouchersForOrder(orderAmount, shopId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers expiring soon
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<Voucher>> getVouchersExpiringSoon(@RequestParam int days) {
        List<Voucher> vouchers = voucherService.getVouchersExpiringSoon(days);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers near usage limit
    @GetMapping("/near-limit")
    public ResponseEntity<List<Voucher>> getVouchersNearUsageLimit(@RequestParam int threshold) {
        List<Voucher> vouchers = voucherService.getVouchersNearUsageLimit(threshold);
        return ResponseEntity.ok(vouchers);
    }
    
    // Update voucher
    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable String id, @RequestBody Voucher voucherDetails) {
        Voucher updatedVoucher = voucherService.updateVoucher(id, voucherDetails);
        if (updatedVoucher != null) {
            return ResponseEntity.ok(updatedVoucher);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update voucher status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Voucher> updateVoucherStatus(@PathVariable String id, @RequestParam String status) {
        Voucher updatedVoucher = voucherService.updateVoucherStatus(id, status);
        if (updatedVoucher != null) {
            return ResponseEntity.ok(updatedVoucher);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Use voucher
    @PostMapping("/{id}/use")
    public ResponseEntity<Voucher> useVoucher(@PathVariable String id) {
        Voucher updatedVoucher = voucherService.useVoucher(id);
        if (updatedVoucher != null) {
            return ResponseEntity.ok(updatedVoucher);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Validate voucher
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateVoucher(@RequestBody VoucherValidationRequest request) {
        boolean isValid = voucherService.validateVoucher(
            request.getCode(),
            request.getOrderAmount(),
            request.getUserId(),
            request.getShopId(),
            request.getCategoryId(),
            request.getProductId()
        );
        return ResponseEntity.ok(isValid);
    }
    
    // Calculate discount
    @PostMapping("/calculate-discount")
    public ResponseEntity<Double> calculateDiscount(@RequestBody DiscountCalculationRequest request) {
        double discount = voucherService.calculateDiscount(request.getCode(), request.getOrderAmount());
        return ResponseEntity.ok(discount);
    }
    
    // Delete voucher
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable String id) {
        boolean deleted = voucherService.deleteVoucher(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if voucher exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> voucherExists(@PathVariable String id) {
        boolean exists = voucherService.voucherExists(id);
        return ResponseEntity.ok(exists);
    }
    
    // Check if voucher code exists
    @GetMapping("/code/{code}/exists")
    public ResponseEntity<Boolean> voucherCodeExists(@PathVariable String code) {
        boolean exists = voucherService.voucherCodeExists(code);
        return ResponseEntity.ok(exists);
    }
    
    // Get voucher count by status
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getVoucherCountByStatus(@PathVariable String status) {
        long count = voucherService.getVoucherCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    // Get user's vouchers
    @GetMapping("/my-vouchers/{userId}")
    public ResponseEntity<List<UserVoucher>> getMyVouchers(@PathVariable String userId) {
        List<UserVoucher> vouchers = voucherService.getUserVouchers(userId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get user's active vouchers
    @GetMapping("/my-vouchers/{userId}/active")
    public ResponseEntity<List<UserVoucher>> getMyActiveVouchers(@PathVariable String userId) {
        List<UserVoucher> vouchers = voucherService.getUserActiveVouchers(userId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get user's vouchers with eligibility info for cart
    @GetMapping("/my-vouchers/{userId}/for-cart")
    public ResponseEntity<List<VoucherEligibilityResponse>> getMyVouchersForCart(
            @PathVariable String userId,
            @RequestParam(required = false, defaultValue = "0") double subtotal,
            @RequestParam(required = false) String shopId,
            @RequestParam(required = false) List<String> productIds,
            @RequestParam(required = false) List<String> categoryIds) {
        List<UserVoucher> userVouchers = voucherService.getUserActiveVouchers(userId);
        List<VoucherEligibilityResponse> responses = new ArrayList<>();
        
        for (UserVoucher userVoucher : userVouchers) {
            VoucherEligibilityResponse response = new VoucherEligibilityResponse();
            response.setUserVoucher(userVoucher);
            
            // Get voucher details
            Optional<Voucher> voucherOpt = voucherService.getVoucherByCode(userVoucher.getVoucherCode());
            if (voucherOpt.isPresent()) {
                Voucher voucher = voucherOpt.get();
                response.setVoucher(voucher);
                
                // Check eligibility
                List<String> productIdList = productIds != null ? productIds : new ArrayList<>();
                List<String> categoryIdList = categoryIds != null ? categoryIds : new ArrayList<>();
                
                boolean isEligible = voucherService.validateVoucherForOrder(
                    userVoucher.getVoucherCode(), userId, subtotal, shopId, productIdList, categoryIdList);
                response.setEligible(isEligible);
                
                // Calculate potential discount
                if (isEligible) {
                    double discount = voucherService.calculateDiscount(userVoucher.getVoucherCode(), subtotal);
                    response.setDiscountAmount(discount);
                } else {
                    response.setDiscountAmount(0);
                    // Set reason if not eligible
                    if (subtotal < voucher.getMinOrderAmount()) {
                        response.setReason("Đơn hàng tối thiểu: " + String.format("%.0f", voucher.getMinOrderAmount()) + "đ");
                    } else {
                        response.setReason("Voucher không thể áp dụng cho đơn hàng này");
                    }
                }
            } else {
                response.setEligible(false);
                response.setReason("Voucher không tồn tại");
            }
            
            responses.add(response);
        }
        
        return ResponseEntity.ok(responses);
    }
    
    // Get eligible vouchers for order
    @GetMapping("/eligible")
    public ResponseEntity<List<Voucher>> getEligibleVouchers(
            @RequestParam String userId,
            @RequestParam double orderAmount,
            @RequestParam(required = false) String shopId,
            @RequestParam(required = false) List<String> productIds,
            @RequestParam(required = false) List<String> categoryIds) {
        List<Voucher> vouchers = voucherService.getEligibleVouchers(
            userId, orderAmount, shopId, 
            productIds != null ? productIds : new ArrayList<>(), 
            categoryIds != null ? categoryIds : new ArrayList<>());
        return ResponseEntity.ok(vouchers);
    }
    
    // Apply voucher to cart
    @PostMapping("/apply-to-cart")
    public ResponseEntity<Double> applyVoucherToCart(@RequestBody ApplyVoucherRequest request) {
        try {
            double discount = voucherService.applyVoucherToCart(
                request.getUserId(), request.getVoucherCode(), request.getCartSubtotal());
            return ResponseEntity.ok(discount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Grant voucher to user
    @PostMapping("/grant")
    public ResponseEntity<UserVoucher> grantVoucherToUser(@RequestBody GrantVoucherRequest request) {
        try {
            UserVoucher userVoucher = voucherService.grantVoucherToUser(
                request.getUserId(), request.getVoucherId());
            return ResponseEntity.ok(userVoucher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Validate voucher for order (comprehensive)
    @PostMapping("/validate-for-order")
    public ResponseEntity<Boolean> validateVoucherForOrder(@RequestBody VoucherValidationForOrderRequest request) {
        boolean isValid = voucherService.validateVoucherForOrder(
            request.getCode(),
            request.getUserId(),
            request.getOrderAmount(),
            request.getShopId(),
            request.getProductIds() != null ? request.getProductIds() : new ArrayList<>(),
            request.getCategoryIds() != null ? request.getCategoryIds() : new ArrayList<>()
        );
        return ResponseEntity.ok(isValid);
    }
    
    // Request classes for validation and discount calculation
    public static class VoucherValidationRequest {
        private String code;
        private double orderAmount;
        private String userId;
        private String shopId;
        private String categoryId;
        private String productId;
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public double getOrderAmount() { return orderAmount; }
        public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getShopId() { return shopId; }
        public void setShopId(String shopId) { this.shopId = shopId; }
        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
    }
    
    public static class DiscountCalculationRequest {
        private String code;
        private double orderAmount;
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public double getOrderAmount() { return orderAmount; }
        public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
    }
    
    public static class ApplyVoucherRequest {
        private String userId;
        private String voucherCode;
        private double cartSubtotal;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getVoucherCode() { return voucherCode; }
        public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
        public double getCartSubtotal() { return cartSubtotal; }
        public void setCartSubtotal(double cartSubtotal) { this.cartSubtotal = cartSubtotal; }
    }
    
    public static class GrantVoucherRequest {
        private String userId;
        private String voucherId;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getVoucherId() { return voucherId; }
        public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    }
    
    public static class VoucherValidationForOrderRequest {
        private String code;
        private String userId;
        private double orderAmount;
        private String shopId;
        private List<String> productIds;
        private List<String> categoryIds;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public double getOrderAmount() { return orderAmount; }
        public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
        public String getShopId() { return shopId; }
        public void setShopId(String shopId) { this.shopId = shopId; }
        public List<String> getProductIds() { return productIds; }
        public void setProductIds(List<String> productIds) { this.productIds = productIds; }
        public List<String> getCategoryIds() { return categoryIds; }
        public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }
    }
    
    // Response class for voucher eligibility
    public static class VoucherEligibilityResponse {
        private UserVoucher userVoucher;
        private Voucher voucher;
        private boolean eligible;
        private double discountAmount;
        private String reason; // Reason if not eligible
        
        public UserVoucher getUserVoucher() { return userVoucher; }
        public void setUserVoucher(UserVoucher userVoucher) { this.userVoucher = userVoucher; }
        
        public Voucher getVoucher() { return voucher; }
        public void setVoucher(Voucher voucher) { this.voucher = voucher; }
        
        public boolean isEligible() { return eligible; }
        public void setEligible(boolean eligible) { this.eligible = eligible; }
        
        public double getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
