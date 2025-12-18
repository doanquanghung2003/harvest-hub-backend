package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByName(String name);
    List<Product> findByCategory(String category);
    List<Product> findTop10ByOrderByCreatedAtDesc();
    List<Product> findByStatus(String status);
    List<Product> findByApprovalStatus(String approvalStatus);
    List<Product> findBySellerId(String sellerId);
}
