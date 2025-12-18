package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.WalletTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends MongoRepository<WalletTransaction, String> {
    List<WalletTransaction> findByUserId(String userId);
    List<WalletTransaction> findByWalletId(String walletId);
    List<WalletTransaction> findByUserIdOrderByCreatedAtDesc(String userId);
    List<WalletTransaction> findByType(String type);
    List<WalletTransaction> findByStatus(String status);
    List<WalletTransaction> findByReferenceIdAndReferenceType(String referenceId, String referenceType);
}

