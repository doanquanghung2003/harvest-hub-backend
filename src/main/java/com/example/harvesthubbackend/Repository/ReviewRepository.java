package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    
    // Find reviews by product ID
    List<Review> findByProductId(String productId);
    
    // Find reviews by user ID
    List<Review> findByUserId(String userId);
    
    // Find reviews by order ID
    List<Review> findByOrderId(String orderId);
    
    // Find reviews by status
    List<Review> findByStatus(String status);
    
    // Find reviews by rating
    List<Review> findByRating(int rating);
    
    // Find reviews by product ID and status
    List<Review> findByProductIdAndStatus(String productId, String status);
    
    // Find reviews by user ID and status
    List<Review> findByUserIdAndStatus(String userId, String status);
    
    // Find reviews by product ID and rating
    List<Review> findByProductIdAndRating(String productId, int rating);
    
    // Find reviews by user ID and product ID
    Optional<Review> findByUserIdAndProductId(String userId, String productId);
    
    // Find reviews by user ID and order ID
    Optional<Review> findByUserIdAndOrderId(String userId, String orderId);
    
    // Find reviews by product ID and verified status
    List<Review> findByProductIdAndIsVerified(String productId, boolean isVerified);
    
    // Find reviews by product ID and helpful status
    List<Review> findByProductIdAndIsHelpful(String productId, boolean isHelpful);
    
    // Find reviews with images by product ID
    @Query("{'productId': ?0, 'images': {$exists: true, $ne: []}}")
    List<Review> findByProductIdAndImagesExists(String productId);
    
    // Find reviews with videos by product ID
    @Query("{'productId': ?0, 'videos': {$exists: true, $ne: []}}")
    List<Review> findByProductIdAndVideosExists(String productId);
    
    // Find reviews with replies by product ID
    @Query("{'productId': ?0, 'reply': {$exists: true}}")
    List<Review> findByProductIdAndReplyExists(String productId);
    
    // Find reviews by rating range
    @Query("{'rating': {$gte: ?0, $lte: ?1}}")
    List<Review> findByRatingBetween(int minRating, int maxRating);
    
    // Find reviews by product ID and rating range
    @Query("{'productId': ?0, 'rating': {$gte: ?1, $lte: ?2}}")
    List<Review> findByProductIdAndRatingBetween(String productId, int minRating, int maxRating);
    
    // Find reviews by user ID and rating
    List<Review> findByUserIdAndRating(String userId, int rating);
    
    // Find reviews by user ID and rating range
    @Query("{'userId': ?0, 'rating': {$gte: ?1, $lte: ?2}}")
    List<Review> findByUserIdAndRatingBetween(String userId, int minRating, int maxRating);
    
    // Find reviews by helpful count
    @Query("{'helpfulCount': {$gte: ?0}}")
    List<Review> findByHelpfulCountGreaterThanEqual(int minHelpfulCount);
    
    // Find reviews by product ID and helpful count
    @Query("{'productId': ?0, 'helpfulCount': {$gte: ?1}}")
    List<Review> findByProductIdAndHelpfulCountGreaterThanEqual(String productId, int minHelpfulCount);
    
    // Find reviews created between dates
    @Query("{'createdAt': {$gte: ?0, $lte: ?1}}")
    List<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find reviews by product ID and creation date range
    @Query("{'productId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}")
    List<Review> findByProductIdAndCreatedAtBetween(String productId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find reviews by user ID and creation date range
    @Query("{'userId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}")
    List<Review> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find reviews sorted by creation date
    @Query(value = "{}", sort = "{'createdAt': -1}")
    List<Review> findAllOrderByCreatedAtDesc();
    
    // Find reviews by product ID sorted by creation date
    @Query(value = "{'productId': ?0}", sort = "{'createdAt': -1}")
    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);
    
    // Find reviews by user ID sorted by creation date
    @Query(value = "{'userId': ?0}", sort = "{'createdAt': -1}")
    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);
    
    // Find reviews sorted by helpful count
    @Query(value = "{}", sort = "{'helpfulCount': -1}")
    List<Review> findAllOrderByHelpfulCountDesc();
    
    // Find reviews by product ID sorted by helpful count
    @Query(value = "{'productId': ?0}", sort = "{'helpfulCount': -1}")
    List<Review> findByProductIdOrderByHelpfulCountDesc(String productId);
    
    // Find reviews sorted by rating
    @Query(value = "{}", sort = "{'rating': -1}")
    List<Review> findAllOrderByRatingDesc();
    
    // Find reviews by product ID sorted by rating
    @Query(value = "{'productId': ?0}", sort = "{'rating': -1}")
    List<Review> findByProductIdOrderByRatingDesc(String productId);
    
    // Count reviews by status
    long countByStatus(String status);
    
    // Count reviews by product ID
    long countByProductId(String productId);
    
    // Count reviews by user ID
    long countByUserId(String userId);
    
    // Count reviews by rating
    long countByRating(int rating);
    
    // Count reviews by product ID and status
    long countByProductIdAndStatus(String productId, String status);
    
    // Count reviews by user ID and status
    long countByUserIdAndStatus(String userId, String status);
    
    // Count reviews by product ID and rating
    long countByProductIdAndRating(String productId, int rating);
    
    // Count reviews by user ID and rating
    long countByUserIdAndRating(String userId, int rating);
    
    // Count reviews by product ID and verified status
    long countByProductIdAndIsVerified(String productId, boolean isVerified);
    
    // Count reviews by product ID and helpful status
    long countByProductIdAndIsHelpful(String productId, boolean isHelpful);
    
    // Count reviews by helpful count
    @Query(value = "{'helpfulCount': {$gte: ?0}}", count = true)
    long countByHelpfulCountGreaterThanEqual(int minHelpfulCount);
    
    // Count reviews by product ID and helpful count
    @Query(value = "{'productId': ?0, 'helpfulCount': {$gte: ?1}}", count = true)
    long countByProductIdAndHelpfulCountGreaterThanEqual(String productId, int minHelpfulCount);
    
    // Count reviews created between dates
    @Query(value = "{'createdAt': {$gte: ?0, $lte: ?1}}", count = true)
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Count reviews by product ID and creation date range
    @Query(value = "{'productId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}", count = true)
    long countByProductIdAndCreatedAtBetween(String productId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Count reviews by user ID and creation date range
    @Query(value = "{'userId': ?0, 'createdAt': {$gte: ?1, $lte: ?2}}", count = true)
    long countByUserIdAndCreatedAtBetween(String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Sum helpful count by product ID
    @Query(value = "{'productId': ?0}", fields = "{'helpfulCount': 1}")
    List<Review> findByProductIdForSum(String productId);
    
    // Sum helpful count by user ID
    @Query(value = "{'userId': ?0}", fields = "{'helpfulCount': 1}")
    List<Review> findByUserIdForSum(String userId);
}
