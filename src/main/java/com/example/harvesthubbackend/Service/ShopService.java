package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Shop;
import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Models.Review;
import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Models.Follow;
import com.example.harvesthubbackend.Repository.ShopRepository;
import com.example.harvesthubbackend.Repository.ProductRepository;
import com.example.harvesthubbackend.Repository.ReviewRepository;
import com.example.harvesthubbackend.Repository.FollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopService {
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private OrderService orderService;
    
    // Create a new shop
    public Shop createShop(Shop shop) {
        shop.setCreatedAt(LocalDateTime.now());
        shop.setUpdatedAt(LocalDateTime.now());
        return shopRepository.save(shop);
    }
    
    // Get shop by ID
    public Optional<Shop> getShopById(String id) {
        Optional<Shop> shopOpt = shopRepository.findById(id);
        if (shopOpt.isPresent()) {
            Shop shop = shopOpt.get();
            calculateShopStats(shop);
            return Optional.of(shop);
        }
        return shopOpt;
    }
    
    // Get shop by owner ID
    public Optional<Shop> getShopByOwnerId(String ownerId) {
        Optional<Shop> shopOpt = shopRepository.findByOwnerId(ownerId);
        if (shopOpt.isPresent()) {
            Shop shop = shopOpt.get();
            calculateShopStats(shop);
            return Optional.of(shop);
        }
        return shopOpt;
    }
    
    // Get all shops
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }
    
    // Get shops by status
    public List<Shop> getShopsByStatus(String status) {
        return shopRepository.findByStatus(status);
    }
    
    // Get shops by name (search)
    public List<Shop> searchShopsByName(String name) {
        return shopRepository.findByNameContainingIgnoreCase(name);
    }
    
    // Get shops by city
    public List<Shop> getShopsByCity(String city) {
        return shopRepository.findByCity(city);
    }
    
    // Get shops by business category
    public List<Shop> getShopsByBusinessCategory(String businessCategory) {
        return shopRepository.findByBusinessCategory(businessCategory);
    }
    
    // Get top-rated shops
    public List<Shop> getTopRatedShops() {
        return shopRepository.findAllOrderByRatingDesc();
    }
    
    // Get most followed shops
    public List<Shop> getMostFollowedShops() {
        return shopRepository.findAllOrderByFollowersDesc();
    }
    
    // Get top revenue shops
    public List<Shop> getTopRevenueShops() {
        return shopRepository.findAllOrderByRevenueDesc();
    }
    
    // Get shops with minimum rating
    public List<Shop> getShopsByMinRating(double minRating) {
        return shopRepository.findByMinRating(minRating);
    }
    
    // Get shops with minimum followers
    public List<Shop> getShopsByMinFollowers(int minFollowers) {
        return shopRepository.findByMinFollowers(minFollowers);
    }
    
    // Get shops by multiple criteria
    public List<Shop> getShopsByCriteria(String status, double minRating, int minFollowers) {
        return shopRepository.findByStatusAndMinRatingAndMinFollowers(status, minRating, minFollowers);
    }
    
    // Update shop
    public Shop updateShop(String id, Shop shopDetails) {
        Optional<Shop> optionalShop = shopRepository.findById(id);
        if (optionalShop.isPresent()) {
            Shop shop = optionalShop.get();
            shop.setName(shopDetails.getName());
            shop.setDescription(shopDetails.getDescription());
            shop.setLogo(shopDetails.getLogo());
            shop.setBanner(shopDetails.getBanner());
            shop.setContactInfo(shopDetails.getContactInfo());
            shop.setAddress(shopDetails.getAddress());
            shop.setBusinessInfo(shopDetails.getBusinessInfo());
            shop.setSettings(shopDetails.getSettings());
            shop.setUpdatedAt(LocalDateTime.now());
            return shopRepository.save(shop);
        }
        return null;
    }
    
    // Update shop status
    public Shop updateShopStatus(String id, String status) {
        Optional<Shop> optionalShop = shopRepository.findById(id);
        if (optionalShop.isPresent()) {
            Shop shop = optionalShop.get();
            shop.setStatus(status);
            shop.setUpdatedAt(LocalDateTime.now());
            return shopRepository.save(shop);
        }
        return null;
    }
    
    // Update shop stats
    public Shop updateShopStats(String id, Shop.ShopStats stats) {
        Optional<Shop> optionalShop = shopRepository.findById(id);
        if (optionalShop.isPresent()) {
            Shop shop = optionalShop.get();
            shop.setStats(stats);
            shop.setUpdatedAt(LocalDateTime.now());
            return shopRepository.save(shop);
        }
        return null;
    }
    
    // Delete shop
    public boolean deleteShop(String id) {
        if (shopRepository.existsById(id)) {
            shopRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Check if shop exists
    public boolean shopExists(String id) {
        return shopRepository.existsById(id);
    }
    
    // Check if shop exists by owner ID
    public boolean shopExistsByOwnerId(String ownerId) {
        return shopRepository.findByOwnerId(ownerId).isPresent();
    }
    
    // Get shop count by status
    public long getShopCountByStatus(String status) {
        return shopRepository.countByStatus(status);
    }
    
    // Get shops by owner ID and status
    public List<Shop> getShopsByOwnerIdAndStatus(String ownerId, String status) {
        return shopRepository.findByOwnerIdAndStatus(ownerId, status);
    }
    
    // Tính toán stats cho shop từ database
    private void calculateShopStats(Shop shop) {
        if (shop == null || shop.getId() == null) {
            System.err.println("Shop is null or has no ID, cannot calculate stats");
            return;
        }
        
        String shopId = shop.getId();
        String ownerId = shop.getOwnerId();
        
        System.out.println("=== Calculating stats for shop: " + shopId + ", ownerId: " + ownerId + " ===");
        
        // Khởi tạo stats nếu chưa có
        if (shop.getStats() == null) {
            shop.setStats(new Shop.ShopStats());
        }
        
        Shop.ShopStats stats = shop.getStats();
        
        try {
            // 1. Tính tổng số sản phẩm (theo sellerId)
            int totalProducts = 0;
            if (ownerId != null && productRepository != null) {
                try {
                    List<Product> products = productRepository.findBySellerId(ownerId);
                    System.out.println("Found " + (products != null ? products.size() : 0) + " products for sellerId: " + ownerId);
                    if (products != null) {
                        // Chỉ đếm sản phẩm đã được duyệt và active
                        totalProducts = (int) products.stream()
                            .filter(p -> {
                                boolean approved = "approved".equals(p.getApprovalStatus());
                                boolean active = "active".equals(p.getStatus()) || p.getStatus() == null;
                                return approved && active;
                            })
                            .count();
                        System.out.println("Active approved products: " + totalProducts);
                    }
                } catch (Exception e) {
                    System.err.println("Error counting products: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Cannot count products: ownerId=" + ownerId + ", productRepository=" + (productRepository != null ? "exists" : "null"));
            }
            stats.setTotalProducts(totalProducts);
            System.out.println("Set totalProducts to: " + totalProducts);
            
            // 2. Tính tổng số đánh giá và rating trung bình
            int totalReviews = 0;
            double averageRating = 0.0;
            if (ownerId != null && productRepository != null && reviewRepository != null) {
                try {
                    List<Product> products = productRepository.findBySellerId(ownerId);
                    if (products != null && !products.isEmpty()) {
                        List<String> productIds = products.stream()
                            .map(Product::getId)
                            .filter(id -> id != null)
                            .collect(Collectors.toList());
                        
                        if (!productIds.isEmpty()) {
                            // Lấy tất cả reviews của các sản phẩm này
                            List<Review> allReviews = reviewRepository.findAll().stream()
                                .filter(review -> review.getProductId() != null && productIds.contains(review.getProductId()))
                                .filter(review -> "approved".equals(review.getStatus()) || review.getStatus() == null)
                                .collect(Collectors.toList());
                            
                            totalReviews = allReviews.size();
                            
                            // Tính rating trung bình
                            if (!allReviews.isEmpty()) {
                                double totalRating = allReviews.stream()
                                    .mapToInt(Review::getRating)
                                    .sum();
                                averageRating = totalRating / allReviews.size();
                                // Làm tròn 1 chữ số thập phân
                                averageRating = Math.round(averageRating * 10.0) / 10.0;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating reviews: " + e.getMessage());
                }
            }
            stats.setTotalReviews(totalReviews);
            stats.setAverageRating(averageRating);
            
            // 3. Tính số lượng followers
            int followers = 0;
            if (followRepository != null) {
                try {
                    long followersCount = followRepository.countByShopId(shopId);
                    followers = (int) followersCount;
                    System.out.println("Followers count: " + followers);
                } catch (Exception e) {
                    System.err.println("Error counting followers: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("FollowRepository is null, cannot count followers");
            }
            stats.setFollowers(followers);
            
            // 4. Tính tổng số đơn hàng và revenue
            if (ownerId != null && orderService != null) {
                try {
                    List<Order> orders = orderService.getBySellerId(ownerId);
                    if (orders != null) {
                        stats.setTotalOrders(orders.size());
                        
                        // Tính revenue từ các đơn đã thanh toán
                        double revenue = orders.stream()
                            .filter(o -> "paid".equals(o.getPaymentStatus()))
                            .mapToDouble(Order::getTotalPrice)
                            .sum();
                        stats.setTotalRevenue(revenue);
                        
                        // Tính số lượng khách hàng unique
                        long uniqueCustomers = orders.stream()
                            .map(Order::getUserId)
                            .distinct()
                            .count();
                        stats.setTotalCustomers((int) uniqueCustomers);
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating order stats: " + e.getMessage());
                }
            }
            
            // 5. Tính response rate và response time
            // Response rate: tỉ lệ phản hồi dựa trên số đơn đã được xử lý
            double responseRate = 0.0;
            String responseTime = "Đang cập nhật";
            
            if (ownerId != null && orderService != null) {
                try {
                    List<Order> orders = orderService.getBySellerId(ownerId);
                    if (orders != null && !orders.isEmpty()) {
                        // Đếm số đơn đã được xử lý (không phải pending)
                        long processedOrders = orders.stream()
                            .filter(o -> o.getStatus() != null && !"pending".equals(o.getStatus()))
                            .count();
                        
                        if (processedOrders > 0) {
                            responseRate = (double) processedOrders / orders.size() * 100;
                            responseRate = Math.round(responseRate * 10.0) / 10.0; // Làm tròn 1 chữ số thập phân
                            
                            // Response time: dựa trên số đơn đã xử lý
                            // Nếu tỉ lệ phản hồi cao (>80%), hiển thị "Trong vài giờ"
                            if (responseRate >= 80) {
                                responseTime = "Trong vài giờ";
                            } else if (responseRate >= 50) {
                                responseTime = "Trong 24 giờ";
                            } else {
                                responseTime = "Trong vài ngày";
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating response rate: " + e.getMessage());
                }
            }
            
            stats.setResponseRate(responseRate);
            stats.setResponseTime(responseTime);
            
            // 6. Format thời gian tham gia (joinedAt)
            if (shop.getCreatedAt() != null) {
                stats.setJoinedAt(formatTimeAgo(shop.getCreatedAt()));
            } else {
                stats.setJoinedAt("Đang cập nhật");
            }
            
            System.out.println("=== Stats calculated successfully ===");
            System.out.println("  - Products: " + stats.getTotalProducts());
            System.out.println("  - Reviews: " + stats.getTotalReviews());
            System.out.println("  - Rating: " + stats.getAverageRating());
            System.out.println("  - Followers: " + stats.getFollowers());
            System.out.println("  - Response Rate: " + stats.getResponseRate() + "%");
            System.out.println("  - Response Time: " + stats.getResponseTime());
            System.out.println("  - Joined: " + stats.getJoinedAt());
            
        } catch (Exception e) {
            System.err.println("Error calculating shop stats for shop " + shopId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Follow a shop
    public boolean followShop(String userId, String shopId) {
        try {
            // Kiểm tra shop có tồn tại không
            if (!shopRepository.existsById(shopId)) {
                return false;
            }
            
            // Kiểm tra đã follow chưa
            if (followRepository.existsByUserIdAndShopId(userId, shopId)) {
                return true; // Đã follow rồi
            }
            
            // Tạo follow mới
            Follow follow = new Follow(userId, shopId);
            followRepository.save(follow);
            
            // Cập nhật stats của shop
            Optional<Shop> shopOpt = shopRepository.findById(shopId);
            if (shopOpt.isPresent()) {
                Shop shop = shopOpt.get();
                calculateShopStats(shop);
                shopRepository.save(shop);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error following shop: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Unfollow a shop
    public boolean unfollowShop(String userId, String shopId) {
        try {
            // Kiểm tra đã follow chưa
            if (!followRepository.existsByUserIdAndShopId(userId, shopId)) {
                return false;
            }
            
            // Xóa follow
            followRepository.deleteByUserIdAndShopId(userId, shopId);
            
            // Cập nhật stats của shop
            Optional<Shop> shopOpt = shopRepository.findById(shopId);
            if (shopOpt.isPresent()) {
                Shop shop = shopOpt.get();
                calculateShopStats(shop);
                shopRepository.save(shop);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error unfollowing shop: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Check if user is following a shop
    public boolean isFollowing(String userId, String shopId) {
        try {
            return followRepository.existsByUserIdAndShopId(userId, shopId);
        } catch (Exception e) {
            System.err.println("Error checking follow status: " + e.getMessage());
            return false;
        }
    }
    
    // Format thời gian thành "X tuần trước", "X tháng trước", etc.
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Đang cập nhật";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime, now);
        long weeks = ChronoUnit.WEEKS.between(dateTime, now);
        long months = ChronoUnit.MONTHS.between(dateTime, now);
        long years = ChronoUnit.YEARS.between(dateTime, now);
        
        if (years > 0) {
            return years + " năm trước";
        } else if (months > 0) {
            return months + " tháng trước";
        } else if (weeks > 0) {
            return weeks + " tuần trước";
        } else if (days > 0) {
            return days + " ngày trước";
        } else {
            long hours = ChronoUnit.HOURS.between(dateTime, now);
            if (hours > 0) {
                return hours + " giờ trước";
            } else {
                long minutes = ChronoUnit.MINUTES.between(dateTime, now);
                if (minutes > 0) {
                    return minutes + " phút trước";
                } else {
                    return "Vừa xong";
                }
            }
        }
    }
}
