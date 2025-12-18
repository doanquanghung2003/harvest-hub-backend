package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Voucher;
import com.example.harvesthubbackend.Models.UserVoucher;
import com.example.harvesthubbackend.Service.VoucherService;
import com.example.harvesthubbackend.Service.VoucherStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vouchers")
@CrossOrigin(origins = "*")
public class AdminVoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VoucherStatisticsService statisticsService;
    
    // Create platform voucher
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@RequestBody Voucher voucher) {
        // Set as platform voucher (shopId = null)
        voucher.setShopId(null);
        Voucher created = voucherService.createVoucher(voucher);
        return ResponseEntity.ok(created);
    }
    
    // Get all vouchers (platform + shop vouchers)
    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    // Get platform vouchers only
    @GetMapping("/platform")
    public ResponseEntity<List<Voucher>> getPlatformVouchers() {
        List<Voucher> vouchers = voucherService.getPlatformVouchers();
        return ResponseEntity.ok(vouchers);
    }
    
    // Get voucher by ID
    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getVoucherById(@PathVariable String id) {
        return voucherService.getVoucherById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Update voucher
    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(
            @PathVariable String id,
            @RequestBody Voucher voucherDetails) {
        Voucher updated = voucherService.updateVoucher(id, voucherDetails);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Delete voucher
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable String id) {
        boolean deleted = voucherService.deleteVoucher(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update voucher status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Voucher> updateVoucherStatus(
            @PathVariable String id,
            @RequestParam String status) {
        Voucher updated = voucherService.updateVoucherStatus(id, status);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Grant voucher to user
    @PostMapping("/grant")
    public ResponseEntity<UserVoucher> grantVoucherToUser(
            @RequestBody GrantVoucherRequest request) {
        try {
            UserVoucher userVoucher = voucherService.grantVoucherToUser(
                request.getUserId(), 
                request.getVoucherId()
            );
            return ResponseEntity.ok(userVoucher);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Get voucher statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getVoucherStatistics() {
        Map<String, Object> stats = statisticsService.getVoucherStatistics();
        return ResponseEntity.ok(stats);
    }
    
    // Get vouchers expiring soon
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<Voucher>> getVouchersExpiringSoon(
            @RequestParam(defaultValue = "7") int days) {
        List<Voucher> vouchers = voucherService.getVouchersExpiringSoon(days);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get vouchers near usage limit
    @GetMapping("/near-limit")
    public ResponseEntity<List<Voucher>> getVouchersNearUsageLimit(
            @RequestParam(defaultValue = "10") int threshold) {
        List<Voucher> vouchers = voucherService.getVouchersNearUsageLimit(threshold);
        return ResponseEntity.ok(vouchers);
    }
    
    // Request class
    public static class GrantVoucherRequest {
        private String userId;
        private String voucherId;
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getVoucherId() { return voucherId; }
        public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    }
}

