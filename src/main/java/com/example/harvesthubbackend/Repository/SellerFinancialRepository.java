package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.SellerFinancial;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerFinancialRepository extends MongoRepository<SellerFinancial, String> {
    Optional<SellerFinancial> findBySellerId(String sellerId);
    Optional<SellerFinancial> findByUserId(String userId);
}

