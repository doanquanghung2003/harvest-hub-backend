package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Shop;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends MongoRepository<Shop, String> {
    
    // Find shop by owner ID
    Optional<Shop> findByOwnerId(String ownerId);
    
    // Find shops by status
    List<Shop> findByStatus(String status);
    
    // Find shops by name (case insensitive)
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Shop> findByNameContainingIgnoreCase(String name);
    
    // Find shops by city
    @Query("{'address.city': {$regex: ?0, $options: 'i'}}")
    List<Shop> findByCity(String city);
    
    // Find shops by business category
    @Query("{'businessInfo.businessCategory': {$regex: ?0, $options: 'i'}}")
    List<Shop> findByBusinessCategory(String businessCategory);
    
    // Find shops with minimum rating
    @Query("{'stats.averageRating': {$gte: ?0}}")
    List<Shop> findByMinRating(double minRating);
    
    // Find shops with minimum followers
    @Query("{'stats.followers': {$gte: ?0}}")
    List<Shop> findByMinFollowers(int minFollowers);
    
    // Find shops by multiple criteria
    @Query("{'status': ?0, 'stats.averageRating': {$gte: ?1}, 'stats.followers': {$gte: ?2}}")
    List<Shop> findByStatusAndMinRatingAndMinFollowers(String status, double minRating, int minFollowers);
    
    // Find shops by owner ID and status
    List<Shop> findByOwnerIdAndStatus(String ownerId, String status);
    
    // Count shops by status
    long countByStatus(String status);
    
    // Find shops sorted by rating
    @Query(value = "{}", sort = "{'stats.averageRating': -1}")
    List<Shop> findAllOrderByRatingDesc();
    
    // Find shops sorted by followers
    @Query(value = "{}", sort = "{'stats.followers': -1}")
    List<Shop> findAllOrderByFollowersDesc();
    
    // Find shops sorted by total revenue
    @Query(value = "{}", sort = "{'stats.totalRevenue': -1}")
    List<Shop> findAllOrderByRevenueDesc();
}
