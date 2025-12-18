package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Seller;
import com.example.harvesthubbackend.Service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sellers")
@CrossOrigin(origins = "*")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    // Lấy danh sách tất cả seller registrations
    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers() {
        List<Seller> sellers = sellerService.getAllSellers();
        return ResponseEntity.ok(sellers);
    }

    // Lấy danh sách seller registrations chờ duyệt
    @GetMapping("/pending")
    public ResponseEntity<List<Seller>> getPendingSellers() {
        List<Seller> pendingSellers = sellerService.getPendingSellers();
        return ResponseEntity.ok(pendingSellers);
    }

    // Kiểm tra user có phải là seller không
    @GetMapping("/check/{userId}")
    public ResponseEntity<Map<String, Object>> checkSellerStatus(@PathVariable String userId) {
        try {
            Seller seller = sellerService.getSellerByUserId(userId);
            if (seller != null) {
                return ResponseEntity.ok(Map.of(
                    "isSeller", true,
                    "seller", seller,
                    "status", seller.getStatus()
                ));
            }
            return ResponseEntity.ok(Map.of(
                "isSeller", false
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "isSeller", false,
                "error", e.getMessage()
            ));
        }
    }

    // Lấy seller theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable String id) {
        Seller seller = sellerService.getSellerById(id);
        if (seller != null) {
            return ResponseEntity.ok(seller);
        }
        return ResponseEntity.notFound().build();
    }

    // Tạo seller registration mới (từ form đăng ký bán hàng)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSeller(@RequestBody Seller seller) {
        try {
            Seller createdSeller = sellerService.createSeller(seller);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đăng ký bán hàng đã được gửi thành công",
                "seller", createdSeller
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Duyệt seller registration
    @PutMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveSeller(@PathVariable String id, @RequestParam String adminId) {
        try {
            Seller approvedSeller = sellerService.approveSeller(id, adminId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã duyệt đăng ký bán hàng thành công",
                "seller", approvedSeller
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Từ chối seller registration
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectSeller(@PathVariable String id, @RequestParam String adminId, @RequestParam(required = false) String reason) {
        try {
            Seller rejectedSeller = sellerService.rejectSeller(id, adminId, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã từ chối đăng ký bán hàng",
                "seller", rejectedSeller
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Lấy thống kê seller registrations
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSellerStats() {
        Map<String, Object> stats = sellerService.getSellerStats();
        return ResponseEntity.ok(stats);
    }

    // Cập nhật seller
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSeller(@PathVariable String id, @RequestBody Seller seller) {
        try {
            Seller updatedSeller = sellerService.updateSeller(id, seller);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật thông tin thành công",
                "seller", updatedSeller
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    // Xóa seller
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSeller(@PathVariable String id) {
        try {
            sellerService.deleteSeller(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Xóa đăng ký thành công"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }
}
