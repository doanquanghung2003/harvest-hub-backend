package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.DTO.CreateReviewDTO;
import com.example.harvesthubbackend.Utils.ImageUrlUtils;
import com.example.harvesthubbackend.Exception.ApiException;
import com.example.harvesthubbackend.Exception.ApiResponse;
import com.example.harvesthubbackend.Exception.ErrorCode;
import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Models.Review;
import com.example.harvesthubbackend.Models.Seller;
import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.Repository.SellerRepository;
import com.example.harvesthubbackend.Service.OrderService;
import com.example.harvesthubbackend.Service.ProductService;
import com.example.harvesthubbackend.Service.ReviewService;
import com.example.harvesthubbackend.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/api/reviews", "/api/v1/reviews"})
@CrossOrigin(origins = "*")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private SellerRepository sellerRepository;
    
    // Create a new review
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review createdReview = reviewService.createReview(review);
        ImageUrlUtils.normalizeReview(createdReview);
        return ResponseEntity.ok(createdReview);
    }
    
    /**
     * Submit review for a delivered order item (verified purchase)
     */
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<Review>> submitReviewForOrder(
            @PathVariable String orderId,
            @Valid @RequestBody CreateReviewDTO request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        
        User currentUser = userService.getByUsername(authentication.getName());
        if (currentUser == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
        
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new ApiException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        if (order.getUserId() == null || !order.getUserId().equals(currentUser.getId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Bạn không thể đánh giá đơn hàng của người khác");
        }
        
        if (!"delivered".equalsIgnoreCase(order.getStatus())) {
            throw new ApiException(ErrorCode.REVIEW_ORDER_NOT_DELIVERED);
        }
        
        String productId = request.getProductId();
        if (productId == null || productId.trim().isEmpty()) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Thiếu thông tin sản phẩm cần đánh giá");
        }
        
        List<Order.OrderItem> orderItems = order.getItems();
        Order.OrderItem targetItem = (orderItems == null) ? null :
            orderItems.stream()
                .filter(item -> productId.equals(item.getProductId()))
                .findFirst()
                .orElse(null);
        
        if (targetItem == null) {
            throw new ApiException(ErrorCode.REVIEW_PRODUCT_NOT_IN_ORDER);
        }
        
        if (targetItem.isReviewed() || targetItem.getReviewId() != null ||
            reviewService.hasUserReviewedOrder(currentUser.getId(), orderId)) {
            throw new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        
        Product product = productService.getById(productId);
        if (product == null) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        
        Review review = new Review();
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setUserId(currentUser.getId());
        review.setUserName(resolveDisplayName(currentUser));
        review.setUserAvatar(currentUser.getAvatar());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImages(request.getImages());
        review.setVerified(true);
        review.setStatus("approved");
        
        Review savedReview = reviewService.createReview(review);
        
        targetItem.setReviewed(true);
        targetItem.setReviewId(savedReview.getId());
        targetItem.setReviewedAt(System.currentTimeMillis());
        orderService.update(orderId, order);
        
        updateProductAggregations(product, savedReview.getRating());
        updateSellerAggregations(product, savedReview.getRating());
        
        ImageUrlUtils.normalizeReview(savedReview);
        ApiResponse<Review> response = ApiResponse.success(savedReview, "Đã ghi nhận đánh giá của bạn");
        return ResponseEntity.ok(response);
    }
    
    // Get review by ID
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable String id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(r -> {
            ImageUrlUtils.normalizeReview(r);
            return ResponseEntity.ok(r);
        }).orElse(ResponseEntity.notFound().build());
    }
    
    // Get reviews by product ID
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable String userId) {
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews by order ID
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Review>> getReviewsByOrderId(@PathVariable String orderId) {
        List<Review> reviews = reviewService.getReviewsByOrderId(orderId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Review>> getReviewsByStatus(@PathVariable String status) {
        List<Review> reviews = reviewService.getReviewsByStatus(status);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews by rating
    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<Review>> getReviewsByRating(@PathVariable int rating) {
        List<Review> reviews = reviewService.getReviewsByRating(rating);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews by product ID and status
    @GetMapping("/product/{productId}/status/{status}")
    public ResponseEntity<List<Review>> getReviewsByProductIdAndStatus(
            @PathVariable String productId, 
            @PathVariable String status) {
        List<Review> reviews = reviewService.getReviewsByProductIdAndStatus(productId, status);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews by user ID and status
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Review>> getReviewsByUserIdAndStatus(
            @PathVariable String userId, 
            @PathVariable String status) {
        List<Review> reviews = reviewService.getReviewsByUserIdAndStatus(userId, status);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews by product ID and rating
    @GetMapping("/product/{productId}/rating/{rating}")
    public ResponseEntity<List<Review>> getReviewsByProductIdAndRating(
            @PathVariable String productId, 
            @PathVariable int rating) {
        List<Review> reviews = reviewService.getReviewsByProductIdAndRating(productId, rating);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get verified reviews by product ID
    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<List<Review>> getVerifiedReviewsByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getVerifiedReviewsByProductId(productId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews with images by product ID
    @GetMapping("/product/{productId}/images")
    public ResponseEntity<List<Review>> getReviewsWithImagesByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsWithImagesByProductId(productId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews with videos by product ID
    @GetMapping("/product/{productId}/videos")
    public ResponseEntity<List<Review>> getReviewsWithVideosByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsWithVideosByProductId(productId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get reviews with replies by product ID
    @GetMapping("/product/{productId}/replies")
    public ResponseEntity<List<Review>> getReviewsWithRepliesByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getReviewsWithRepliesByProductId(productId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Get helpful reviews by product ID
    @GetMapping("/product/{productId}/helpful")
    public ResponseEntity<List<Review>> getHelpfulReviewsByProductId(@PathVariable String productId) {
        List<Review> reviews = reviewService.getHelpfulReviewsByProductId(productId);
        ImageUrlUtils.normalizeReviews(reviews);
        return ResponseEntity.ok(reviews);
    }
    
    // Update review
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable String id, @RequestBody Review reviewDetails) {
        Review updatedReview = reviewService.updateReview(id, reviewDetails);
        if (updatedReview != null) {
            ImageUrlUtils.normalizeReview(updatedReview);
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update review status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Review> updateReviewStatus(@PathVariable String id, @RequestParam String status) {
        Review updatedReview = reviewService.updateReviewStatus(id, status);
        if (updatedReview != null) {
            ImageUrlUtils.normalizeReview(updatedReview);
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Add reply to review
    @PostMapping("/{id}/reply")
    public ResponseEntity<Review> addReplyToReview(@PathVariable String id, @RequestBody Review.ReviewReply reply) {
        Review updatedReview = reviewService.addReplyToReview(id, reply);
        if (updatedReview != null) {
            ImageUrlUtils.normalizeReview(updatedReview);
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update reply status
    @PatchMapping("/{id}/reply/status")
    public ResponseEntity<Review> updateReplyStatus(@PathVariable String id, @RequestParam String status) {
        Review updatedReview = reviewService.updateReplyStatus(id, status);
        if (updatedReview != null) {
            ImageUrlUtils.normalizeReview(updatedReview);
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Mark review as helpful
    @PostMapping("/{id}/helpful")
    public ResponseEntity<Review> markReviewAsHelpful(@PathVariable String id) {
        Review updatedReview = reviewService.markReviewAsHelpful(id);
        if (updatedReview != null) {
            ImageUrlUtils.normalizeReview(updatedReview);
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Unmark review as helpful
    @DeleteMapping("/{id}/helpful")
    public ResponseEntity<Review> unmarkReviewAsHelpful(@PathVariable String id) {
        Review updatedReview = reviewService.unmarkReviewAsHelpful(id);
        if (updatedReview != null) {
            ImageUrlUtils.normalizeReview(updatedReview);
            return ResponseEntity.ok(updatedReview);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Get review statistics by product ID
    @GetMapping("/product/{productId}/statistics")
    public ResponseEntity<Map<String, Object>> getReviewStatisticsByProductId(@PathVariable String productId) {
        Map<String, Object> statistics = reviewService.getReviewStatisticsByProductId(productId);
        return ResponseEntity.ok(statistics);
    }
    
    // Get review statistics by user ID
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getReviewStatisticsByUserId(@PathVariable String userId) {
        Map<String, Object> statistics = reviewService.getReviewStatisticsByUserId(userId);
        return ResponseEntity.ok(statistics);
    }
    
    // Get overall review statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOverallReviewStatistics() {
        Map<String, Object> statistics = reviewService.getOverallReviewStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    // Delete review
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        boolean deleted = reviewService.deleteReview(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if review exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> reviewExists(@PathVariable String id) {
        boolean exists = reviewService.reviewExists(id);
        return ResponseEntity.ok(exists);
    }
    
    // Check if user has reviewed product
    @GetMapping("/user/{userId}/product/{productId}/exists")
    public ResponseEntity<Boolean> hasUserReviewedProduct(
            @PathVariable String userId, 
            @PathVariable String productId) {
        boolean exists = reviewService.hasUserReviewedProduct(userId, productId);
        return ResponseEntity.ok(exists);
    }
    
    // Check if user has reviewed order
    @GetMapping("/user/{userId}/order/{orderId}/exists")
    public ResponseEntity<Boolean> hasUserReviewedOrder(
            @PathVariable String userId, 
            @PathVariable String orderId) {
        boolean exists = reviewService.hasUserReviewedOrder(userId, orderId);
        return ResponseEntity.ok(exists);
    }
    
    private String resolveDisplayName(User user) {
        if (user == null) {
            return "Người dùng";
        }
        StringBuilder builder = new StringBuilder();
        if (user.getLastName() != null && !user.getLastName().trim().isEmpty()) {
            builder.append(user.getLastName().trim());
        }
        if (user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            builder.append(user.getFirstName().trim());
        }
        String result = builder.toString().trim();
        if (result.isEmpty()) {
            return user.getUsername() != null ? user.getUsername() : "Người dùng";
        }
        return result;
    }
    
    private void updateProductAggregations(Product product, int rating) {
        if (product == null) {
            return;
        }
        int currentCount = Math.max(product.getReviewCount(), 0);
        double totalScore = product.getRating() * currentCount;
        int newCount = currentCount + 1;
        double newAverage = (totalScore + rating) / newCount;
        product.setReviewCount(newCount);
        product.setRating(Math.round(newAverage * 10.0) / 10.0);
        product.setUpdatedAt(LocalDateTime.now());
        productService.update(product.getId(), product);
    }
    
    private void updateSellerAggregations(Product product, int rating) {
        if (product == null) {
            return;
        }
        String sellerId = product.getSellerId();
        if (sellerId == null) {
            return;
        }
        Seller seller = sellerRepository.findById(sellerId).orElse(null);
        if (seller == null) {
            seller = sellerRepository.findByUserId(sellerId).orElse(null);
        }
        if (seller == null) {
            return;
        }
        int currentCount = Math.max(seller.getReviewCount(), 0);
        double totalScore = seller.getAverageRating() * currentCount;
        int newCount = currentCount + 1;
        double newAverage = (totalScore + rating) / newCount;
        seller.setReviewCount(newCount);
        seller.setAverageRating(Math.round(newAverage * 10.0) / 10.0);
        seller.setUpdatedAt(LocalDateTime.now());
        sellerRepository.save(seller);
    }
}
