package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByName(String name);
    List<Product> findByCategory(String category);
    List<Product> findTop10ByOrderByCreatedAtDesc();
    List<Product> findByStatus(String status);
    List<Product> findByApprovalStatus(String approvalStatus);
    
    // Explicit query to ensure correct filtering by sellerId
    @Query("{'sellerId': ?0}")
    List<Product> findBySellerId(String sellerId);
}
