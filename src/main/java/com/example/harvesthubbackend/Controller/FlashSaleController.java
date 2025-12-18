package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.FlashSale;
import com.example.harvesthubbackend.Service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flashsales")
@CrossOrigin(origins = "*")
public class FlashSaleController {
    
    @Autowired
    private FlashSaleService flashSaleService;
    
    // Create a new flash sale
    @PostMapping
    public ResponseEntity<FlashSale> createFlashSale(@RequestBody FlashSale flashSale) {
        FlashSale createdFlashSale = flashSaleService.createFlashSale(flashSale);
        return ResponseEntity.ok(createdFlashSale);
    }
    
    // Get flash sale by ID
    @GetMapping("/{id}")
    public ResponseEntity<FlashSale> getFlashSaleById(@PathVariable String id) {
        Optional<FlashSale> flashSale = flashSaleService.getFlashSaleById(id);
        return flashSale.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    // Get all flash sales
    @GetMapping
    public ResponseEntity<List<FlashSale>> getAllFlashSales() {
        List<FlashSale> flashSales = flashSaleService.getAllFlashSales();
        return ResponseEntity.ok(flashSales);
    }
    
    // Get active flash sales
    @GetMapping("/active")
    public ResponseEntity<List<FlashSale>> getActiveFlashSales() {
        List<FlashSale> flashSales = flashSaleService.getActiveFlashSales();
        return ResponseEntity.ok(flashSales);
    }
    
    // Get upcoming flash sales
    @GetMapping("/upcoming")
    public ResponseEntity<List<FlashSale>> getUpcomingFlashSales() {
        List<FlashSale> flashSales = flashSaleService.getUpcomingFlashSales();
        return ResponseEntity.ok(flashSales);
    }
    
    // Get ended flash sales
    @GetMapping("/ended")
    public ResponseEntity<List<FlashSale>> getEndedFlashSales() {
        List<FlashSale> flashSales = flashSaleService.getEndedFlashSales();
        return ResponseEntity.ok(flashSales);
    }
    
    // Get flash sales by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<FlashSale>> getFlashSalesByStatus(@PathVariable String status) {
        List<FlashSale> flashSales = flashSaleService.getFlashSalesByStatus(status);
        return ResponseEntity.ok(flashSales);
    }
    
    // Get flash sales starting soon
    @GetMapping("/starting-soon")
    public ResponseEntity<List<FlashSale>> getFlashSalesStartingSoon(@RequestParam int hours) {
        List<FlashSale> flashSales = flashSaleService.getFlashSalesStartingSoon(hours);
        return ResponseEntity.ok(flashSales);
    }
    
    // Get flash sales ending soon
    @GetMapping("/ending-soon")
    public ResponseEntity<List<FlashSale>> getFlashSalesEndingSoon(@RequestParam int hours) {
        List<FlashSale> flashSales = flashSaleService.getFlashSalesEndingSoon(hours);
        return ResponseEntity.ok(flashSales);
    }
    
    // Search flash sales by name
    @GetMapping("/search")
    public ResponseEntity<List<FlashSale>> searchFlashSalesByName(@RequestParam String name) {
        List<FlashSale> flashSales = flashSaleService.searchFlashSalesByName(name);
        return ResponseEntity.ok(flashSales);
    }
    
