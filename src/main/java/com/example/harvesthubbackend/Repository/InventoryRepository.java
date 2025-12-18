package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<Inventory, String> {
    Optional<Inventory> findByProductId(String productId);
    List<Inventory> findBySellerId(String sellerId);
    List<Inventory> findByStatus(String status);
    List<Inventory> findByWarehouseId(String warehouseId);
    List<Inventory> findByLowStockAlertTrue();
}

