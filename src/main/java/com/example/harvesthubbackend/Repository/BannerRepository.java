package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Banner;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends MongoRepository<Banner, String> {
    
    // Find banners by position
    List<Banner> findByPosition(String position);
    
    // Find active banners
    @Query("{'status': 'active', 'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}}")
    List<Banner> findActiveBanners(LocalDateTime now);
    
    // Find banners by position and status
    List<Banner> findByPositionAndStatus(String position, String status);
    
    // Find active banners by position
    @Query("{'position': ?0, 'status': 'active', 'startDate': {$lte: ?1}, 'endDate': {$gte: ?1}}")
    List<Banner> findActiveBannersByPosition(String position, LocalDateTime now);
    
    // Find banners by status
    List<Banner> findByStatus(String status);
    
    // Find banners by link type
    List<Banner> findByLinkType(String linkType);
    
    // Find banners by target ID
    List<Banner> findByTargetId(String targetId);
    
    // Find banners by title (case insensitive)
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Banner> findByTitleContainingIgnoreCase(String title);
    
    // Find banners sorted by priority
    @Query(value = "{}", sort = "{'priority': -1, 'createdAt': -1}")
    List<Banner> findAllOrderByPriorityAndCreatedAtDesc();
    
    // Find banners sorted by click count
    @Query(value = "{}", sort = "{'clickCount': -1}")
    List<Banner> findAllOrderByClickCountDesc();
    
    // Find banners sorted by view count
    @Query(value = "{}", sort = "{'viewCount': -1}")
    List<Banner> findAllOrderByViewCountDesc();
    
    // Find banners sorted by creation date
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<Banner> findAllOrderByCreatedAtDesc();
    
    // Find banners expiring soon
    @Query("{'status': 'active', 'endDate': {$gte: ?0, $lte: ?1}}")
    List<Banner> findBannersExpiringSoon(LocalDateTime start, LocalDateTime end);
    
    // Find banners starting soon
    @Query("{'status': 'active', 'startDate': {$gte: ?0, $lte: ?1}}")
    List<Banner> findBannersStartingSoon(LocalDateTime start, LocalDateTime end);
    
    // Count banners by status
    long countByStatus(String status);
    
    // Count banners by position
    long countByPosition(String position);
    
    // Find banners with minimum priority
    @Query("{'priority': {$gte: ?0}}")
    List<Banner> findByMinPriority(int minPriority);
}
