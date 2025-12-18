package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Payment;
import com.example.harvesthubbackend.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    // Create a new payment
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        Payment createdPayment = paymentService.createPayment(payment);
        return ResponseEntity.ok(createdPayment);
    }
    
    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // Get payment by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId) {
        Optional<Payment> payment = paymentService.getPaymentByOrderId(orderId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // Get payment by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        Optional<Payment> payment = paymentService.getPaymentByTransactionId(transactionId);
        return payment.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }
    
    // Get payments by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUserId(@PathVariable String userId) {
        List<Payment> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }
    
    // Get payments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    // Get payments by method
    @GetMapping("/method/{method}")
    public ResponseEntity<List<Payment>> getPaymentsByMethod(@PathVariable String method) {
        List<Payment> payments = paymentService.getPaymentsByMethod(method);
        return ResponseEntity.ok(payments);
    }
    
    // Get payments by gateway
    @GetMapping("/gateway/{gateway}")
    public ResponseEntity<List<Payment>> getPaymentsByGateway(@PathVariable String gateway) {
        List<Payment> payments = paymentService.getPaymentsByGateway(gateway);
        return ResponseEntity.ok(payments);
    }
    
    // Update payment status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(@PathVariable String id, @RequestParam String status) {
        Payment updatedPayment = paymentService.updatePaymentStatus(id, status);
        if (updatedPayment != null) {
            return ResponseEntity.ok(updatedPayment);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Process payment
    @PostMapping("/{id}/process")
    public ResponseEntity<Payment> processPayment(
            @PathVariable String id,
            @RequestParam String method,
            @RequestBody Map<String, Object> paymentData) {
        Payment processedPayment = paymentService.processPayment(id, method, paymentData);
        if (processedPayment != null) {
            return ResponseEntity.ok(processedPayment);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Create online payment and get payment URL
    @PostMapping("/create-online")
    public ResponseEntity<?> createOnlinePayment(
            @RequestBody Map<String, Object> request) {
        try {
            String orderId = (String) request.get("orderId");
            String userId = (String) request.get("userId");
            Double amount = ((Number) request.get("amount")).doubleValue();
            String method = (String) request.get("method");
            String ipAddress = (String) request.getOrDefault("ipAddress", "127.0.0.1");
            String bankCardId = (String) request.get("bankCardId");
            
            if (orderId == null || userId == null || amount == null || method == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing required fields"));
            }
            
            Payment payment = paymentService.createOnlinePayment(orderId, userId, amount, method, ipAddress, bankCardId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error creating payment: " + e.getMessage()));
        }
    }
    
    // Verify payment callback from VNPay
    @PostMapping("/callback")
    public ResponseEntity<?> verifyPaymentCallback(@RequestParam Map<String, String> params) {
        try {
            // Convert params to Map<String, Object>
            Map<String, Object> callbackData = new HashMap<>(params);
            
            // Get transaction ID from params
            String transactionId = params.get("vnp_TransactionNo");
            if (transactionId == null) {
                transactionId = params.get("vnp_TxnRef");
            }
            
            Payment verifiedPayment = paymentService.verifyPaymentCallback(transactionId, callbackData);
            if (verifiedPayment != null) {
                // Redirect to frontend with payment result
                String redirectUrl = "http://localhost:5173/payment/return?status=" + 
                    verifiedPayment.getStatus() + "&orderId=" + verifiedPayment.getOrderId();
                return ResponseEntity.status(302)
                    .header("Location", redirectUrl)
                    .build();
            }
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Payment verification failed"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Error verifying payment: " + e.getMessage()));
        }
    }
    
    // Verify payment callback (old endpoint for backward compatibility)
    @PostMapping("/callback/{transactionId}")
    public ResponseEntity<Payment> verifyPaymentCallbackOld(
            @PathVariable String transactionId,
            @RequestBody Map<String, Object> callbackData) {
        Payment verifiedPayment = paymentService.verifyPaymentCallback(transactionId, callbackData);
        if (verifiedPayment != null) {
            return ResponseEntity.ok(verifiedPayment);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Refund payment
    @PostMapping("/{id}/refund")
    public ResponseEntity<Payment> refundPayment(
            @PathVariable String id,
            @RequestParam double refundAmount,
            @RequestParam String reason) {
        Payment refundedPayment = paymentService.refundPayment(id, refundAmount, reason);
        if (refundedPayment != null) {
            return ResponseEntity.ok(refundedPayment);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Get payment statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics() {
        Map<String, Object> statistics = paymentService.getPaymentStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    // Get payment methods statistics
    @GetMapping("/statistics/methods")
    public ResponseEntity<Map<String, Long>> getPaymentMethodsStatistics() {
        Map<String, Long> methods = paymentService.getPaymentMethodsStatistics();
        return ResponseEntity.ok(methods);
    }
    
    // Delete payment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        boolean deleted = paymentService.deletePayment(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if payment exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> paymentExists(@PathVariable String id) {
        boolean exists = paymentService.paymentExists(id);
        return ResponseEntity.ok(exists);
    }
}
