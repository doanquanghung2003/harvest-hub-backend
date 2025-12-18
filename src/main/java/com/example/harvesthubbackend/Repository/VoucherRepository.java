package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Voucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {
    
    // Find voucher by code
    Optional<Voucher> findByCode(String code);
    
    // Find active vouchers
    @Query("{'status': 'active', 'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}}")
    List<Voucher> findActiveVouchers(LocalDateTime now);
    
    // Find vouchers by shop
    List<Voucher> findByShopId(String shopId);
    
    // Find platform-wide vouchers (shopId is null)
    @Query("{'shopId': null}")
    List<Voucher> findPlatformVouchers();
    
    // Find vouchers by type
    List<Voucher> findByType(String type);
    
    // Find vouchers by status
    List<Voucher> findByStatus(String status);
    
    // Find vouchers for specific user
    @Query("{'$or': [{'userIds': null}, {'userIds': {$in: [?0]}}]}")
    List<Voucher> findVouchersForUser(String userId);
    
    // Find vouchers for specific category
    @Query("{'$or': [{'categoryIds': null}, {'categoryIds': {$in: [?0]}}]}")
    List<Voucher> findVouchersForCategory(String categoryId);
    
    // Find vouchers for specific product
    @Query("{'$or': [{'productIds': null}, {'productIds': {$in: [?0]}}]}")
    List<Voucher> findVouchersForProduct(String productId);
    
    // Find valid vouchers for order
    @Query("{'status': 'active', 'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}, 'minOrderAmount': {$lte: ?1}, '$or': [{'shopId': ?2}, {'shopId': null}]}")
    List<Voucher> findValidVouchersForOrder(LocalDateTime now, double orderAmount, String shopId);
    
    // Find vouchers expiring soon
    @Query("{'status': 'active', 'endDate': {$gte: ?0, $lte: ?1}}")
    List<Voucher> findVouchersExpiringSoon(LocalDateTime start, LocalDateTime end);
    
    // Find vouchers by usage limit
    @Query("{'usageLimit': {$ne: -1}, 'usedCount': {$gte: ?0}}")
    List<Voucher> findVouchersNearUsageLimit(int threshold);
    
    // Count vouchers by status
    long countByStatus(String status);
    
    // Find vouchers sorted by creation date
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<Voucher> findAllOrderByCreatedAtDesc();
    
    // Find vouchers sorted by end date
    @Query(value = "{}", sort = "{'endDate': 1}")
    List<Voucher> findAllOrderByEndDateAsc();
}
