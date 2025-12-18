package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Payment;
import com.example.harvesthubbackend.Models.WalletTransaction;
import com.example.harvesthubbackend.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired(required = false)
    private com.example.harvesthubbackend.Service.MockPaymentService mockPaymentService;
    
    @Autowired
    private WalletService walletService;
    
    // Create a new payment
    public Payment createPayment(Payment payment) {
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    // Get payment by ID
    public Optional<Payment> getPaymentById(String id) {
        return paymentRepository.findById(id);
    }
    
    // Get payment by order ID
    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    // Get payment by transaction ID
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    // Get payments by user ID
    public List<Payment> getPaymentsByUserId(String userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    // Get payments by status
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }
    
    // Get payments by method
    public List<Payment> getPaymentsByMethod(String method) {
        return paymentRepository.findByMethod(method);
    }
    
    // Get payments by gateway
    public List<Payment> getPaymentsByGateway(String gateway) {
        return paymentRepository.findByGateway(gateway);
    }
    
    // Update payment status
    public Payment updatePaymentStatus(String id, String status) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setStatus(status);
            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);
        }
        return null;
    }
    
    // Update payment with gateway response
    public Payment updatePaymentWithGatewayResponse(String id, String transactionId, Map<String, Object> gatewayResponse) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            payment.setTransactionId(transactionId);
            payment.setGatewayResponse(gatewayResponse);
            payment.setUpdatedAt(LocalDateTime.now());
            return paymentRepository.save(payment);
        }
        return null;
    }
    
    // Process payment (mock implementation)
    public Payment processPayment(String paymentId, String method, Map<String, Object> paymentData) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (!optionalPayment.isPresent()) {
            return null;
        }
        
        Payment payment = optionalPayment.get();
        
        // Mock payment processing based on method
        switch (method.toLowerCase()) {
            case "cod":
                return processCODPayment(payment);
            case "banking":
                return processBankingPayment(payment, paymentData);
            case "vnpay":
                return processVNPayPayment(payment, paymentData);
            case "zalopay":
                return processZaloPayPayment(payment, paymentData);
            case "qr_pay":
            case "qrpay":
                return processQRPayPayment(payment, paymentData);
            default:
                return null;
        }
    }
    
    // Process COD payment
    private Payment processCODPayment(Payment payment) {
        payment.setStatus("completed");
        payment.setMethod("cod");
        payment.setDescription("Thanh toán khi nhận hàng");
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    // Process banking payment
    private Payment processBankingPayment(Payment payment, Map<String, Object> paymentData) {
        payment.setStatus("pending");
        payment.setMethod("banking");
        payment.setBankCode((String) paymentData.get("bankCode"));
        payment.setBankName((String) paymentData.get("bankName"));
        payment.setAccountNumber((String) paymentData.get("accountNumber"));
        payment.setAccountName((String) paymentData.get("accountName"));
        payment.setDescription("Chuyển khoản ngân hàng");
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    
    // Process VNPay payment
    private Payment processVNPayPayment(Payment payment, Map<String, Object> paymentData) {
        payment.setStatus("pending");
        payment.setMethod("vnpay");
        payment.setGateway("vnpay");
        
        // Get IP address from payment data
        String ipAddress = (String) paymentData.getOrDefault("ipAddress", "127.0.0.1");
        String orderInfo = payment.getDescription() != null ? payment.getDescription() : 
                          "Thanh toan don hang " + payment.getOrderId();
        
        // Use mock payment service for testing (when VNPay credentials are DEMO)
        // This allows testing without needing VNPay sandbox access
        String paymentUrl;
        if (mockPaymentService != null) {
            // Use mock payment service
            String bankCardId = (String) paymentData.get("bankCardId");
            paymentUrl = mockPaymentService.createMockPaymentUrl(
                payment.getOrderId(),
                payment.getAmount(),
                orderInfo,
                ipAddress,
                bankCardId
            );
        } else {
            // Fallback: try real VNPay, but if it fails, use simple mock URL
            try {
                paymentUrl = vnPayService.createPaymentUrl(
                    payment.getOrderId(),
                    payment.getAmount(),
                    orderInfo,
                    ipAddress
                );
            } catch (Exception e) {
                // Fallback to simple mock URL
                try {
                    String baseUrl = "http://localhost:8082"; // Default to port 8082 (frontend port)
                    paymentUrl = baseUrl + "/payment/mock?vnp_Amount=" + 
                               ((long)(payment.getAmount() * 100)) +
                               "&vnp_OrderInfo=" + URLEncoder.encode(orderInfo, StandardCharsets.UTF_8.toString()) +
                               "&vnp_TxnRef=" + payment.getOrderId() + "_" + System.currentTimeMillis();
                } catch (Exception ex) {
                    paymentUrl = "http://localhost:8082/payment/mock?orderId=" + payment.getOrderId();
                }
            }
        }
        
        payment.setPaymentUrl(paymentUrl);
        payment.setDescription("Thanh toán qua VNPay");
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    // Process ZaloPay payment
    // Process QR Pay payment
    private Payment processQRPayPayment(Payment payment, Map<String, Object> paymentData) {
        payment.setStatus("pending");
        payment.setMethod("qr_pay");
        payment.setGateway("qr_pay");
        payment.setDescription("Thanh toán bằng QR Pay");
        
        // Generate QR payment URL
        String baseUrl = (String) paymentData.getOrDefault("baseUrl", "http://localhost:8082");
        String paymentUrl = baseUrl + "/payment/qr?orderId=" + payment.getOrderId() + 
                           "&paymentId=" + payment.getId() + 
                           "&amount=" + payment.getAmount();
        
        payment.setPaymentUrl(paymentUrl);
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.save(payment);
    }
    
    private Payment processZaloPayPayment(Payment payment, Map<String, Object> paymentData) {
        payment.setStatus("pending");
        payment.setMethod("zalopay");
        payment.setGateway("zalopay");
        payment.setPaymentUrl(generateZaloPayPaymentUrl(payment));
        payment.setQrCode(generateQRCode(payment));
        payment.setDescription("Thanh toán qua ZaloPay");
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    
    // Generate ZaloPay payment URL (mock)
    private String generateZaloPayPaymentUrl(Payment payment) {
        return "https://zalopay.vn/payment?amount=" + payment.getAmount() + "&orderId=" + payment.getOrderId();
    }
    
    // Generate QR code (mock)
    private String generateQRCode(Payment payment) {
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    }
    
    // Verify payment callback
    public Payment verifyPaymentCallback(String transactionId, Map<String, Object> callbackData) {
        System.out.println("=== PaymentService.verifyPaymentCallback ===");
        System.out.println("TransactionId: " + transactionId);
        System.out.println("CallbackData: " + callbackData);
        
        Optional<Payment> optionalPayment = paymentRepository.findByTransactionId(transactionId);
        if (!optionalPayment.isPresent()) {
            // Try to find by order ID if transaction ID not found
            String vnpTxnRef = (String) callbackData.get("vnp_TxnRef");
            System.out.println("vnp_TxnRef from callback: " + vnpTxnRef);
            
            if (vnpTxnRef != null) {
                // Extract order ID from vnp_TxnRef (format: orderId_timestamp)
                // For WALLET_DEPOSIT: WALLET_DEPOSIT_{transactionId}_{timestamp}
                // We need to extract: WALLET_DEPOSIT_{transactionId}
                String orderId = vnpTxnRef;
                if (vnpTxnRef.startsWith("WALLET_DEPOSIT_")) {
                    // Split and take first 2 parts: WALLET_DEPOSIT and transactionId
                    String[] parts = vnpTxnRef.split("_");
                    System.out.println("WALLET_DEPOSIT detected, parts count: " + parts.length);
                    if (parts.length >= 3) {
                        orderId = parts[0] + "_" + parts[1]; // WALLET_DEPOSIT_{transactionId}
                        System.out.println("Extracted orderId (with timestamp): " + orderId);
                    } else if (parts.length == 2) {
                        orderId = vnpTxnRef; // Already correct format
                        System.out.println("Using full vnpTxnRef as orderId: " + orderId);
                    }
                } else {
                    // For other orderIds, just take the first part (before first underscore)
                    String[] parts = vnpTxnRef.split("_");
                    if (parts.length > 0) {
                        orderId = parts[0];
                    }
                    System.out.println("Regular orderId extracted: " + orderId);
                }
                System.out.println("Looking for payment with orderId: " + orderId + " (from vnp_TxnRef: " + vnpTxnRef + ")");
                optionalPayment = paymentRepository.findByOrderId(orderId);
                if (optionalPayment.isPresent()) {
                    System.out.println("Payment found by orderId: " + optionalPayment.get().getId());
                } else {
                    System.err.println("Payment NOT found by orderId: " + orderId);
                }
            }
        } else {
            System.out.println("Payment found by transactionId: " + optionalPayment.get().getId());
        }
        
        if (!optionalPayment.isPresent()) {
            return null;
        }
        
        Payment payment = optionalPayment.get();
        
        // Convert callback data to Map<String, String> for VNPay verification
        Map<String, String> vnpayParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : callbackData.entrySet()) {
            vnpayParams.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        
        // Verify with VNPay
        if ("vnpay".equals(payment.getGateway())) {
            boolean isValid = vnPayService.verifyPayment(vnpayParams);
            if (isValid && vnPayService.isPaymentSuccess(vnpayParams)) {
                payment.setStatus("completed");
                String vnpTransactionNo = vnPayService.getTransactionId(vnpayParams);
                if (vnpTransactionNo != null) {
                    payment.setTransactionId(vnpTransactionNo);
                }
            } else {
                payment.setStatus("failed");
            }
        } else {
            // For other gateways, use simple status check
            String status = (String) callbackData.get("status");
            if ("success".equals(status) || "00".equals(status)) {
                payment.setStatus("completed");
            } else {
                payment.setStatus("failed");
            }
        }
        
        payment.setGatewayResponse(callbackData);
        payment.setUpdatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        
        // If payment is completed and orderId starts with "WALLET_DEPOSIT_", complete wallet deposit
        if ("completed".equals(savedPayment.getStatus()) && savedPayment.getOrderId() != null 
            && savedPayment.getOrderId().startsWith("WALLET_DEPOSIT_")) {
            try {
                // Extract transaction ID from orderId (format: WALLET_DEPOSIT_{transactionId} or WALLET_DEPOSIT_{transactionId}_{timestamp})
                String orderId = savedPayment.getOrderId();
                System.out.println("Processing wallet deposit completion for orderId: " + orderId);
                
                String walletTransactionId = orderId.replace("WALLET_DEPOSIT_", "");
                
                // If there's timestamp, remove it (format: transactionId_timestamp)
                if (walletTransactionId.contains("_")) {
                    String[] parts = walletTransactionId.split("_");
                    walletTransactionId = parts[0]; // Take only the transaction ID part
                }
                
                System.out.println("Extracted wallet transaction ID: " + walletTransactionId);
                
                if (walletTransactionId != null && !walletTransactionId.isEmpty()) {
                    // Complete wallet deposit transaction
                    System.out.println("Calling walletService.completeDeposit for transaction: " + walletTransactionId);
                    WalletTransaction completedTransaction = walletService.completeDeposit(walletTransactionId);
                    System.out.println("Wallet deposit completed successfully! New balance: " + completedTransaction.getBalanceAfter());
                } else {
                    System.err.println("Could not extract wallet transaction ID from orderId: " + orderId);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Wallet deposit transaction not found: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                // Log error but don't fail payment verification
                System.err.println("Error completing wallet deposit: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Debug log
            if (savedPayment.getOrderId() != null) {
                System.out.println("Payment orderId: " + savedPayment.getOrderId() + ", status: " + savedPayment.getStatus() + 
                    ", startsWith WALLET_DEPOSIT_: " + savedPayment.getOrderId().startsWith("WALLET_DEPOSIT_"));
            }
        }
        
        return savedPayment;
    }
    
    // Create payment and generate payment URL for online payment
    public Payment createOnlinePayment(String orderId, String userId, double amount, String method, String ipAddress) {
        return createOnlinePayment(orderId, userId, amount, method, ipAddress, null);
    }
    
    // Create payment and generate payment URL for online payment (with bank card support)
    public Payment createOnlinePayment(String orderId, String userId, double amount, String method, String ipAddress, String bankCardId) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus("pending");
        payment.setDescription("Thanh toán đơn hàng " + orderId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("ipAddress", ipAddress);
        paymentData.put("baseUrl", "http://localhost:8082"); // Frontend base URL for QR Pay
        if (bankCardId != null && !bankCardId.isEmpty()) {
            paymentData.put("bankCardId", bankCardId);
        }
        
        // If method is bank_card, process it specially
        if ("bank_card".equals(method) && bankCardId != null && !bankCardId.isEmpty()) {
            return processBankCardPayment(payment, paymentData);
        }
        
        return processPayment(paymentRepository.save(payment).getId(), method, paymentData);
    }
    
    // Process bank card payment
    private Payment processBankCardPayment(Payment payment, Map<String, Object> paymentData) {
        String bankCardId = (String) paymentData.get("bankCardId");
        
        // For now, we'll use VNPay gateway for bank card payments
        // In a real scenario, you would integrate with a card payment gateway
        payment.setMethod("bank_card");
        payment.setGateway("vnpay"); // Use VNPay as gateway for card payments
        payment.setStatus("pending");
        
        String ipAddress = (String) paymentData.getOrDefault("ipAddress", "127.0.0.1");
        String orderInfo = "Thanh toan don hang " + payment.getOrderId() + " bang the ngan hang";
        
        // Generate payment URL (using VNPay or mock)
        String paymentUrl;
        if (mockPaymentService != null) {
            paymentUrl = mockPaymentService.createMockPaymentUrl(
                payment.getOrderId(),
                payment.getAmount(),
                orderInfo,
                ipAddress,
                bankCardId
            );
        } else {
            try {
                paymentUrl = vnPayService.createPaymentUrl(
                    payment.getOrderId(),
                    payment.getAmount(),
                    orderInfo,
                    ipAddress
                );
                // Add bankCardId to URL if provided
                if (bankCardId != null && !bankCardId.isEmpty()) {
                    paymentUrl += (paymentUrl.contains("?") ? "&" : "?") + "bankCardId=" + bankCardId;
                }
            } catch (Exception e) {
                try {
                    String baseUrl = "http://localhost:8082";
                    paymentUrl = baseUrl + "/payment/mock?vnp_Amount=" + 
                               ((long)(payment.getAmount() * 100)) +
                               "&vnp_OrderInfo=" + URLEncoder.encode(orderInfo, StandardCharsets.UTF_8.toString()) +
                               "&vnp_TxnRef=" + payment.getOrderId() + "_" + System.currentTimeMillis();
                    // Add bankCardId to URL if provided
                    if (bankCardId != null && !bankCardId.isEmpty()) {
                        paymentUrl += "&bankCardId=" + bankCardId;
                    }
                } catch (Exception ex) {
                    paymentUrl = "http://localhost:8082/payment/mock?orderId=" + payment.getOrderId();
                    if (bankCardId != null && !bankCardId.isEmpty()) {
                        paymentUrl += "&bankCardId=" + bankCardId;
                    }
                }
            }
        }
        
        payment.setPaymentUrl(paymentUrl);
        payment.setDescription("Thanh toán bằng thẻ ngân hàng");
        payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        payment.setUpdatedAt(LocalDateTime.now());
        
        // Store bank card ID in gateway response for reference
        Map<String, Object> gatewayResponse = new HashMap<>();
        gatewayResponse.put("bankCardId", bankCardId);
        payment.setGatewayResponse(gatewayResponse);
        
        return paymentRepository.save(payment);
    }
    
    // Refund payment
    public Payment refundPayment(String id, double refundAmount, String reason) {
        Optional<Payment> optionalPayment = paymentRepository.findById(id);
        if (!optionalPayment.isPresent()) {
            return null;
        }
        
        Payment payment = optionalPayment.get();
        
        if (!payment.canRefund()) {
            return null;
        }
        
        payment.setStatus("refunded");
        payment.setRefundAmount(refundAmount);
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
    
    // Get payment statistics
    public Map<String, Object> getPaymentStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", paymentRepository.count());
        stats.put("completedPayments", paymentRepository.countByStatus("completed"));
        stats.put("pendingPayments", paymentRepository.countByStatus("pending"));
        stats.put("failedPayments", paymentRepository.countByStatus("failed"));
        stats.put("refundedPayments", paymentRepository.countByStatus("refunded"));
        return stats;
    }
    
    // Get payment methods statistics
    public Map<String, Long> getPaymentMethodsStatistics() {
        Map<String, Long> methods = new HashMap<>();
        methods.put("cod", paymentRepository.countByMethod("cod"));
        methods.put("banking", paymentRepository.countByMethod("banking"));
        methods.put("momo", paymentRepository.countByMethod("momo"));
        methods.put("vnpay", paymentRepository.countByMethod("vnpay"));
        methods.put("zalopay", paymentRepository.countByMethod("zalopay"));
        return methods;
    }
    
    // Delete payment
    public boolean deletePayment(String id) {
        if (paymentRepository.existsById(id)) {
            paymentRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Check if payment exists
    public boolean paymentExists(String id) {
        return paymentRepository.existsById(id);
    }
}
