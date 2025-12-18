package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Shipping;
import com.example.harvesthubbackend.Service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/shippings")
@CrossOrigin(origins = "*")
public class ShippingController {
    
    @Autowired
    private ShippingService shippingService;
    
    // Tạo shipping mới
    @PostMapping
    public ResponseEntity<?> createShipping(@RequestParam String orderId, @RequestBody Shipping shipping) {
        try {
            Shipping created = shippingService.createShipping(orderId, shipping);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy tất cả shipping
    @GetMapping
    public ResponseEntity<List<Shipping>> getAllShippings() {
        return ResponseEntity.ok(shippingService.getAll());
    }
    
    // Lấy shipping theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getShippingById(@PathVariable String id) {
        Optional<Shipping> shipping = shippingService.getById(id);
        if (shipping.isPresent()) {
            return ResponseEntity.ok(shipping.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Lấy shipping theo order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Shipping>> getShippingByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(shippingService.getShippingByOrderId(orderId));
    }
    
    // Lấy shipping theo user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Shipping>> getShippingByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(shippingService.getShippingByUserId(userId));
    }
    
    // Lấy shipping theo seller ID
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Shipping>> getShippingBySellerId(@PathVariable String sellerId) {
        return ResponseEntity.ok(shippingService.getShippingBySellerId(sellerId));
    }
    
    // Lấy shipping theo tracking number
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> getShippingByTrackingNumber(@PathVariable String trackingNumber) {
        Optional<Shipping> shipping = shippingService.getShippingByTrackingNumber(trackingNumber);
        if (shipping.isPresent()) {
            return ResponseEntity.ok(shipping.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Tính phí vận chuyển
    @PostMapping("/calculate-fee")
    public ResponseEntity<?> calculateShippingFee(
            @RequestParam String city,
            @RequestParam String district,
            @RequestParam double weight,
            @RequestParam(defaultValue = "standard") String shippingMethod) {
        try {
            double fee = shippingService.calculateShippingFee(city, district, weight, shippingMethod);
            return ResponseEntity.ok(Map.of(
                "shippingFee", fee,
                "currency", "VND"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Cập nhật trạng thái vận chuyển
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateShippingStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String description) {
        try {
            Shipping updated = shippingService.updateShippingStatus(
                id, 
                status, 
                location != null ? location : "", 
                description != null ? description : ""
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Thêm tracking event
    @PostMapping("/{id}/tracking")
    public ResponseEntity<?> addTrackingEvent(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String location) {
        try {
            Shipping updated = shippingService.addTrackingEvent(
                id, 
                status, 
                description != null ? description : "", 
                location != null ? location : ""
            );
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Cập nhật shipping
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShipping(@PathVariable String id, @RequestBody Shipping shipping) {
        try {
            Shipping updated = shippingService.update(id, shipping);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Xóa shipping
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShipping(@PathVariable String id) {
        try {
            shippingService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Shipping deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    // Lấy shipping theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Shipping>> getShippingByStatus(@PathVariable String status) {
        return ResponseEntity.ok(shippingService.getByStatus(status));
    }
}

