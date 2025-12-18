package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Banner;
import com.example.harvesthubbackend.Repository.BannerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BannerService {
    
    @Autowired
    private BannerRepository bannerRepository;
    
    // Create a new banner
    public Banner createBanner(Banner banner) {
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());
        return bannerRepository.save(banner);
    }
    
    // Get banner by ID
    public Optional<Banner> getBannerById(String id) {
        return bannerRepository.findById(id);
    }
    
    // Get all banners
    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }
    
    // Get active banners
    public List<Banner> getActiveBanners() {
        return bannerRepository.findActiveBanners(LocalDateTime.now());
    }
    
    // Get banners by position
    public List<Banner> getBannersByPosition(String position) {
        return bannerRepository.findByPosition(position);
    }
    
    // Get active banners by position
    public List<Banner> getActiveBannersByPosition(String position) {
        return bannerRepository.findActiveBannersByPosition(position, LocalDateTime.now());
    }
    
    // Get banners by status
    public List<Banner> getBannersByStatus(String status) {
        return bannerRepository.findByStatus(status);
    }
    
    // Get banners by link type
    public List<Banner> getBannersByLinkType(String linkType) {
        return bannerRepository.findByLinkType(linkType);
    }
    
    // Get banners by target ID
    public List<Banner> getBannersByTargetId(String targetId) {
        return bannerRepository.findByTargetId(targetId);
    }
    
    // Get banners by title (search)
    public List<Banner> searchBannersByTitle(String title) {
        return bannerRepository.findByTitleContainingIgnoreCase(title);
    }
    
    // Get banners expiring soon
    public List<Banner> getBannersExpiringSoon(int days) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(days);
        return bannerRepository.findBannersExpiringSoon(start, end);
    }
    
    // Get banners starting soon
    public List<Banner> getBannersStartingSoon(int days) {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(days);
        return bannerRepository.findBannersStartingSoon(start, end);
    }
    
    // Get banners with minimum priority
    public List<Banner> getBannersByMinPriority(int minPriority) {
        return bannerRepository.findByMinPriority(minPriority);
    }
    
    // Update banner
    public Banner updateBanner(String id, Banner bannerDetails) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            Banner banner = optionalBanner.get();
            banner.setTitle(bannerDetails.getTitle());
            banner.setDescription(bannerDetails.getDescription());
            banner.setImage(bannerDetails.getImage());
            banner.setLink(bannerDetails.getLink());
            banner.setLinkType(bannerDetails.getLinkType());
            banner.setTargetId(bannerDetails.getTargetId());
            banner.setPosition(bannerDetails.getPosition());
            banner.setPriority(bannerDetails.getPriority());
            banner.setStartDate(bannerDetails.getStartDate());
            banner.setEndDate(bannerDetails.getEndDate());
            banner.setStatus(bannerDetails.getStatus());
            banner.setUpdatedAt(LocalDateTime.now());
            return bannerRepository.save(banner);
        }
        return null;
    }
    
    // Update banner status
    public Banner updateBannerStatus(String id, String status) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            Banner banner = optionalBanner.get();
            banner.setStatus(status);
            banner.setUpdatedAt(LocalDateTime.now());
            return bannerRepository.save(banner);
        }
        return null;
    }
    
    // Increment click count
    public Banner incrementClickCount(String id) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            Banner banner = optionalBanner.get();
            banner.incrementClickCount();
            return bannerRepository.save(banner);
        }
        return null;
    }
    
    // Increment view count
    public Banner incrementViewCount(String id) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            Banner banner = optionalBanner.get();
            banner.incrementViewCount();
            return bannerRepository.save(banner);
        }
        return null;
    }
    
    // Update banner priority
    public Banner updateBannerPriority(String id, int priority) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            Banner banner = optionalBanner.get();
            banner.setPriority(priority);
            banner.setUpdatedAt(LocalDateTime.now());
            return bannerRepository.save(banner);
        }
        return null;
    }
    
    // Check if banner is valid
    public boolean isBannerValid(String id) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            return optionalBanner.get().isValid();
        }
        return false;
    }
    
    // Delete banner
    public boolean deleteBanner(String id) {
        if (bannerRepository.existsById(id)) {
            bannerRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Check if banner exists
    public boolean bannerExists(String id) {
        return bannerRepository.existsById(id);
    }
    
    // Get banner count by status
    public long getBannerCountByStatus(String status) {
        return bannerRepository.countByStatus(status);
    }
    
    // Get banner count by position
    public long getBannerCountByPosition(String position) {
        return bannerRepository.countByPosition(position);
    }
    
    // Get banners sorted by priority
    public List<Banner> getBannersOrderByPriority() {
        return bannerRepository.findAllOrderByPriorityAndCreatedAtDesc();
    }
    
    // Get banners sorted by click count
    public List<Banner> getBannersOrderByClickCount() {
        return bannerRepository.findAllOrderByClickCountDesc();
    }
    
    // Get banners sorted by view count
    public List<Banner> getBannersOrderByViewCount() {
        return bannerRepository.findAllOrderByViewCountDesc();
    }
    
    // Get banners sorted by creation date
    public List<Banner> getBannersOrderByCreatedAt() {
        return bannerRepository.findAllOrderByCreatedAtDesc();
    }
}
