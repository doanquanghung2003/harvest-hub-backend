package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Seller;
import com.example.harvesthubbackend.Repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private UserService userService;

    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    public List<Seller> getPendingSellers() {
        return sellerRepository.findByStatus("pending");
    }

    public Seller getSellerById(String id) {
        Optional<Seller> seller = sellerRepository.findById(id);
        return seller.orElse(null);
    }

    public Seller getSellerByUserId(String userId) {
        return sellerRepository.findByUserId(userId).orElse(null);
    }

    public Seller createSeller(Seller seller) {
        // Set default values
        seller.setStatus("pending");
        seller.setVerified(false);
        seller.setCreatedAt(LocalDateTime.now());
        seller.setUpdatedAt(LocalDateTime.now());
        seller.setLastActiveAt(LocalDateTime.now());
        
        return sellerRepository.save(seller);
    }

    public Seller approveSeller(String id, String adminId) {
        Optional<Seller> sellerOpt = sellerRepository.findById(id);
        if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            seller.setStatus("active");
            seller.setVerified(true);
            seller.setVerifiedAt(LocalDateTime.now());
            seller.setVerifiedBy(adminId);
            seller.setUpdatedAt(LocalDateTime.now());
            // Cập nhật vai trò user thành SELLER nếu có userId
            if (seller.getUserId() != null && !seller.getUserId().isEmpty()) {
                try {
                    System.out.println("🔍 Updating user role for userId: " + seller.getUserId());
                    com.example.harvesthubbackend.Models.User user = userService.getById(seller.getUserId());
                    if (user != null) {
                        System.out.println("✅ Found user: " + user.getUsername() + " with current role: " + user.getRole());
                        com.example.harvesthubbackend.Models.User update = new com.example.harvesthubbackend.Models.User();
                        update.setRole("SELLER");
                        userService.update(user.getId(), update);
                        System.out.println("✅ Updated user role to SELLER");
                    } else {
                        System.out.println("❌ User not found with ID: " + seller.getUserId());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error updating user role: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ Seller has no userId set: " + seller.getId());
            }
            return sellerRepository.save(seller);
        }
        throw new RuntimeException("Không tìm thấy seller với ID: " + id);
    }

    public Seller rejectSeller(String id, String adminId, String reason) {
        Optional<Seller> sellerOpt = sellerRepository.findById(id);
        if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            seller.setStatus("rejected");
            seller.setVerified(false);
            seller.setUpdatedAt(LocalDateTime.now());
            // Có thể lưu lý do từ chối vào description hoặc tạo field mới
            if (reason != null && !reason.isEmpty()) {
                seller.setDescription(seller.getDescription() + "\nLý do từ chối: " + reason);
            }
            return sellerRepository.save(seller);
        }
        throw new RuntimeException("Không tìm thấy seller với ID: " + id);
    }

    public Map<String, Object> getSellerStats() {
        List<Seller> allSellers = sellerRepository.findAll();
        List<Seller> pendingSellers = sellerRepository.findByStatus("pending");
        List<Seller> activeSellers = sellerRepository.findByStatus("active");
        List<Seller> rejectedSellers = sellerRepository.findByStatus("rejected");
        
        // Tính toán thống kê tuần này (giả sử)
        long approvedThisWeek = activeSellers.stream()
            .filter(seller -> seller.getVerifiedAt() != null && 
                    seller.getVerifiedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
            .count();
            
        long rejectedThisWeek = rejectedSellers.stream()
            .filter(seller -> seller.getUpdatedAt() != null && 
                    seller.getUpdatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
            .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allSellers.size());
        stats.put("pending", pendingSellers.size());
        stats.put("active", activeSellers.size());
        stats.put("rejected", rejectedSellers.size());
        stats.put("approvedThisWeek", approvedThisWeek);
        stats.put("rejectedThisWeek", rejectedThisWeek);
        
        return stats;
    }

    public Seller updateSeller(String id, Seller sellerDetails) {
        Optional<Seller> sellerOpt = sellerRepository.findById(id);
        if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            
            // Update fields
            if (sellerDetails.getBusinessName() != null) {
                seller.setBusinessName(sellerDetails.getBusinessName());
            }
            if (sellerDetails.getContactPerson() != null) {
                seller.setContactPerson(sellerDetails.getContactPerson());
            }
            if (sellerDetails.getPhone() != null) {
                seller.setPhone(sellerDetails.getPhone());
            }
            if (sellerDetails.getEmail() != null) {
                seller.setEmail(sellerDetails.getEmail());
            }
            if (sellerDetails.getAddress() != null) {
                seller.setAddress(sellerDetails.getAddress());
            }
            if (sellerDetails.getDescription() != null) {
                seller.setDescription(sellerDetails.getDescription());
            }
            if (sellerDetails.getWard() != null) {
                seller.setWard(sellerDetails.getWard());
            }
            if (sellerDetails.getDistrict() != null) {
                seller.setDistrict(sellerDetails.getDistrict());
            }
            if (sellerDetails.getCity() != null) {
                seller.setCity(sellerDetails.getCity());
            }
            if (sellerDetails.getProvince() != null) {
                seller.setProvince(sellerDetails.getProvince());
            }
            
            seller.setUpdatedAt(LocalDateTime.now());
            return sellerRepository.save(seller);
        }
        throw new RuntimeException("Không tìm thấy seller với ID: " + id);
    }

    public void deleteSeller(String id) {
        if (sellerRepository.existsById(id)) {
            sellerRepository.deleteById(id);
        } else {
            throw new RuntimeException("Không tìm thấy seller với ID: " + id);
        }
    }
}
