package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.VoucherUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherUsageRepository extends MongoRepository<VoucherUsage, String> {
    
    // Find by order ID
    Optional<VoucherUsage> findByOrderId(String orderId);
    
    // Find by user ID
    List<VoucherUsage> findByUserId(String userId);
    
    // Find by voucher ID
    List<VoucherUsage> findByVoucherId(String voucherId);
    
    // Find by voucher code
    List<VoucherUsage> findByVoucherCode(String voucherCode);
    
    // Find by status
    List<VoucherUsage> findByStatus(String status);
    
    // Count usage by user and voucher
    long countByUserIdAndVoucherId(String userId, String voucherId);
    
    // Count usage by user and voucher code
    long countByUserIdAndVoucherCode(String userId, String voucherCode);
    
    // Find by user and voucher
    List<VoucherUsage> findByUserIdAndVoucherId(String userId, String voucherId);
    
    // Find by user and order
    Optional<VoucherUsage> findByUserIdAndOrderId(String userId, String orderId);
}

