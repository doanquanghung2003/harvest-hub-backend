package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.InventoryTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends MongoRepository<InventoryTransaction, String> {
    List<InventoryTransaction> findByInventoryId(String inventoryId);
    List<InventoryTransaction> findByProductId(String productId);
    List<InventoryTransaction> findBySellerId(String sellerId);
    List<InventoryTransaction> findByType(String type);
    List<InventoryTransaction> findByOrderId(String orderId);
}

