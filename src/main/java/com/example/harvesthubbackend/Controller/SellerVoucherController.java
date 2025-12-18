package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Voucher;
import com.example.harvesthubbackend.Service.VoucherService;
import com.example.harvesthubbackend.Service.VoucherStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/vouchers")
@CrossOrigin(origins = "*")
public class SellerVoucherController {
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VoucherStatisticsService statisticsService;
    
    // Create shop voucher
    @PostMapping
    public ResponseEntity<Voucher> createVoucher(
            @RequestBody Voucher voucher,
            @RequestParam String shopId) {
        // Set shop ID for shop voucher
        voucher.setShopId(shopId);
        Voucher created = voucherService.createVoucher(voucher);
        return ResponseEntity.ok(created);
    }
    
    // Get all vouchers for shop
    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<Voucher>> getShopVouchers(@PathVariable String shopId) {
        List<Voucher> vouchers = voucherService.getVouchersByShop(shopId);
        return ResponseEntity.ok(vouchers);
    }
    
    // Get voucher by ID (must belong to shop)
    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getVoucherById(
            @PathVariable String id,
            @RequestParam String shopId) {
        java.util.Optional<Voucher> voucherOpt = voucherService.getVoucherById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            // Verify voucher belongs to shop
            if (voucher.getShopId() != null && voucher.getShopId().equals(shopId)) {
                return ResponseEntity.ok(voucher);
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update voucher (must belong to shop)
    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(
            @PathVariable String id,
            @RequestParam String shopId,
            @RequestBody Voucher voucherDetails) {
        java.util.Optional<Voucher> voucherOpt = voucherService.getVoucherById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            // Verify voucher belongs to shop
            if (voucher.getShopId() != null && voucher.getShopId().equals(shopId)) {
                // Ensure shopId is not changed
                voucherDetails.setShopId(shopId);
                Voucher updated = voucherService.updateVoucher(id, voucherDetails);
                if (updated != null) {
                    return ResponseEntity.ok(updated);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    // Delete voucher (must belong to shop)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(
            @PathVariable String id,
            @RequestParam String shopId) {
        java.util.Optional<Voucher> voucherOpt = voucherService.getVoucherById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            // Verify voucher belongs to shop
            if (voucher.getShopId() != null && voucher.getShopId().equals(shopId)) {
                boolean deleted = voucherService.deleteVoucher(id);
                if (deleted) {
                    return ResponseEntity.noContent().build();
                }
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update voucher status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Voucher> updateVoucherStatus(
            @PathVariable String id,
            @RequestParam String shopId,
            @RequestParam String status) {
        java.util.Optional<Voucher> voucherOpt = voucherService.getVoucherById(id);
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            // Verify voucher belongs to shop
            if (voucher.getShopId() != null && voucher.getShopId().equals(shopId)) {
                Voucher updated = voucherService.updateVoucherStatus(id, status);
                if (updated != null) {
                    return ResponseEntity.ok(updated);
                }
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    // Get shop voucher statistics
    @GetMapping("/shop/{shopId}/statistics")
    public ResponseEntity<Map<String, Object>> getShopVoucherStatistics(@PathVariable String shopId) {
        Map<String, Object> stats = statisticsService.getShopVoucherStatistics(shopId);
        return ResponseEntity.ok(stats);
    }
    
    // Get vouchers expiring soon for shop
    @GetMapping("/shop/{shopId}/expiring-soon")
    public ResponseEntity<List<Voucher>> getShopVouchersExpiringSoon(
            @PathVariable String shopId,
            @RequestParam(defaultValue = "7") int days) {
        List<Voucher> expiringSoon = voucherService.getVouchersExpiringSoon(days);
        
        // Filter to only shop vouchers
        List<Voucher> result = expiringSoon.stream()
            .filter(v -> shopId.equals(v.getShopId()))
            .toList();
        
        return ResponseEntity.ok(result);
    }
}

