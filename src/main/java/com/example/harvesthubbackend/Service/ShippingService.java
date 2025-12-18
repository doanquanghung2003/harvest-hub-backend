package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Shipping;
import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Repository.ShippingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ShippingService {
    
    @Autowired
    private ShippingRepository shippingRepository;
    
    @Autowired
    private OrderService orderService;
    
    // Tính phí vận chuyển dựa trên khoảng cách và trọng lượng
    public double calculateShippingFee(String city, String district, double weight, String shippingMethod) {
        // Logic tính phí đơn giản (có thể tích hợp API của GHN, GHTK sau)
        double baseFee = 20000; // Phí cơ bản
        double weightFee = weight * 5000; // 5000 VND/kg
        double distanceMultiplier = 1.0; // Có thể tính dựa trên khoảng cách thực tế
        
        double methodMultiplier = switch (shippingMethod != null ? shippingMethod.toLowerCase() : "standard") {
            case "express" -> 1.5;
            case "same_day" -> 2.0;
            default -> 1.0;
        };
        
        return (baseFee + weightFee) * distanceMultiplier * methodMultiplier;
    }
    
    // Tạo shipping cho đơn hàng
    public Shipping createShipping(String orderId, Shipping shipping) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        
        shipping.setOrderId(orderId);
        shipping.setUserId(order.getUserId());
        
        // Tính phí vận chuyển nếu chưa có
        if (shipping.getShippingFee() == 0.0) {
            // Giả sử trọng lượng 1kg, có thể lấy từ order items
            double weight = 1.0; // TODO: Tính từ order items
            double fee = calculateShippingFee(
                shipping.getCity(),
                shipping.getDistrict(),
                weight,
                shipping.getShippingMethod()
            );
            shipping.setShippingFee(fee);
            shipping.setTotalFee(fee + shipping.getInsuranceFee() + shipping.getCodFee());
        }
        
        shipping.setStatus("pending");
        shipping.setCreatedAt(LocalDateTime.now());
        shipping.setUpdatedAt(LocalDateTime.now());
        
        return shippingRepository.save(shipping);
    }
    
    // Cập nhật trạng thái vận chuyển
    public Shipping updateShippingStatus(String shippingId, String status, String location, String description) {
        Shipping shipping = shippingRepository.findById(shippingId)
            .orElseThrow(() -> new RuntimeException("Shipping not found"));
        
        shipping.setStatus(status);
        shipping.setUpdatedAt(LocalDateTime.now());
        
        // Thêm vào lịch sử tracking
        if (shipping.getTrackingHistory() == null) {
            shipping.setTrackingHistory(new ArrayList<>());
        }
        
        Shipping.TrackingEvent event = new Shipping.TrackingEvent(status, description, location);
        shipping.getTrackingHistory().add(event);
        
        // Cập nhật ngày giao hàng thực tế nếu đã giao
        if ("delivered".equals(status)) {
            shipping.setActualDeliveryDate(LocalDateTime.now());
            // Cập nhật trạng thái đơn hàng
            Order order = orderService.getById(shipping.getOrderId());
            if (order != null) {
                order.setStatus("delivered");
                orderService.update(shipping.getOrderId(), order);
            }
        }
        
        return shippingRepository.save(shipping);
    }
    
    // Thêm tracking event
    public Shipping addTrackingEvent(String shippingId, String status, String description, String location) {
        Shipping shipping = shippingRepository.findById(shippingId)
            .orElseThrow(() -> new RuntimeException("Shipping not found"));
        
        if (shipping.getTrackingHistory() == null) {
            shipping.setTrackingHistory(new ArrayList<>());
        }
        
        Shipping.TrackingEvent event = new Shipping.TrackingEvent(status, description, location);
        shipping.getTrackingHistory().add(event);
        shipping.setUpdatedAt(LocalDateTime.now());
        
        return shippingRepository.save(shipping);
    }
    
    // Lấy shipping theo order ID
    public List<Shipping> getShippingByOrderId(String orderId) {
        return shippingRepository.findByOrderId(orderId);
    }
    
    // Lấy shipping theo user ID
    public List<Shipping> getShippingByUserId(String userId) {
        return shippingRepository.findByUserId(userId);
    }
    
    // Lấy shipping theo seller ID
    public List<Shipping> getShippingBySellerId(String sellerId) {
        return shippingRepository.findBySellerId(sellerId);
    }
    
    // Lấy shipping theo tracking number
    public Optional<Shipping> getShippingByTrackingNumber(String trackingNumber) {
        return shippingRepository.findByTrackingNumber(trackingNumber);
    }
    
    // Lấy tất cả shipping
    public List<Shipping> getAll() {
        return shippingRepository.findAll();
    }
    
    // Lấy shipping theo ID
    public Optional<Shipping> getById(String id) {
        return shippingRepository.findById(id);
    }
    
    // Cập nhật shipping
    public Shipping update(String id, Shipping shipping) {
        shipping.setId(id);
        shipping.setUpdatedAt(LocalDateTime.now());
        return shippingRepository.save(shipping);
    }
    
    // Xóa shipping
    public void delete(String id) {
        shippingRepository.deleteById(id);
    }
    
    // Lấy shipping theo trạng thái
    public List<Shipping> getByStatus(String status) {
        return shippingRepository.findByStatus(status);
    }
    
    // Tính toán ngày giao hàng ước tính
    public LocalDateTime calculateEstimatedDeliveryDate(String shippingMethod, String city) {
        int days = switch (shippingMethod != null ? shippingMethod.toLowerCase() : "standard") {
            case "same_day" -> 0;
            case "express" -> 1;
            default -> 3;
        };
        
        return LocalDateTime.now().plusDays(days);
    }
}

