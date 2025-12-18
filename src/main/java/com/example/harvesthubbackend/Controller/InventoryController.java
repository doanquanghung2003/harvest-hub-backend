package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Inventory;
import com.example.harvesthubbackend.Models.InventoryTransaction;
import com.example.harvesthubbackend.Service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    // Tạo inventory mới
    @PostMapping
    public ResponseEntity<?> createInventory(
            @RequestParam String productId,
            @RequestParam String sellerId,
            @RequestParam(defaultValue = "0") int initialStock) {
        try {
            Inventory inventory = inventoryService.createInventory(productId, sellerId, initialStock);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Nhập hàng
    @PostMapping("/{productId}/stock-in")
    public ResponseEntity<?> stockIn(
            @PathVariable String productId,
            @RequestParam int quantity,
            @RequestParam(defaultValue = "purchase") String reason,
            @RequestParam(required = false) String notes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String createdBy = auth != null ? auth.getName() : "system";
            
            Inventory inventory = inventoryService.stockIn(productId, quantity, reason, notes, createdBy);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Xuất hàng
    @PostMapping("/{productId}/stock-out")
    public ResponseEntity<?> stockOut(
            @PathVariable String productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String orderId,
            @RequestParam(defaultValue = "sale") String reason) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String createdBy = auth != null ? auth.getName() : "system";
            
            Inventory inventory = inventoryService.stockOut(productId, quantity, orderId, reason, createdBy);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Reserve stock
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<?> reserveStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        try {
            Inventory inventory = inventoryService.reserveStock(productId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Release reserved stock
    @PostMapping("/{productId}/release")
    public ResponseEntity<?> releaseReservedStock(
            @PathVariable String productId,
            @RequestParam int quantity) {
        try {
            Inventory inventory = inventoryService.releaseReservedStock(productId, quantity);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Điều chỉnh stock
    @PostMapping("/{productId}/adjust")
    public ResponseEntity<?> adjustStock(
            @PathVariable String productId,
            @RequestParam int newQuantity,
            @RequestParam(defaultValue = "adjustment") String reason,
            @RequestParam(required = false) String notes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String createdBy = auth != null ? auth.getName() : "system";
            
            Inventory inventory = inventoryService.adjustStock(productId, newQuantity, reason, notes, createdBy);
            return ResponseEntity.ok(inventory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy inventory theo product ID
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getByProductId(@PathVariable String productId) {
        Optional<Inventory> inventory = inventoryService.getByProductId(productId);
        if (inventory.isPresent()) {
            return ResponseEntity.ok(inventory.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Lấy inventory theo seller ID
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Inventory>> getBySellerId(@PathVariable String sellerId) {
        return ResponseEntity.ok(inventoryService.getBySellerId(sellerId));
    }
    
    // Lấy inventory theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Inventory>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(inventoryService.getByStatus(status));
    }
    
    // Lấy danh sách sản phẩm sắp hết hàng
    @GetMapping("/low-stock")
    public ResponseEntity<List<Inventory>> getLowStockItems() {
        return ResponseEntity.ok(inventoryService.getLowStockItems());
    }
    
    // Lấy tất cả inventory
    @GetMapping
    public ResponseEntity<List<Inventory>> getAll() {
        return ResponseEntity.ok(inventoryService.getAll());
    }
    
    // Lấy inventory theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        Optional<Inventory> inventory = inventoryService.getById(id);
        if (inventory.isPresent()) {
            return ResponseEntity.ok(inventory.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Cập nhật inventory
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventory(@PathVariable String id, @RequestBody Inventory inventory) {
        try {
            Inventory updated = inventoryService.update(id, inventory);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Xóa inventory
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInventory(@PathVariable String id) {
        try {
            inventoryService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Inventory deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy lịch sử giao dịch
    @GetMapping("/{inventoryId}/transactions")
    public ResponseEntity<List<InventoryTransaction>> getTransactionHistory(@PathVariable String inventoryId) {
        return ResponseEntity.ok(inventoryService.getTransactionHistory(inventoryId));
    }
    
    // Lấy lịch sử giao dịch theo product
    @GetMapping("/product/{productId}/transactions")
    public ResponseEntity<List<InventoryTransaction>> getTransactionHistoryByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getTransactionHistoryByProduct(productId));
    }
}

