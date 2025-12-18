package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Shop;
import com.example.harvesthubbackend.Service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/shops")
@CrossOrigin(origins = "*")
public class ShopController {
    
    @Autowired
    private ShopService shopService;
    
    // Create a new shop
    @PostMapping
    public ResponseEntity<Shop> createShop(@RequestBody Shop shop) {
        Shop createdShop = shopService.createShop(shop);
        return ResponseEntity.ok(createdShop);
    }
    
    // Get shop by ID
    @GetMapping("/{id}")
    public ResponseEntity<Shop> getShopById(@PathVariable String id) {
        System.out.println("=== API: Get shop by ID: " + id + " ===");
        Optional<Shop> shop = shopService.getShopById(id);
        if (shop.isPresent()) {
            System.out.println("Shop found: " + shop.get().getName() + ", OwnerId: " + shop.get().getOwnerId());
            if (shop.get().getStats() != null) {
                System.out.println("Stats - Products: " + shop.get().getStats().getTotalProducts() + 
                                 ", Followers: " + shop.get().getStats().getFollowers() +
                                 ", Reviews: " + shop.get().getStats().getTotalReviews());
            }
        } else {
            System.out.println("Shop not found with ID: " + id);
        }
        return shop.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    // Get shop by owner ID
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Shop> getShopByOwnerId(@PathVariable String ownerId) {
        System.out.println("=== API: Get shop by ownerId: " + ownerId + " ===");
        Optional<Shop> shop = shopService.getShopByOwnerId(ownerId);
        if (shop.isPresent()) {
            System.out.println("Shop found: " + shop.get().getName() + ", ShopId: " + shop.get().getId());
            if (shop.get().getStats() != null) {
                System.out.println("Stats - Products: " + shop.get().getStats().getTotalProducts() + 
                                 ", Followers: " + shop.get().getStats().getFollowers() +
                                 ", Reviews: " + shop.get().getStats().getTotalReviews());
            }
        } else {
            System.out.println("Shop not found with ownerId: " + ownerId);
        }
        return shop.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    // Get shop by seller ID (alias for ownerId, commonly used in products)
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Shop> getShopBySellerId(@PathVariable String sellerId) {
        System.out.println("=== API: Get shop by sellerId: " + sellerId + " ===");
        Optional<Shop> shop = shopService.getShopByOwnerId(sellerId);
        if (shop.isPresent()) {
            System.out.println("Shop found: " + shop.get().getName() + ", ShopId: " + shop.get().getId());
            if (shop.get().getStats() != null) {
                System.out.println("Stats - Products: " + shop.get().getStats().getTotalProducts() + 
                                 ", Followers: " + shop.get().getStats().getFollowers() +
                                 ", Reviews: " + shop.get().getStats().getTotalReviews());
            }
        } else {
            System.out.println("Shop not found with sellerId: " + sellerId);
        }
        return shop.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    // Refresh shop stats (force recalculation)
    @PostMapping("/{id}/refresh-stats")
    public ResponseEntity<Shop> refreshShopStats(@PathVariable String id) {
        System.out.println("=== API: Refresh stats for shop: " + id + " ===");
        Optional<Shop> shop = shopService.getShopById(id);
        if (shop.isPresent()) {
            return ResponseEntity.ok(shop.get());
        }
        return ResponseEntity.notFound().build();
    }
    
    // Get all shops
    @GetMapping
    public ResponseEntity<List<Shop>> getAllShops() {
        List<Shop> shops = shopService.getAllShops();
        return ResponseEntity.ok(shops);
    }
    
    // Get shops by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Shop>> getShopsByStatus(@PathVariable String status) {
        List<Shop> shops = shopService.getShopsByStatus(status);
        return ResponseEntity.ok(shops);
    }
    
    // Search shops by name
    @GetMapping("/search")
    public ResponseEntity<List<Shop>> searchShopsByName(@RequestParam String name) {
        List<Shop> shops = shopService.searchShopsByName(name);
        return ResponseEntity.ok(shops);
    }
    
    // Get shops by city
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Shop>> getShopsByCity(@PathVariable String city) {
        List<Shop> shops = shopService.getShopsByCity(city);
        return ResponseEntity.ok(shops);
    }
    
    // Get shops by business category
    @GetMapping("/category/{businessCategory}")
    public ResponseEntity<List<Shop>> getShopsByBusinessCategory(@PathVariable String businessCategory) {
        List<Shop> shops = shopService.getShopsByBusinessCategory(businessCategory);
        return ResponseEntity.ok(shops);
    }
    
    // Get top-rated shops
    @GetMapping("/top-rated")
    public ResponseEntity<List<Shop>> getTopRatedShops() {
        List<Shop> shops = shopService.getTopRatedShops();
        return ResponseEntity.ok(shops);
    }
    
    // Get most followed shops
    @GetMapping("/most-followed")
    public ResponseEntity<List<Shop>> getMostFollowedShops() {
        List<Shop> shops = shopService.getMostFollowedShops();
        return ResponseEntity.ok(shops);
    }
    
    // Get top revenue shops
    @GetMapping("/top-revenue")
    public ResponseEntity<List<Shop>> getTopRevenueShops() {
        List<Shop> shops = shopService.getTopRevenueShops();
        return ResponseEntity.ok(shops);
    }
    
    // Get shops with minimum rating
    @GetMapping("/rating/{minRating}")
    public ResponseEntity<List<Shop>> getShopsByMinRating(@PathVariable double minRating) {
        List<Shop> shops = shopService.getShopsByMinRating(minRating);
        return ResponseEntity.ok(shops);
    }
    
    // Get shops with minimum followers
    @GetMapping("/followers/{minFollowers}")
    public ResponseEntity<List<Shop>> getShopsByMinFollowers(@PathVariable int minFollowers) {
        List<Shop> shops = shopService.getShopsByMinFollowers(minFollowers);
        return ResponseEntity.ok(shops);
    }
    
    // Get shops by multiple criteria
    @GetMapping("/criteria")
    public ResponseEntity<List<Shop>> getShopsByCriteria(
            @RequestParam String status,
            @RequestParam double minRating,
            @RequestParam int minFollowers) {
        List<Shop> shops = shopService.getShopsByCriteria(status, minRating, minFollowers);
        return ResponseEntity.ok(shops);
    }
    
    // Update shop
    @PutMapping("/{id}")
    public ResponseEntity<Shop> updateShop(@PathVariable String id, @RequestBody Shop shopDetails) {
        Shop updatedShop = shopService.updateShop(id, shopDetails);
        if (updatedShop != null) {
            return ResponseEntity.ok(updatedShop);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update shop status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Shop> updateShopStatus(@PathVariable String id, @RequestParam String status) {
        Shop updatedShop = shopService.updateShopStatus(id, status);
        if (updatedShop != null) {
            return ResponseEntity.ok(updatedShop);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update shop stats
    @PutMapping("/{id}/stats")
    public ResponseEntity<Shop> updateShopStats(@PathVariable String id, @RequestBody Shop.ShopStats stats) {
        Shop updatedShop = shopService.updateShopStats(id, stats);
        if (updatedShop != null) {
            return ResponseEntity.ok(updatedShop);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Delete shop
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable String id) {
        boolean deleted = shopService.deleteShop(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if shop exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> shopExists(@PathVariable String id) {
        boolean exists = shopService.shopExists(id);
        return ResponseEntity.ok(exists);
    }
    
    // Check if shop exists by owner ID
    @GetMapping("/owner/{ownerId}/exists")
    public ResponseEntity<Boolean> shopExistsByOwnerId(@PathVariable String ownerId) {
        boolean exists = shopService.shopExistsByOwnerId(ownerId);
        return ResponseEntity.ok(exists);
    }
    
    // Get shop count by status
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getShopCountByStatus(@PathVariable String status) {
        long count = shopService.getShopCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    // Get shops by owner ID and status
    @GetMapping("/owner/{ownerId}/status/{status}")
    public ResponseEntity<List<Shop>> getShopsByOwnerIdAndStatus(
            @PathVariable String ownerId, 
            @PathVariable String status) {
        List<Shop> shops = shopService.getShopsByOwnerIdAndStatus(ownerId, status);
        return ResponseEntity.ok(shops);
    }
    
    // Follow a shop
    @PostMapping("/{shopId}/follow")
    public ResponseEntity<Map<String, Object>> followShop(
            @PathVariable String shopId,
            @RequestParam String userId) {
        try {
            boolean followed = shopService.followShop(userId, shopId);
            Map<String, Object> response = new HashMap<>();
            if (followed) {
                response.put("success", true);
                response.put("message", "Đã theo dõi shop thành công");
                response.put("isFollowing", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Shop không tồn tại");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi theo dõi shop: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Unfollow a shop
    @PostMapping("/{shopId}/unfollow")
    public ResponseEntity<Map<String, Object>> unfollowShop(
            @PathVariable String shopId,
            @RequestParam String userId) {
        try {
            boolean unfollowed = shopService.unfollowShop(userId, shopId);
            Map<String, Object> response = new HashMap<>();
            if (unfollowed) {
                response.put("success", true);
                response.put("message", "Đã bỏ theo dõi shop thành công");
                response.put("isFollowing", false);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Bạn chưa theo dõi shop này");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi bỏ theo dõi shop: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Check if user is following a shop
    @GetMapping("/{shopId}/is-following")
    public ResponseEntity<Map<String, Object>> isFollowing(
            @PathVariable String shopId,
            @RequestParam String userId) {
        try {
            boolean isFollowing = shopService.isFollowing(userId, shopId);
            Map<String, Object> response = new HashMap<>();
            response.put("isFollowing", isFollowing);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("isFollowing", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
