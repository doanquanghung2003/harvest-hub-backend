package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Shipping;
import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Models.Product;
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
    
    @Autowired
    private ProductService productService;
    
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
            throw new RuntimeException("Không tìm thấy đơn hàng");
        }
        
        shipping.setOrderId(orderId);
        shipping.setUserId(order.getUserId());
        
        // Tính phí vận chuyển nếu chưa có
        if (shipping.getShippingFee() == 0.0) {
            // Tính trọng lượng từ order items
            double weight = calculateOrderWeight(order);
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
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin vận chuyển"));
        
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
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin vận chuyển"));
        
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
    
    // Tính trọng lượng từ order items
    private double calculateOrderWeight(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return 1.0; // Default weight if no items
        }
        
        double totalWeight = 0.0;
        for (Order.OrderItem item : order.getItems()) {
            try {
                Product product = productService.getById(item.getProductId());
                if (product != null && product.getWeight() != null && !product.getWeight().isEmpty()) {
                    // Parse weight string (e.g., "1.5kg", "500g", "1.5")
                    double itemWeight = parseWeight(product.getWeight());
                    totalWeight += itemWeight * item.getQuantity();
                } else {
                    // Default weight per item if not specified
                    totalWeight += 1.0 * item.getQuantity();
                }
            } catch (Exception e) {
                // If product not found or error parsing, use default weight
                totalWeight += 1.0 * item.getQuantity();
            }
        }
        
        // Minimum weight is 0.5kg
        return Math.max(0.5, totalWeight);
    }
    
    // Parse weight string to double (in kg)
    private double parseWeight(String weightStr) {
        if (weightStr == null || weightStr.isEmpty()) {
            return 1.0; // Default weight
        }
        
        try {
            // Remove spaces and convert to lowercase
            String cleaned = weightStr.trim().toLowerCase().replaceAll("\\s+", "");
            
            // Check for "kg" or "g" suffix
            if (cleaned.endsWith("kg")) {
                String numStr = cleaned.substring(0, cleaned.length() - 2);
                return Double.parseDouble(numStr);
            } else if (cleaned.endsWith("g")) {
                String numStr = cleaned.substring(0, cleaned.length() - 1);
                return Double.parseDouble(numStr) / 1000.0; // Convert grams to kg
            } else {
                // Try to parse as number directly (assume kg)
                return Double.parseDouble(cleaned);
            }
        } catch (NumberFormatException e) {
            // If parsing fails, return default weight
            return 1.0;
        }
    }
}

