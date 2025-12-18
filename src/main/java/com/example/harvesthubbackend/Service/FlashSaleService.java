package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.FlashSale;
import com.example.harvesthubbackend.Repository.FlashSaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FlashSaleService {
    
    @Autowired
    private FlashSaleRepository flashSaleRepository;
    
    // Create a new flash sale
    public FlashSale createFlashSale(FlashSale flashSale) {
        flashSale.setCreatedAt(LocalDateTime.now());
        flashSale.setUpdatedAt(LocalDateTime.now());
        return flashSaleRepository.save(flashSale);
    }
    
    // Get flash sale by ID
    public Optional<FlashSale> getFlashSaleById(String id) {
        return flashSaleRepository.findById(id);
    }
    
    // Get all flash sales
    public List<FlashSale> getAllFlashSales() {
        return flashSaleRepository.findAll();
    }
    
    // Get active flash sales
    public List<FlashSale> getActiveFlashSales() {
        return flashSaleRepository.findActiveFlashSales(LocalDateTime.now());
    }
    
    // Get upcoming flash sales
    public List<FlashSale> getUpcomingFlashSales() {
        return flashSaleRepository.findUpcomingFlashSales(LocalDateTime.now());
    }
    
    // Get ended flash sales
    public List<FlashSale> getEndedFlashSales() {
        return flashSaleRepository.findEndedFlashSales(LocalDateTime.now());
    }
    
    // Get flash sales by status
    public List<FlashSale> getFlashSalesByStatus(String status) {
        return flashSaleRepository.findByStatus(status);
    }
    
    // Get flash sales starting soon
    public List<FlashSale> getFlashSalesStartingSoon(int hours) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(hours);
        return flashSaleRepository.findFlashSalesStartingSoon(start, end);
    }
    
    // Get flash sales ending soon
    public List<FlashSale> getFlashSalesEndingSoon(int hours) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(hours);
        return flashSaleRepository.findFlashSalesEndingSoon(start, end);
    }
    
    // Get flash sales by name (search)
    public List<FlashSale> searchFlashSalesByName(String name) {
        return flashSaleRepository.findByNameContainingIgnoreCase(name);
    }
    
    // Get flash sales by product
    public List<FlashSale> getFlashSalesByProduct(String productId) {
        return flashSaleRepository.findByProductId(productId);
    }
    
    // Update flash sale
    public FlashSale updateFlashSale(String id, FlashSale flashSaleDetails) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(id);
        if (optionalFlashSale.isPresent()) {
            FlashSale flashSale = optionalFlashSale.get();
            flashSale.setName(flashSaleDetails.getName());
            flashSale.setDescription(flashSaleDetails.getDescription());
            flashSale.setBanner(flashSaleDetails.getBanner());
            flashSale.setStartTime(flashSaleDetails.getStartTime());
            flashSale.setEndTime(flashSaleDetails.getEndTime());
            flashSale.setStatus(flashSaleDetails.getStatus());
            flashSale.setProducts(flashSaleDetails.getProducts());
            flashSale.setUpdatedAt(LocalDateTime.now());
            return flashSaleRepository.save(flashSale);
        }
        return null;
    }
    
    // Update flash sale status
    public FlashSale updateFlashSaleStatus(String id, String status) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(id);
        if (optionalFlashSale.isPresent()) {
            FlashSale flashSale = optionalFlashSale.get();
            flashSale.setStatus(status);
            flashSale.setUpdatedAt(LocalDateTime.now());
            return flashSaleRepository.save(flashSale);
        }
        return null;
    }
    
    // Update flash sale products
    public FlashSale updateFlashSaleProducts(String id, List<FlashSale.FlashSaleProduct> products) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(id);
        if (optionalFlashSale.isPresent()) {
            FlashSale flashSale = optionalFlashSale.get();
            flashSale.setProducts(products);
            flashSale.setUpdatedAt(LocalDateTime.now());
            return flashSaleRepository.save(flashSale);
        }
        return null;
    }
    
    // Update product sold count in flash sale
    public FlashSale updateProductSoldCount(String flashSaleId, String productId, int soldCount) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(flashSaleId);
        if (optionalFlashSale.isPresent()) {
            FlashSale flashSale = optionalFlashSale.get();
            if (flashSale.getProducts() != null) {
                for (FlashSale.FlashSaleProduct product : flashSale.getProducts()) {
                    if (product.getProductId().equals(productId)) {
                        product.setSoldCount(soldCount);
                        break;
                    }
                }
            }
            flashSale.setUpdatedAt(LocalDateTime.now());
            return flashSaleRepository.save(flashSale);
        }
        return null;
    }
    
    // Check if flash sale is active
    public boolean isFlashSaleActive(String id) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(id);
        if (optionalFlashSale.isPresent()) {
            return optionalFlashSale.get().isActive();
        }
        return false;
    }
    
    // Check if flash sale is upcoming
    public boolean isFlashSaleUpcoming(String id) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(id);
        if (optionalFlashSale.isPresent()) {
            return optionalFlashSale.get().isUpcoming();
        }
        return false;
    }
    
    // Check if flash sale is ended
    public boolean isFlashSaleEnded(String id) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(id);
        if (optionalFlashSale.isPresent()) {
            return optionalFlashSale.get().isEnded();
        }
        return false;
    }
    
    // Get remaining time for flash sale
    public long getRemainingTime(String id) {
        Optional<FlashSale> optionalFlashSale = flashSaleRepository.findById(id);
        if (optionalFlashSale.isPresent()) {
            return optionalFlashSale.get().getRemainingTimeInSeconds();
        }
        return 0;
    }
    
    // Delete flash sale
    public boolean deleteFlashSale(String id) {
        if (flashSaleRepository.existsById(id)) {
            flashSaleRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Check if flash sale exists
    public boolean flashSaleExists(String id) {
        return flashSaleRepository.existsById(id);
    }
    
    // Get flash sale count by status
    public long getFlashSaleCountByStatus(String status) {
        return flashSaleRepository.countByStatus(status);
    }
    
    // Get flash sales sorted by start time
    public List<FlashSale> getFlashSalesOrderByStartTime() {
        return flashSaleRepository.findAllOrderByStartTimeAsc();
    }
    
    // Get flash sales sorted by end time
    public List<FlashSale> getFlashSalesOrderByEndTime() {
        return flashSaleRepository.findAllOrderByEndTimeAsc();
    }
    
    // Get flash sales sorted by creation date
    public List<FlashSale> getFlashSalesOrderByCreatedAt() {
        return flashSaleRepository.findAllOrderByCreatedAtDesc();
    }
    
    // Get flash sale price for a product (if product is in an active flash sale)
    public Double getFlashSalePriceForProduct(String productId) {
        List<FlashSale> activeFlashSales = getActiveFlashSales();
        for (FlashSale flashSale : activeFlashSales) {
            if (flashSale.getProducts() != null) {
                for (FlashSale.FlashSaleProduct product : flashSale.getProducts()) {
                    if (product.getProductId().equals(productId)) {
                        // Check if there's remaining stock
                        if (product.getRemainingStock() > 0) {
                            return product.getFlashSalePrice();
                        }
                    }
                }
            }
        }
        return null; // No active flash sale for this product
    }
}
