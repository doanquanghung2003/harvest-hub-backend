package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Banner;
import com.example.harvesthubbackend.Service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin(origins = "*")
public class BannerController {
    
    @Autowired
    private BannerService bannerService;
    
    // Create a new banner
    @PostMapping
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
        Banner createdBanner = bannerService.createBanner(banner);
        return ResponseEntity.ok(createdBanner);
    }
    
    // Get banner by ID
    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable String id) {
        Optional<Banner> banner = bannerService.getBannerById(id);
        return banner.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
    
    // Get all banners
    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners() {
        List<Banner> banners = bannerService.getAllBanners();
        return ResponseEntity.ok(banners);
    }
    
    // Get active banners
    @GetMapping("/active")
    public ResponseEntity<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return ResponseEntity.ok(banners);
    }
    
    // Get banners by position
    @GetMapping("/position/{position}")
    public ResponseEntity<List<Banner>> getBannersByPosition(@PathVariable String position) {
        List<Banner> banners = bannerService.getBannersByPosition(position);
        return ResponseEntity.ok(banners);
    }
    
    // Get active banners by position
    @GetMapping("/position/{position}/active")
    public ResponseEntity<List<Banner>> getActiveBannersByPosition(@PathVariable String position) {
        List<Banner> banners = bannerService.getActiveBannersByPosition(position);
        return ResponseEntity.ok(banners);
    }
    
    // Get banners by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Banner>> getBannersByStatus(@PathVariable String status) {
        List<Banner> banners = bannerService.getBannersByStatus(status);
        return ResponseEntity.ok(banners);
    }
    
    // Get banners by link type
    @GetMapping("/link-type/{linkType}")
    public ResponseEntity<List<Banner>> getBannersByLinkType(@PathVariable String linkType) {
        List<Banner> banners = bannerService.getBannersByLinkType(linkType);
        return ResponseEntity.ok(banners);
    }
    
    // Get banners by target ID
    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<Banner>> getBannersByTargetId(@PathVariable String targetId) {
        List<Banner> banners = bannerService.getBannersByTargetId(targetId);
        return ResponseEntity.ok(banners);
    }
    
    // Search banners by title
    @GetMapping("/search")
    public ResponseEntity<List<Banner>> searchBannersByTitle(@RequestParam String title) {
        List<Banner> banners = bannerService.searchBannersByTitle(title);
        return ResponseEntity.ok(banners);
    }
    
    // Get banners expiring soon
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<Banner>> getBannersExpiringSoon(@RequestParam int days) {
        List<Banner> banners = bannerService.getBannersExpiringSoon(days);
        return ResponseEntity.ok(banners);
    }
    
    // Get banners starting soon
    @GetMapping("/starting-soon")
    public ResponseEntity<List<Banner>> getBannersStartingSoon(@RequestParam int days) {
        List<Banner> banners = bannerService.getBannersStartingSoon(days);
        return ResponseEntity.ok(banners);
    }
    
    // Get banners with minimum priority
    @GetMapping("/priority/{minPriority}")
    public ResponseEntity<List<Banner>> getBannersByMinPriority(@PathVariable int minPriority) {
        List<Banner> banners = bannerService.getBannersByMinPriority(minPriority);
        return ResponseEntity.ok(banners);
    }
    
    // Update banner
    @PutMapping("/{id}")
    public ResponseEntity<Banner> updateBanner(@PathVariable String id, @RequestBody Banner bannerDetails) {
        Banner updatedBanner = bannerService.updateBanner(id, bannerDetails);
        if (updatedBanner != null) {
            return ResponseEntity.ok(updatedBanner);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update banner status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Banner> updateBannerStatus(@PathVariable String id, @RequestParam String status) {
        Banner updatedBanner = bannerService.updateBannerStatus(id, status);
        if (updatedBanner != null) {
            return ResponseEntity.ok(updatedBanner);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Increment click count
    @PostMapping("/{id}/click")
    public ResponseEntity<Banner> incrementClickCount(@PathVariable String id) {
        Banner updatedBanner = bannerService.incrementClickCount(id);
        if (updatedBanner != null) {
            return ResponseEntity.ok(updatedBanner);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Increment view count
    @PostMapping("/{id}/view")
    public ResponseEntity<Banner> incrementViewCount(@PathVariable String id) {
        Banner updatedBanner = bannerService.incrementViewCount(id);
        if (updatedBanner != null) {
            return ResponseEntity.ok(updatedBanner);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Update banner priority
    @PatchMapping("/{id}/priority")
    public ResponseEntity<Banner> updateBannerPriority(@PathVariable String id, @RequestParam int priority) {
        Banner updatedBanner = bannerService.updateBannerPriority(id, priority);
        if (updatedBanner != null) {
            return ResponseEntity.ok(updatedBanner);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if banner is valid
    @GetMapping("/{id}/valid")
    public ResponseEntity<Boolean> isBannerValid(@PathVariable String id) {
        boolean isValid = bannerService.isBannerValid(id);
        return ResponseEntity.ok(isValid);
    }
    
    // Delete banner
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable String id) {
        boolean deleted = bannerService.deleteBanner(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Check if banner exists
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> bannerExists(@PathVariable String id) {
        boolean exists = bannerService.bannerExists(id);
        return ResponseEntity.ok(exists);
    }
    
    // Get banner count by status
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getBannerCountByStatus(@PathVariable String status) {
        long count = bannerService.getBannerCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    // Get banner count by position
    @GetMapping("/count/position/{position}")
    public ResponseEntity<Long> getBannerCountByPosition(@PathVariable String position) {
        long count = bannerService.getBannerCountByPosition(position);
        return ResponseEntity.ok(count);
    }
    
    // Get banners sorted by priority
    @GetMapping("/sorted/priority")
    public ResponseEntity<List<Banner>> getBannersOrderByPriority() {
        List<Banner> banners = bannerService.getBannersOrderByPriority();
        return ResponseEntity.ok(banners);
    }
    
    // Get banners sorted by click count
    @GetMapping("/sorted/clicks")
    public ResponseEntity<List<Banner>> getBannersOrderByClickCount() {
        List<Banner> banners = bannerService.getBannersOrderByClickCount();
        return ResponseEntity.ok(banners);
    }
    
    // Get banners sorted by view count
    @GetMapping("/sorted/views")
    public ResponseEntity<List<Banner>> getBannersOrderByViewCount() {
        List<Banner> banners = bannerService.getBannersOrderByViewCount();
        return ResponseEntity.ok(banners);
    }
    
    // Get banners sorted by creation date
    @GetMapping("/sorted/created-at")
    public ResponseEntity<List<Banner>> getBannersOrderByCreatedAt() {
        List<Banner> banners = bannerService.getBannersOrderByCreatedAt();
        return ResponseEntity.ok(banners);
    }
}
