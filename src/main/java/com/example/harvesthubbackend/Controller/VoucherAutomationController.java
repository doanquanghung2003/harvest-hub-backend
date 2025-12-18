package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Service.VoucherAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin/vouchers/automation")
@CrossOrigin(origins = "*")
public class VoucherAutomationController {
    
    @Autowired
    private VoucherAutomationService automationService;
    
    // Manually trigger welcome voucher for user
    @PostMapping("/welcome/{userId}")
    public ResponseEntity<Map<String, String>> grantWelcomeVoucher(@PathVariable String userId) {
        try {
            automationService.grantWelcomeVoucher(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Welcome voucher granted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Manually trigger birthday vouchers
    @PostMapping("/birthday")
    public ResponseEntity<Map<String, String>> grantBirthdayVouchers() {
        try {
            automationService.grantBirthdayVouchers();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Birthday vouchers granted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Manually trigger purchase reward voucher
    @PostMapping("/purchase-reward")
    public ResponseEntity<Map<String, String>> grantPurchaseRewardVoucher(
            @RequestParam String userId,
            @RequestParam String orderId) {
        try {
            automationService.grantPurchaseRewardVoucher(userId, orderId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Purchase reward voucher granted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Manually trigger referral voucher
    @PostMapping("/referral")
    public ResponseEntity<Map<String, String>> grantReferralVoucher(
            @RequestParam String referrerUserId,
            @RequestParam String referredUserId) {
        try {
            automationService.grantReferralVoucher(referrerUserId, referredUserId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Referral voucher granted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

