package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.UserVoucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserVoucherRepository extends MongoRepository<UserVoucher, String> {
    
    // Find by user ID
    List<UserVoucher> findByUserId(String userId);
    
    // Find by user ID and voucher ID
    Optional<UserVoucher> findByUserIdAndVoucherId(String userId, String voucherId);
    
    // Find by user ID and voucher code
    Optional<UserVoucher> findByUserIdAndVoucherCode(String userId, String voucherCode);
    
    // Find active vouchers for user (not used and not expired)
    @Query("{'userId': ?0, 'isUsed': false, 'expiresAt': {$gte: ?1}}")
    List<UserVoucher> findActiveVouchersByUserId(String userId, LocalDateTime now);
    
    // Find used vouchers for user
    List<UserVoucher> findByUserIdAndIsUsedTrue(String userId);
    
    // Find unused vouchers for user
    List<UserVoucher> findByUserIdAndIsUsedFalse(String userId);
    
    // Find expired vouchers for user
    @Query("{'userId': ?0, 'expiresAt': {$lt: ?1}}")
    List<UserVoucher> findExpiredVouchersByUserId(String userId, LocalDateTime now);
    
    // Find by order ID
    Optional<UserVoucher> findByOrderId(String orderId);
    
    // Count active vouchers for user
    @Query(value = "{'userId': ?0, 'isUsed': false, 'expiresAt': {$gte: ?1}}", count = true)
    long countActiveVouchersByUserId(String userId, LocalDateTime now);
}

