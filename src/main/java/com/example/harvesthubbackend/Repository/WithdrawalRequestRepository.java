package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.WithdrawalRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalRequestRepository extends MongoRepository<WithdrawalRequest, String> {
    List<WithdrawalRequest> findBySellerId(String sellerId);
    List<WithdrawalRequest> findByStatus(String status);
}

