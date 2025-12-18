package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Review;
import com.example.harvesthubbackend.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    // Create a new review
    public Review createReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }
    
    // Get review by ID
    public Optional<Review> getReviewById(String id) {
        return reviewRepository.findById(id);
    }
    
    // Get reviews by product ID
    public List<Review> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId);
    }
    
    // Get reviews by user ID
    public List<Review> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }
    
    // Get reviews by order ID
    public List<Review> getReviewsByOrderId(String orderId) {
        return reviewRepository.findByOrderId(orderId);
    }
    
    // Get reviews by status
    public List<Review> getReviewsByStatus(String status) {
        return reviewRepository.findByStatus(status);
    }
    
    // Get reviews by rating
    public List<Review> getReviewsByRating(int rating) {
        return reviewRepository.findByRating(rating);
    }
    
    // Get reviews by product ID and status
    public List<Review> getReviewsByProductIdAndStatus(String productId, String status) {
        return reviewRepository.findByProductIdAndStatus(productId, status);
    }
    
    // Get reviews by user ID and status
    public List<Review> getReviewsByUserIdAndStatus(String userId, String status) {
        return reviewRepository.findByUserIdAndStatus(userId, status);
    }
    
    // Get reviews by product ID and rating
    public List<Review> getReviewsByProductIdAndRating(String productId, int rating) {
        return reviewRepository.findByProductIdAndRating(productId, rating);
    }
    
    // Get verified reviews by product ID
    public List<Review> getVerifiedReviewsByProductId(String productId) {
        return reviewRepository.findByProductIdAndIsVerified(productId, true);
    }
    
    // Get reviews with images by product ID
    public List<Review> getReviewsWithImagesByProductId(String productId) {
        return reviewRepository.findByProductIdAndImagesExists(productId);
    }
    
    // Get reviews with videos by product ID
    public List<Review> getReviewsWithVideosByProductId(String productId) {
        return reviewRepository.findByProductIdAndVideosExists(productId);
    }
    
    // Get reviews with replies by product ID
    public List<Review> getReviewsWithRepliesByProductId(String productId) {
        return reviewRepository.findByProductIdAndReplyExists(productId);
    }
    
    // Get helpful reviews by product ID
    public List<Review> getHelpfulReviewsByProductId(String productId) {
        return reviewRepository.findByProductIdAndIsHelpful(productId, true);
    }
    
    // Update review
    public Review updateReview(String id, Review reviewDetails) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setRating(reviewDetails.getRating());
            review.setTitle(reviewDetails.getTitle());
            review.setComment(reviewDetails.getComment());
            review.setImages(reviewDetails.getImages());
            review.setVideos(reviewDetails.getVideos());
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        return null;
    }
    
    // Update review status
    public Review updateReviewStatus(String id, String status) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setStatus(status);
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        return null;
    }
    
    // Add reply to review
    public Review addReplyToReview(String id, Review.ReviewReply reply) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setReply(reply);
            review.setUpdatedAt(LocalDateTime.now());
            return reviewRepository.save(review);
        }
        return null;
    }
    
    // Update reply status
    public Review updateReplyStatus(String id, String status) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            if (review.getReply() != null) {
                review.getReply().setStatus(status);
                review.setUpdatedAt(LocalDateTime.now());
                return reviewRepository.save(review);
            }
        }
        return null;
    }
    
    // Mark review as helpful
    public Review markReviewAsHelpful(String id) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setHelpful(true);
            review.incrementHelpfulCount();
            return reviewRepository.save(review);
        }
        return null;
    }
    
    // Unmark review as helpful
    public Review unmarkReviewAsHelpful(String id) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setHelpful(false);
            review.decrementHelpfulCount();
            return reviewRepository.save(review);
        }
        return null;
    }
    
    // Get review statistics by product ID
    public Map<String, Object> getReviewStatisticsByProductId(String productId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Review> allReviews = reviewRepository.findByProductIdAndStatus(productId, "approved");
        stats.put("totalReviews", allReviews.size());
        
        if (!allReviews.isEmpty()) {
            double averageRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
            stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
            
            // Rating distribution
            Map<Integer, Long> ratingDistribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                final int rating = i;
                long count = allReviews.stream()
                    .mapToInt(Review::getRating)
                    .filter(r -> r == rating)
                    .count();
                ratingDistribution.put(i, count);
            }
            stats.put("ratingDistribution", ratingDistribution);
            
            // Verified reviews count
            long verifiedCount = allReviews.stream()
                .mapToInt(review -> review.isVerified() ? 1 : 0)
                .sum();
            stats.put("verifiedReviews", verifiedCount);
            
            // Reviews with images count
            long imagesCount = allReviews.stream()
                .mapToInt(review -> review.hasImages() ? 1 : 0)
                .sum();
            stats.put("reviewsWithImages", imagesCount);
            
            // Reviews with videos count
            long videosCount = allReviews.stream()
                .mapToInt(review -> review.hasVideos() ? 1 : 0)
                .sum();
            stats.put("reviewsWithVideos", videosCount);
            
            // Reviews with replies count
            long repliesCount = allReviews.stream()
                .mapToInt(review -> review.hasReply() ? 1 : 0)
                .sum();
            stats.put("reviewsWithReplies", repliesCount);
            
            // Helpful reviews count
            long helpfulCount = allReviews.stream()
                .mapToInt(review -> review.isHelpful() ? 1 : 0)
                .sum();
            stats.put("helpfulReviews", helpfulCount);
        } else {
            stats.put("averageRating", 0.0);
            stats.put("ratingDistribution", new HashMap<>());
            stats.put("verifiedReviews", 0);
            stats.put("reviewsWithImages", 0);
            stats.put("reviewsWithVideos", 0);
            stats.put("reviewsWithReplies", 0);
            stats.put("helpfulReviews", 0);
        }
        
        return stats;
    }
    
    // Get review statistics by user ID
    public Map<String, Object> getReviewStatisticsByUserId(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Review> userReviews = reviewRepository.findByUserId(userId);
        stats.put("totalReviews", userReviews.size());
        
        if (!userReviews.isEmpty()) {
            double averageRating = userReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
            stats.put("averageRating", Math.round(averageRating * 10.0) / 10.0);
            
            // Rating distribution
            Map<Integer, Long> ratingDistribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                final int rating = i;
                long count = userReviews.stream()
                    .mapToInt(Review::getRating)
                    .filter(r -> r == rating)
                    .count();
                ratingDistribution.put(i, count);
            }
            stats.put("ratingDistribution", ratingDistribution);
        } else {
            stats.put("averageRating", 0.0);
            stats.put("ratingDistribution", new HashMap<>());
        }
        
        return stats;
    }
    
    // Get overall review statistics
    public Map<String, Object> getOverallReviewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalReviews", reviewRepository.count());
        stats.put("approvedReviews", reviewRepository.countByStatus("approved"));
        stats.put("pendingReviews", reviewRepository.countByStatus("pending"));
        stats.put("rejectedReviews", reviewRepository.countByStatus("rejected"));
        stats.put("hiddenReviews", reviewRepository.countByStatus("hidden"));
        
        return stats;
    }
    
    // Delete review
    public boolean deleteReview(String id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Check if review exists
    public boolean reviewExists(String id) {
        return reviewRepository.existsById(id);
    }
    
    // Check if user has reviewed product
    public boolean hasUserReviewedProduct(String userId, String productId) {
        return reviewRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }
    
    // Check if user has reviewed order
    public boolean hasUserReviewedOrder(String userId, String orderId) {
        return reviewRepository.findByUserIdAndOrderId(userId, orderId).isPresent();
    }
}
