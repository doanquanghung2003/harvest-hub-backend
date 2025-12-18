package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Follow;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends MongoRepository<Follow, String> {
    
    // Tìm follow theo userId và shopId
    Optional<Follow> findByUserIdAndShopId(String userId, String shopId);
    
    // Kiểm tra xem user đã follow shop chưa
    boolean existsByUserIdAndShopId(String userId, String shopId);
    
    // Lấy tất cả shop mà user đang follow
    List<Follow> findByUserId(String userId);
    
    // Lấy tất cả user đang follow shop
    List<Follow> findByShopId(String shopId);
    
    // Đếm số lượng follower của shop
    long countByShopId(String shopId);
    
    // Đếm số lượng shop mà user đang follow
    long countByUserId(String userId);
    
    // Xóa follow
    void deleteByUserIdAndShopId(String userId, String shopId);
}
