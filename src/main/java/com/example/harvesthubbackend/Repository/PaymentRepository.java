package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {
    
    // Find payment by order ID
    Optional<Payment> findByOrderId(String orderId);
    
    // Find payment by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Find payments by user ID
    List<Payment> findByUserId(String userId);
    
    // Find payments by status
    List<Payment> findByStatus(String status);
    
    // Find payments by method
    List<Payment> findByMethod(String method);
    
    // Find payments by gateway
    List<Payment> findByGateway(String gateway);
    
    // Find payments by user ID and status
    List<Payment> findByUserIdAndStatus(String userId, String status);
    
    // Find payments by order ID and status
    List<Payment> findByOrderIdAndStatus(String orderId, String status);
    
    // Find payments by method and status
    List<Payment> findByMethodAndStatus(String method, String status);
    
    // Find payments by gateway and status
    List<Payment> findByGatewayAndStatus(String gateway, String status);
    
    // Find payments created between dates
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find payments by amount range
    @Query("{'amount': {$gte: ?0, $lte: ?1}}")
    List<Payment> findByAmountBetween(double minAmount, double maxAmount);
    
    // Find expired payments
    @Query("{'expiredAt': {$lt: ?0}, 'status': 'pending'}")
    List<Payment> findExpiredPayments(LocalDateTime now);
    
    // Find payments by user ID and method
    List<Payment> findByUserIdAndMethod(String userId, String method);
    
    // Find payments by user ID and gateway
    List<Payment> findByUserIdAndGateway(String userId, String gateway);
    
    // Find payments sorted by creation date
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<Payment> findAllOrderByCreatedAtDesc();
    
    // Find payments sorted by amount
    @Query(value = "{}", sort = "{'amount': -1}")
    List<Payment> findAllOrderByAmountDesc();
    
    // Find payments sorted by updated date
    @Query(value = "{}", sort = "{'updatedAt': -1}")
    List<Payment> findAllOrderByUpdatedAtDesc();
    
    // Count payments by status
    long countByStatus(String status);
    
    // Count payments by method
    long countByMethod(String method);
    
    // Count payments by gateway
    long countByGateway(String gateway);
    
    // Count payments by user ID
    long countByUserId(String userId);
    
    // Count payments by user ID and status
    long countByUserIdAndStatus(String userId, String status);
    
    // Count payments by method and status
    long countByMethodAndStatus(String method, String status);
    
    // Count payments by gateway and status
    long countByGatewayAndStatus(String gateway, String status);
    
    // Count payments created between dates
    @Query(value = "{'createdAt': {$gte: ?0, $lte: ?1}}", count = true)
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count payments by amount range
    @Query(value = "{'amount': {$gte: ?0, $lte: ?1}}", count = true)
    long countByAmountBetween(double minAmount, double maxAmount);
    
    // Sum total amount by status
    @Query(value = "{'status': ?0}", fields = "{'amount': 1}")
    List<Payment> findByStatusForSum(String status);
    
    // Sum total amount by user ID
    @Query(value = "{'userId': ?0}", fields = "{'amount': 1}")
    List<Payment> findByUserIdForSum(String userId);
    
    // Sum total amount by method
    @Query(value = "{'method': ?0}", fields = "{'amount': 1}")
    List<Payment> findByMethodForSum(String method);
    
    // Sum total amount by gateway
    @Query(value = "{'gateway': ?0}", fields = "{'amount': 1}")
    List<Payment> findByGatewayForSum(String gateway);
    
    // Find payments with refunds
    @Query("{'refundAmount': {$gt: 0}}")
    List<Payment> findPaymentsWithRefunds();
    
    // Find payments by refund reason
    List<Payment> findByRefundReason(String refundReason);
    
    // Find payments by bank code
    List<Payment> findByBankCode(String bankCode);
    
    // Find payments by bank name
    List<Payment> findByBankName(String bankName);
    
    // Find payments by account number
    List<Payment> findByAccountNumber(String accountNumber);
    
    // Find payments by account name
    List<Payment> findByAccountName(String accountName);
}
