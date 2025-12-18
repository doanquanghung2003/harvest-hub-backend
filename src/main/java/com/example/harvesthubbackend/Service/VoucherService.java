package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.*;
import com.example.harvesthubbackend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class VoucherService {
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private VoucherUsageRepository voucherUsageRepository;
    
    @Autowired
    private UserVoucherRepository userVoucherRepository;
    
    @Autowired
    private UserService userService;
    
    // Create a new voucher
    public Voucher createVoucher(Voucher voucher) {
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());
        return voucherRepository.save(voucher);
    }
    
    // Get voucher by ID
    public Optional<Voucher> getVoucherById(String id) {
        return voucherRepository.findById(id);
    }
    
    // Get voucher by code
    public Optional<Voucher> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code);
    }
    
    // Get all vouchers
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }
    
    // Get active vouchers
    public List<Voucher> getActiveVouchers() {
        return voucherRepository.findActiveVouchers(LocalDateTime.now());
    }
    
    // Get vouchers by shop
    public List<Voucher> getVouchersByShop(String shopId) {
        return voucherRepository.findByShopId(shopId);
    }
    
    // Get platform-wide vouchers
    public List<Voucher> getPlatformVouchers() {
        return voucherRepository.findPlatformVouchers();
    }
    
    // Get vouchers by type
    public List<Voucher> getVouchersByType(String type) {
        return voucherRepository.findByType(type);
    }
    
    // Get vouchers by status
    public List<Voucher> getVouchersByStatus(String status) {
        return voucherRepository.findByStatus(status);
    }
    
    // Get vouchers for specific user
    public List<Voucher> getVouchersForUser(String userId) {
        return voucherRepository.findVouchersForUser(userId);
    }
    
    // Get vouchers for specific category
    public List<Voucher> getVouchersForCategory(String categoryId) {
        return voucherRepository.findVouchersForCategory(categoryId);
    }
    
    // Get vouchers for specific product
    public List<Voucher> getVouchersForProduct(String productId) {
        return voucherRepository.findVouchersForProduct(productId);
    }
    
    // Get valid vouchers for order
    public List<Voucher> getValidVouchersForOrder(double orderAmount, String shopId) {
        return voucherRepository.findValidVouchersForOrder(LocalDateTime.now(), orderAmount, shopId);
    }
    
    // Get vouchers expiring soon
    public List<Voucher> getVouchersExpiringSoon(int days) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(days);
        return voucherRepository.findVouchersExpiringSoon(start, end);
    }
    
    // Get vouchers near usage limit
    public List<Voucher> getVouchersNearUsageLimit(int threshold) {
        return voucherRepository.findVouchersNearUsageLimit(threshold);
    }
    
    // Update voucher
    public Voucher updateVoucher(String id, Voucher voucherDetails) {
        Optional<Voucher> optionalVoucher = voucherRepository.findById(id);
        if (optionalVoucher.isPresent()) {
            Voucher voucher = optionalVoucher.get();
            voucher.setName(voucherDetails.getName());
            voucher.setDescription(voucherDetails.getDescription());
            voucher.setType(voucherDetails.getType());
            voucher.setValue(voucherDetails.getValue());
            voucher.setMinOrderAmount(voucherDetails.getMinOrderAmount());
            voucher.setMaxDiscountAmount(voucherDetails.getMaxDiscountAmount());
            voucher.setShopId(voucherDetails.getShopId());
            voucher.setCategoryIds(voucherDetails.getCategoryIds());
            voucher.setProductIds(voucherDetails.getProductIds());
            voucher.setUserIds(voucherDetails.getUserIds());
            voucher.setUsageLimit(voucherDetails.getUsageLimit());
            voucher.setStartDate(voucherDetails.getStartDate());
            voucher.setEndDate(voucherDetails.getEndDate());
            voucher.setStatus(voucherDetails.getStatus());
            voucher.setUpdatedAt(LocalDateTime.now());
            return voucherRepository.save(voucher);
        }
        return null;
    }
    
    // Update voucher status
    public Voucher updateVoucherStatus(String id, String status) {
        Optional<Voucher> optionalVoucher = voucherRepository.findById(id);
        if (optionalVoucher.isPresent()) {
            Voucher voucher = optionalVoucher.get();
            voucher.setStatus(status);
            voucher.setUpdatedAt(LocalDateTime.now());
            return voucherRepository.save(voucher);
        }
        return null;
    }
    
    // Use voucher (increment used count)
    public Voucher useVoucher(String id) {
        Optional<Voucher> optionalVoucher = voucherRepository.findById(id);
        if (optionalVoucher.isPresent()) {
            Voucher voucher = optionalVoucher.get();
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucher.setUpdatedAt(LocalDateTime.now());
            return voucherRepository.save(voucher);
        }
        return null;
    }
    
    // Validate voucher
    public boolean validateVoucher(String code, double orderAmount, String userId, String shopId, String categoryId, String productId) {
        Optional<Voucher> optionalVoucher = voucherRepository.findByCode(code);
        if (!optionalVoucher.isPresent()) {
            return false;
        }
        
        Voucher voucher = optionalVoucher.get();
        
        // Check if voucher is valid
        if (!voucher.isValid()) {
            return false;
        }
        
        // Check minimum order amount
        if (orderAmount < voucher.getMinOrderAmount()) {
            return false;
        }
        
        // Check shop restriction
        if (voucher.getShopId() != null && !voucher.getShopId().equals(shopId)) {
            return false;
        }
        
        // Check user restriction
        if (voucher.getUserIds() != null && !voucher.getUserIds().contains(userId)) {
            return false;
        }
        
        // Check category restriction
        if (voucher.getCategoryIds() != null && !voucher.getCategoryIds().contains(categoryId)) {
            return false;
        }
        
        // Check product restriction
        if (voucher.getProductIds() != null && !voucher.getProductIds().contains(productId)) {
            return false;
        }
        
        return true;
    }
    
    // Calculate discount
    public double calculateDiscount(String code, double orderAmount) {
        Optional<Voucher> optionalVoucher = voucherRepository.findByCode(code);
        if (!optionalVoucher.isPresent()) {
            return 0;
        }
        
        Voucher voucher = optionalVoucher.get();
        return voucher.calculateDiscount(orderAmount);
    }
    
    // Calculate discount with shipping fee (for free shipping)
    public double calculateDiscount(String code, double orderAmount, double shippingFee) {
        Optional<Voucher> optionalVoucher = voucherRepository.findByCode(code);
        if (!optionalVoucher.isPresent()) {
            return 0;
        }
        
        Voucher voucher = optionalVoucher.get();
        
        if (voucher.getType().equals("free_shipping")) {
            return shippingFee;
        }
        
        return voucher.calculateDiscount(orderAmount);
    }
    
    // Validate voucher for order (comprehensive validation)
    public boolean validateVoucherForOrder(String code, String userId, double orderAmount, 
                                           String shopId, List<String> productIds, 
                                           List<String> categoryIds) {
        Optional<Voucher> optionalVoucher = voucherRepository.findByCode(code);
        if (!optionalVoucher.isPresent()) {
            return false;
        }
        
        Voucher voucher = optionalVoucher.get();
        
        // 1. Check voucher is valid
        if (!voucher.isValid()) {
            return false;
        }
        
        // 2. Check minimum order amount
        if (orderAmount < voucher.getMinOrderAmount()) {
            return false;
        }
        
        // 3. Check shop restriction
        if (voucher.getShopId() != null && !voucher.getShopId().equals(shopId)) {
            return false;
        }
        
        // 4. Check user restriction
        if (voucher.getUserIds() != null && !voucher.getUserIds().contains(userId)) {
            return false;
        }
        
        // 5. Check membership type
        if (voucher.getMembershipType() != null && !voucher.getMembershipType().isEmpty()) {
            User user = userService.getById(userId);
            if (user == null || user.getMembershipType() == null) {
                return false;
            }
            // Check if user's membership type matches or is higher
            if (!isMembershipTypeEligible(user.getMembershipType(), voucher.getMembershipType())) {
                return false;
            }
        }
        
        // 6. Check product restrictions
        if (voucher.getProductIds() != null && !voucher.getProductIds().isEmpty()) {
            if (productIds == null || productIds.isEmpty()) {
                return false;
            }
            boolean hasMatchingProduct = productIds.stream()
                .anyMatch(voucher.getProductIds()::contains);
            if (!hasMatchingProduct) {
                return false;
            }
        }
        
        // 7. Check excluded products
        if (voucher.getExcludedProductIds() != null && !voucher.getExcludedProductIds().isEmpty()) {
            if (productIds != null && !productIds.isEmpty()) {
                boolean hasExcludedProduct = productIds.stream()
                    .anyMatch(voucher.getExcludedProductIds()::contains);
                if (hasExcludedProduct) {
                    return false;
                }
            }
        }
        
        // 8. Check category restrictions
        if (voucher.getCategoryIds() != null && !voucher.getCategoryIds().isEmpty()) {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return false;
            }
            boolean hasMatchingCategory = categoryIds.stream()
                .anyMatch(voucher.getCategoryIds()::contains);
            if (!hasMatchingCategory) {
                return false;
            }
        }
        
        // 9. Check excluded categories
        if (voucher.getExcludedCategoryIds() != null && !voucher.getExcludedCategoryIds().isEmpty()) {
            if (categoryIds != null && !categoryIds.isEmpty()) {
                boolean hasExcludedCategory = categoryIds.stream()
                    .anyMatch(voucher.getExcludedCategoryIds()::contains);
                if (hasExcludedCategory) {
                    return false;
                }
            }
        }
        
        // 10. Check max usage per user
        if (voucher.getMaxUsagePerUser() > 0) {
            long usageCount = voucherUsageRepository.countByUserIdAndVoucherId(userId, voucher.getId());
            if (usageCount >= voucher.getMaxUsagePerUser()) {
                return false;
            }
        }
        
        return true;
    }
    
    // Check if user's membership type is eligible
    private boolean isMembershipTypeEligible(String userMembership, String requiredMembership) {
        if (userMembership == null || requiredMembership == null) {
            return false;
        }
        
        // VIP tier hierarchy: STANDARD < VIP1 < VIP2 < VIP3
        int userTier = getMembershipTier(userMembership);
        int requiredTier = getMembershipTier(requiredMembership);
        
        return userTier >= requiredTier;
    }
    
    private int getMembershipTier(String membership) {
        if (membership == null) return 0;
        switch (membership.toUpperCase()) {
            case "VIP3": return 3;
            case "VIP2": return 2;
            case "VIP1": return 1;
            case "STANDARD":
            default: return 0;
        }
    }
    
    // Apply voucher to cart
    public double applyVoucherToCart(String userId, String voucherCode, double cartSubtotal) {
        if (!validateVoucherForOrder(voucherCode, userId, cartSubtotal, null, null, null)) {
            throw new RuntimeException("Voucher không hợp lệ");
        }
        
        return calculateDiscount(voucherCode, cartSubtotal);
    }
    
    // Apply voucher to order and create usage record
    public VoucherUsage applyVoucherToOrder(String userId, String orderId, String voucherCode, 
                                            double orderAmount, double discountAmount) {
        Optional<Voucher> optionalVoucher = voucherRepository.findByCode(voucherCode);
        if (!optionalVoucher.isPresent()) {
            throw new RuntimeException("Voucher không tồn tại");
        }
        
        Voucher voucher = optionalVoucher.get();
        
        // Create VoucherUsage record
        VoucherUsage usage = new VoucherUsage();
        usage.setVoucherId(voucher.getId());
        usage.setVoucherCode(voucherCode);
        usage.setUserId(userId);
        usage.setOrderId(orderId);
        usage.setDiscountAmount(discountAmount);
        usage.setOrderAmount(orderAmount);
        usage.setStatus("used");
        
        VoucherUsage savedUsage = voucherUsageRepository.save(usage);
        
        // Increment voucher used count
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucher.setUpdatedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
        
        // Update UserVoucher if exists
        Optional<UserVoucher> userVoucherOpt = userVoucherRepository.findByUserIdAndVoucherCode(userId, voucherCode);
        if (userVoucherOpt.isPresent()) {
            UserVoucher userVoucher = userVoucherOpt.get();
            if (!userVoucher.isUsed()) {
                userVoucher.setUsed(true);
                userVoucher.setUsedAt(LocalDateTime.now());
                userVoucher.setOrderId(orderId);
                userVoucherRepository.save(userVoucher);
            }
        }
        
        return savedUsage;
    }
    
    // Refund voucher when order is cancelled
    public void refundVoucher(String orderId) {
        Optional<VoucherUsage> usageOpt = voucherUsageRepository.findByOrderId(orderId);
        if (!usageOpt.isPresent()) {
            return; // No voucher used for this order
        }
        
        VoucherUsage usage = usageOpt.get();
        if ("refunded".equals(usage.getStatus())) {
            return; // Already refunded
        }
        
        // Update VoucherUsage status
        usage.setStatus("refunded");
        usage.setRefundedAt(LocalDateTime.now());
        voucherUsageRepository.save(usage);
        
        // Decrement voucher used count
        Optional<Voucher> voucherOpt = voucherRepository.findById(usage.getVoucherId());
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setUsedCount(Math.max(0, voucher.getUsedCount() - 1));
            voucher.setUpdatedAt(LocalDateTime.now());
            voucherRepository.save(voucher);
        }
        
        // Update UserVoucher if exists
        Optional<UserVoucher> userVoucherOpt = userVoucherRepository.findByUserIdAndVoucherCode(
            usage.getUserId(), usage.getVoucherCode());
        if (userVoucherOpt.isPresent()) {
            UserVoucher userVoucher = userVoucherOpt.get();
            userVoucher.setUsed(false);
            userVoucher.setUsedAt(null);
            userVoucher.setOrderId(null);
            userVoucherRepository.save(userVoucher);
        }
    }
    
    // Grant voucher to user
    public UserVoucher grantVoucherToUser(String userId, String voucherId) {
        Optional<Voucher> voucherOpt = voucherRepository.findById(voucherId);
        if (!voucherOpt.isPresent()) {
            throw new RuntimeException("Voucher không tồn tại");
        }
        
        Voucher voucher = voucherOpt.get();
        
        // Check if user already has this voucher
        Optional<UserVoucher> existing = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId);
        if (existing.isPresent()) {
            return existing.get(); // Already granted
        }
        
        // Create UserVoucher
        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUserId(userId);
        userVoucher.setVoucherId(voucherId);
        userVoucher.setVoucherCode(voucher.getCode());
        userVoucher.setReceivedAt(LocalDateTime.now());
        userVoucher.setExpiresAt(voucher.getEndDate());
        userVoucher.setUsed(false);
        
        return userVoucherRepository.save(userVoucher);
    }
    
    // Get user's vouchers
    public List<UserVoucher> getUserVouchers(String userId) {
        return userVoucherRepository.findByUserId(userId);
    }
    
    // Get user's active vouchers
    public List<UserVoucher> getUserActiveVouchers(String userId) {
        return userVoucherRepository.findActiveVouchersByUserId(userId, LocalDateTime.now());
    }
    
    // Get eligible vouchers for order
    public List<Voucher> getEligibleVouchers(String userId, double orderAmount, String shopId, 
                                             List<String> productIds, List<String> categoryIds) {
        List<Voucher> allVouchers = getActiveVouchers();
        List<Voucher> eligible = new ArrayList<>();
        
        for (Voucher voucher : allVouchers) {
            if (validateVoucherForOrder(voucher.getCode(), userId, orderAmount, shopId, 
                                       productIds, categoryIds)) {
                eligible.add(voucher);
            }
        }
        
        return eligible;
    }
    
    // Delete voucher
    public boolean deleteVoucher(String id) {
        if (voucherRepository.existsById(id)) {
            voucherRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Check if voucher exists
    public boolean voucherExists(String id) {
        return voucherRepository.existsById(id);
    }
    
    // Check if voucher code exists
    public boolean voucherCodeExists(String code) {
        return voucherRepository.findByCode(code).isPresent();
    }
    
    // Get voucher count by status
    public long getVoucherCountByStatus(String status) {
        return voucherRepository.countByStatus(status);
    }
}
