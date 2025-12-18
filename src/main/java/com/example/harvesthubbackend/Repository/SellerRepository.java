package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Seller;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends MongoRepository<Seller, String> {
    
    // Tìm seller theo status
    List<Seller> findByStatus(String status);
    
    // Tìm seller theo userId
    Optional<Seller> findByUserId(String userId);
    
    // Tìm seller đã được verify
    List<Seller> findByIsVerifiedTrue();
    
    // Tìm seller chưa được verify
    List<Seller> findByIsVerifiedFalse();
    
    // Tìm seller theo business name (tìm kiếm gần đúng)
    List<Seller> findByBusinessNameContainingIgnoreCase(String businessName);
    
    // Tìm seller theo email
    List<Seller> findByEmail(String email);
    
    // Tìm seller theo phone
    List<Seller> findByPhone(String phone);
    
    // Tìm seller theo city
    List<Seller> findByCity(String city);
    
    // Tìm seller theo province
    List<Seller> findByProvince(String province);
    
    // Tìm seller theo farm type
    List<Seller> findByFarmType(String farmType);
    
    // Tìm seller có certification cụ thể
    List<Seller> findByCertificationsContaining(String certification);
    
    // Đếm số lượng seller theo status
    long countByStatus(String status);
    
    // Đếm số lượng seller đã verify
    long countByIsVerifiedTrue();
    
    // Đếm số lượng seller chưa verify
    long countByIsVerifiedFalse();
}
