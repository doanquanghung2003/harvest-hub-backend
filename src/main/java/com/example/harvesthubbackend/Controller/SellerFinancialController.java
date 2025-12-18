package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.SellerFinancial;
import com.example.harvesthubbackend.Models.WithdrawalRequest;
import com.example.harvesthubbackend.Service.SellerFinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/seller-financial")
@CrossOrigin(origins = "*")
public class SellerFinancialController {
    
    @Autowired
    private SellerFinancialService financialService;
    
    // Lấy financial theo seller ID
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<?> getBySellerId(@PathVariable String sellerId) {
        Optional<SellerFinancial> financial = financialService.getBySellerId(sellerId);
        if (financial.isPresent()) {
            return ResponseEntity.ok(financial.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Lấy financial theo user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUserId(@PathVariable String userId) {
        Optional<SellerFinancial> financial = financialService.getByUserId(userId);
        if (financial.isPresent()) {
            return ResponseEntity.ok(financial.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Lấy tất cả financial (Admin only)
    @GetMapping
    public ResponseEntity<List<SellerFinancial>> getAll() {
        return ResponseEntity.ok(financialService.getAll());
    }
    
    // Cập nhật financial
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFinancial(@PathVariable String id, @RequestBody SellerFinancial financial) {
        try {
            SellerFinancial updated = financialService.update(id, financial);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Yêu cầu rút tiền
    @PostMapping("/{sellerId}/withdraw")
    public ResponseEntity<?> requestWithdrawal(
            @PathVariable String sellerId,
            @RequestParam double amount,
            @RequestParam String bankName,
            @RequestParam String bankAccountNumber,
            @RequestParam String bankAccountName,
            @RequestParam(required = false) String bankBranch) {
        try {
            WithdrawalRequest request = financialService.requestWithdrawal(
                sellerId, amount, bankName, bankAccountNumber, bankAccountName, bankBranch);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy yêu cầu rút tiền theo seller
    @GetMapping("/{sellerId}/withdrawals")
    public ResponseEntity<List<WithdrawalRequest>> getWithdrawalRequests(@PathVariable String sellerId) {
        return ResponseEntity.ok(financialService.getWithdrawalRequestsBySeller(sellerId));
    }
    
    // Xử lý yêu cầu rút tiền (Admin)
    @PutMapping("/withdrawals/{requestId}/process")
    public ResponseEntity<?> processWithdrawal(
            @PathVariable String requestId,
            @RequestParam String status,
            @RequestParam(required = false) String transactionReference,
            @RequestParam(required = false) String rejectionReason) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminId = auth != null ? auth.getName() : "admin";
            
            WithdrawalRequest request = financialService.processWithdrawal(
                requestId, status, adminId, transactionReference, rejectionReason);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy yêu cầu rút tiền theo trạng thái (Admin)
    @GetMapping("/withdrawals/status/{status}")
    public ResponseEntity<List<WithdrawalRequest>> getWithdrawalRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(financialService.getWithdrawalRequestsByStatus(status));
    }
    
    // Lấy tất cả yêu cầu rút tiền (Admin)
    @GetMapping("/withdrawals")
    public ResponseEntity<List<WithdrawalRequest>> getAllWithdrawalRequests() {
        return ResponseEntity.ok(financialService.getAllWithdrawalRequests());
    }
}

