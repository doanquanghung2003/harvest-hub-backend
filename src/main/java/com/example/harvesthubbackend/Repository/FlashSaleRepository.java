package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.FlashSale;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FlashSaleRepository extends MongoRepository<FlashSale, String> {
    
    // Find flash sales by status
    List<FlashSale> findByStatus(String status);
    
    // Find active flash sales
    @Query("{'status': 'active', 'startTime': {$lte: ?0}, 'endTime': {$gte: ?0}}")
    List<FlashSale> findActiveFlashSales(LocalDateTime now);
    
    // Find upcoming flash sales
    @Query("{'status': 'upcoming', 'startTime': {$gte: ?0}}")
    List<FlashSale> findUpcomingFlashSales(LocalDateTime now);
    
    // Find ended flash sales
    @Query("{'$or': [{'status': 'ended'}, {'endTime': {$lt: ?0}}]}")
    List<FlashSale> findEndedFlashSales(LocalDateTime now);
    
    // Find flash sales starting soon
    @Query("{'status': 'upcoming', 'startTime': {$gte: ?0, $lte: ?1}}")
    List<FlashSale> findFlashSalesStartingSoon(LocalDateTime start, LocalDateTime end);
    
    // Find flash sales ending soon
    @Query("{'status': 'active', 'endTime': {$gte: ?0, $lte: ?1}}")
    List<FlashSale> findFlashSalesEndingSoon(LocalDateTime start, LocalDateTime end);
    
    // Find flash sales by name (case insensitive)
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<FlashSale> findByNameContainingIgnoreCase(String name);
    
    // Find flash sales sorted by start time
    @Query(value = "{}", sort = "{'startTime': 1}")
    List<FlashSale> findAllOrderByStartTimeAsc();
    
    // Find flash sales sorted by end time
    @Query(value = "{}", sort = "{'endTime': 1}")
    List<FlashSale> findAllOrderByEndTimeAsc();
    
    // Find flash sales sorted by creation date
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<FlashSale> findAllOrderByCreatedAtDesc();
    
    // Count flash sales by status
    long countByStatus(String status);
    
    // Find flash sales with specific product
    @Query("{'products.productId': ?0}")
    List<FlashSale> findByProductId(String productId);
}
