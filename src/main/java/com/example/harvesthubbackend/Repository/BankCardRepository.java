package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.BankCard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends MongoRepository<BankCard, String> {
    List<BankCard> findByUserId(String userId);
    List<BankCard> findByUserIdAndStatus(String userId, String status);
    Optional<BankCard> findByUserIdAndIsDefaultTrue(String userId);
    Optional<BankCard> findByIdAndUserId(String id, String userId);
    boolean existsByIdAndUserId(String id, String userId);
}

