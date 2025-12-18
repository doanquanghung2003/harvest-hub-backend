package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Wallet;
import com.example.harvesthubbackend.Models.WalletTransaction;
import com.example.harvesthubbackend.Service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private com.example.harvesthubbackend.Service.PaymentService paymentService;
    
    /**
     * Get wallet by user ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getWallet(@PathVariable String userId) {
        try {
            Wallet wallet = walletService.getOrCreateWallet(userId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy thông tin ví: " + e.getMessage()));
        }
    }
    
    /**
     * Deposit money to wallet
     */
    @PostMapping("/{userId}/deposit")
    public ResponseEntity<?> deposit(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            Double amount = ((Number) request.get("amount")).doubleValue();
            String paymentMethod = (String) request.getOrDefault("paymentMethod", "wallet");
            String description = (String) request.getOrDefault("description", "Nạp tiền vào ví");
            
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Số tiền nạp phải lớn hơn 0"));
            }
            
            WalletTransaction transaction = walletService.deposit(userId, amount, paymentMethod, description);
            
            // If payment method is online (vnpay, banking, bank_card), create payment
            if (!"wallet".equals(paymentMethod) && transaction.getStatus().equals("pending")) {
                // Create online payment for deposit
                try {
                    String ipAddress = (String) request.getOrDefault("ipAddress", "127.0.0.1");
                    String bankCardId = (String) request.get("bankCardId");
                    com.example.harvesthubbackend.Models.Payment payment = paymentService.createOnlinePayment(
                        "WALLET_DEPOSIT_" + transaction.getId(),
                        userId,
                        amount,
                        paymentMethod,
                        ipAddress,
                        bankCardId
                    );
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("transaction", transaction);
                    response.put("payment", payment);
                    return ResponseEntity.ok(response);
                } catch (Exception e) {
                    // If payment creation fails, still return transaction
                    System.err.println("Error creating payment for deposit: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            return ResponseEntity.ok(Map.of("success", true, "transaction", transaction));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi nạp tiền: " + e.getMessage()));
        }
    }
    
    /**
     * Withdraw money from wallet
     */
    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<?> withdraw(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            Double amount = ((Number) request.get("amount")).doubleValue();
            String description = (String) request.getOrDefault("description", "Rút tiền từ ví");
            
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Số tiền rút phải lớn hơn 0"));
            }
            
            WalletTransaction transaction = walletService.withdraw(userId, amount, description);
            return ResponseEntity.ok(Map.of("success", true, "transaction", transaction));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi rút tiền: " + e.getMessage()));
        }
    }
    
    /**
     * Pay with wallet
     */
    @PostMapping("/{userId}/pay")
    public ResponseEntity<?> payWithWallet(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            Double amount = ((Number) request.get("amount")).doubleValue();
            String orderId = (String) request.get("orderId");
            String description = (String) request.getOrDefault("description", "Thanh toán đơn hàng");
            
            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Số tiền thanh toán phải lớn hơn 0"));
            }
            
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Order ID is required"));
            }
            
            WalletTransaction transaction = walletService.payWithWallet(userId, amount, orderId, description);
            return ResponseEntity.ok(Map.of("success", true, "transaction", transaction));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi thanh toán: " + e.getMessage()));
        }
    }
    
    /**
     * Get transaction history
     */
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int limit) {
        try {
            List<WalletTransaction> transactions;
            if (limit > 0) {
                transactions = walletService.getTransactionHistory(userId, limit);
            } else {
                transactions = walletService.getAllTransactions(userId);
            }
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy lịch sử giao dịch: " + e.getMessage()));
        }
    }
    
    /**
     * Complete deposit transaction (callback from payment gateway)
     */
    @PostMapping("/transactions/{transactionId}/complete")
    public ResponseEntity<?> completeDeposit(@PathVariable String transactionId) {
        try {
            WalletTransaction transaction = walletService.completeDeposit(transactionId);
            return ResponseEntity.ok(Map.of("success", true, "transaction", transaction));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi hoàn tất giao dịch: " + e.getMessage()));
        }
    }
}