    // Get flash sales by product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<FlashSale>> getFlashSalesByProduct(@PathVariable String productId) {
        List<FlashSale> flashSales = flashSaleService.getFlashSalesByProduct(productId);
        return ResponseEntity.ok(flashSales);
    }
    
    // Update flash sale
    @PutMapping("/{id}")
    public ResponseEntity<FlashSale> updateFlashSale(@PathVariable String id, @RequestBody FlashSale flashSaleDetails) {
        FlashSale updatedFlashSale = flashSaleService.updateFlashSale(id, flashSaleDetails);
        if (updatedFlashSale != null) {
            return ResponseEntity.ok(updatedFlashSale);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update flash sale status
    @PatchMapping("/{id}/status")
    public ResponseEntity<FlashSale> updateFlashSaleStatus(@PathVariable String id, @RequestParam String status) {
        FlashSale updatedFlashSale = flashSaleService.updateFlashSaleStatus(id, status);
        if (updatedFlashSale != null) {
            return ResponseEntity.ok(updatedFlashSale);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update flash sale products
    @PutMapping("/{id}/products")
    public ResponseEntity<FlashSale> updateFlashSaleProducts(
            @PathVariable String id, 
            @RequestBody List<FlashSale.FlashSaleProduct> products) {
        FlashSale updatedFlashSale = flashSaleService.updateFlashSaleProducts(id, products);
        if (updatedFlashSale != null) {
            return ResponseEntity.ok(updatedFlashSale);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update product sold count in flash sale
    @PatchMapping("/{id}/products/{productId}/sold")
    public ResponseEntity<FlashSale> updateProductSoldCount(
            @PathVariable String id, 
            @PathVariable String productId, 
            @RequestParam int soldCount) {
        FlashSale updatedFlashSale = flashSaleService.updateProductSoldCount(id, productId, soldCount);
        if (updatedFlashSale != null) {
            return ResponseEntity.ok(updatedFlashSale);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if flash sale is active
    @GetMapping("/{id}/active")
    public ResponseEntity<Boolean> isFlashSaleActive(@PathVariable String id) {
        boolean isActive = flashSaleService.isFlashSaleActive(id);
        return ResponseEntity.ok(isActive);
    }
    
    // Check if flash sale is upcoming
    @GetMapping("/{id}/upcoming")
    public ResponseEntity<Boolean> isFlashSaleUpcoming(@PathVariable String id) {
        boolean isUpcoming = flashSaleService.isFlashSaleUpcoming(id);
        return ResponseEntity.ok(isUpcoming);
    }
    
    // Check if flash sale is ended
    @GetMapping("/{id}/ended")
    public ResponseEntity<Boolean> isFlashSaleEnded(@PathVariable String id) {
        boolean isEnded = flashSaleService.isFlashSaleEnded(id);
        return ResponseEntity.ok(isEnded);
    }
    
    // Get remaining time for flash sale
    @GetMapping("/{id}/remaining-time")
    public ResponseEntity<Long> getRemainingTime(@PathVariable String id) {
        long remainingTime = flashSaleService.getRemainingTime(id);
        return ResponseEntity.ok(remainingTime);
    }
    
    // Delete flash sale
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable String id) {
        boolean deleted = flashSaleService.deleteFlashSale(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if flash sale exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> flashSaleExists(@PathVariable String id) {
        boolean exists = flashSaleService.flashSaleExists(id);
        return ResponseEntity.ok(exists);
    }
    
    // Get flash sale count by status
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getFlashSaleCountByStatus(@PathVariable String status) {
        long count = flashSaleService.getFlashSaleCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    // Get flash sales sorted by start time
    @GetMapping("/sorted/start-time")
    public ResponseEntity<List<FlashSale>> getFlashSalesOrderByStartTime() {
        List<FlashSale> flashSales = flashSaleService.getFlashSalesOrderByStartTime();
        return ResponseEntity.ok(flashSales);
    }
    
    // Get flash sales sorted by end time
    @GetMapping("/sorted/end-time")
    public ResponseEntity<List<FlashSale>> getFlashSalesOrderByEndTime() {
        List<FlashSale> flashSales = flashSaleService.getFlashSalesOrderByEndTime();
        return ResponseEntity.ok(flashSales);
    }
    
    // Get flash sales sorted by creation date
    @GetMapping("/sorted/created-at")
    public ResponseEntity<List<FlashSale>> getFlashSalesOrderByCreatedAt() {
        List<FlashSale> flashSales = flashSaleService.getFlashSalesOrderByCreatedAt();
        return ResponseEntity.ok(flashSales);
    }
}
