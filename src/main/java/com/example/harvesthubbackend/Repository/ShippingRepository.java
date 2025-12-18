package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Shipping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRepository extends MongoRepository<Shipping, String> {
    List<Shipping> findByOrderId(String orderId);
    List<Shipping> findByUserId(String userId);
    List<Shipping> findBySellerId(String sellerId);
    List<Shipping> findByStatus(String status);
    Optional<Shipping> findByTrackingNumber(String trackingNumber);
    List<Shipping> findByShippingProvider(String shippingProvider);
}

