package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AdminStatisticsService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private SellerService sellerService;
    
    // Lấy thống kê tổng quan
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Thống kê user
        List<User> allUsers = userService.getAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isEnabled).count();
        long adminUsers = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long sellerUsers = allUsers.stream().filter(u -> "SELLER".equals(u.getRole())).count();
        long buyerUsers = allUsers.stream().filter(u -> "USER".equals(u.getRole()) || u.getRole() == null).count();
        
        // Thống kê sản phẩm
        List<Product> allProducts = productService.getAll();
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream().filter(p -> "active".equals(p.getStatus())).count();
        long pendingProducts = allProducts.stream().filter(p -> "pending".equals(p.getStatus())).count();
        long outOfStockProducts = allProducts.stream().filter(p -> p.getStock() != null && p.getStock() == 0).count();
        
        // Thống kê đơn hàng
        List<Order> allOrders = orderService.getAll();
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream().filter(o -> "pending".equals(o.getStatus())).count();
        long processingOrders = allOrders.stream().filter(o -> "processing".equals(o.getStatus())).count();
        long shippingOrders = allOrders.stream().filter(o -> "shipping".equals(o.getStatus())).count();
        long deliveredOrders = allOrders.stream().filter(o -> "delivered".equals(o.getStatus())).count();
        long cancelledOrders = allOrders.stream().filter(o -> "cancelled".equals(o.getStatus())).count();
        
        // Tính tổng doanh thu
        double totalRevenue = allOrders.stream()
            .filter(o -> "delivered".equals(o.getStatus()))
            .mapToDouble(Order::getTotalPrice)
            .sum();
        
        // Thống kê seller
        List<Seller> allSellers = sellerService.getAllSellers();
        long totalSellers = allSellers.size();
        long activeSellers = allSellers.stream().filter(s -> "active".equals(s.getStatus())).count();
        long pendingSellers = allSellers.stream().filter(s -> "pending".equals(s.getStatus())).count();
        
        // Thống kê theo thời gian (7 ngày qua)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        long newUsersLast7Days = allUsers.stream()
            .filter(u -> u.getPasswordChangedAt() != null && u.getPasswordChangedAt().isAfter(sevenDaysAgo))
            .count();
        
        long newOrdersLast7Days = allOrders.stream()
            .filter(o -> {
                long createdAt = o.getCreatedAt();
                LocalDateTime orderDate = new java.util.Date(createdAt).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                return orderDate.isAfter(sevenDaysAgo);
            })
            .count();
        
        double revenueLast7Days = allOrders.stream()
            .filter(o -> {
                long createdAt = o.getCreatedAt();
                LocalDateTime orderDate = new java.util.Date(createdAt).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                return orderDate.isAfter(sevenDaysAgo) && "delivered".equals(o.getStatus());
            })
            .mapToDouble(Order::getTotalPrice)
            .sum();
        
        // Tổng hợp
        stats.put("users", Map.of(
            "total", totalUsers,
            "active", activeUsers,
            "admins", adminUsers,
            "sellers", sellerUsers,
            "buyers", buyerUsers,
            "newLast7Days", newUsersLast7Days
        ));
        
        stats.put("products", Map.of(
            "total", totalProducts,
            "active", activeProducts,
            "pending", pendingProducts,
            "outOfStock", outOfStockProducts
        ));
        
        stats.put("orders", Map.of(
            "total", totalOrders,
            "pending", pendingOrders,
            "processing", processingOrders,
            "shipping", shippingOrders,
            "delivered", deliveredOrders,
            "cancelled", cancelledOrders,
            "newLast7Days", newOrdersLast7Days
        ));
        
        stats.put("revenue", Map.of(
            "total", totalRevenue,
            "last7Days", revenueLast7Days
        ));
        
        stats.put("sellers", Map.of(
            "total", totalSellers,
            "active", activeSellers,
            "pending", pendingSellers
        ));
        
        stats.put("timestamp", LocalDateTime.now().toString());
        
        return stats;
    }
    
    // Lấy thống kê doanh thu theo thời gian
    public Map<String, Object> getRevenueStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        List<Order> allOrders = orderService.getAll();
        
        LocalDateTime startDate;
        switch (period != null ? period.toLowerCase() : "month") {
            case "day":
                startDate = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
                break;
            case "week":
                startDate = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
                break;
            case "month":
                startDate = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
                break;
            case "year":
                startDate = LocalDateTime.now().minus(365, ChronoUnit.DAYS);
                break;
            default:
                startDate = LocalDateTime.now().minus(30, ChronoUnit.DAYS);
        }
        
        double revenue = allOrders.stream()
            .filter(o -> {
                long createdAt = o.getCreatedAt();
                LocalDateTime orderDate = new java.util.Date(createdAt).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                return orderDate.isAfter(startDate) && "delivered".equals(o.getStatus());
            })
            .mapToDouble(Order::getTotalPrice)
            .sum();
        
        long orderCount = allOrders.stream()
            .filter(o -> {
                long createdAt = o.getCreatedAt();
                LocalDateTime orderDate = new java.util.Date(createdAt).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                return orderDate.isAfter(startDate);
            })
            .count();
        
        stats.put("period", period);
        stats.put("revenue", revenue);
        stats.put("orderCount", orderCount);
        stats.put("averageOrderValue", orderCount > 0 ? revenue / orderCount : 0);
        stats.put("startDate", startDate.toString());
        stats.put("endDate", LocalDateTime.now().toString());
        
        return stats;
    }
    
    // Lấy top sản phẩm bán chạy
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Product> products = productService.getAll();
        
        return products.stream()
            .filter(p -> p.getSoldCount() > 0)
            .sorted((p1, p2) -> Integer.compare(p2.getSoldCount(), p1.getSoldCount()))
            .limit(limit)
            .map(p -> {
                Map<String, Object> productStats = new HashMap<>();
                productStats.put("productId", p.getId());
                productStats.put("productName", p.getName());
                productStats.put("soldCount", p.getSoldCount());
                productStats.put("revenue", p.getSoldCount() * (p.getPrice() != null ? p.getPrice() : 0));
                productStats.put("rating", p.getRating());
                return productStats;
            })
            .toList();
    }
    
    // Lấy top seller
    public List<Map<String, Object>> getTopSellers(int limit) {
        List<Seller> sellers = sellerService.getAllSellers();
        
        return sellers.stream()
            .filter(s -> s.getTotalRevenue() > 0)
            .sorted((s1, s2) -> Double.compare(s2.getTotalRevenue(), s1.getTotalRevenue()))
            .limit(limit)
            .map(s -> {
                Map<String, Object> sellerStats = new HashMap<>();
                sellerStats.put("sellerId", s.getId());
                sellerStats.put("sellerName", s.getBusinessName());
                sellerStats.put("totalRevenue", s.getTotalRevenue());
                sellerStats.put("totalOrders", s.getTotalOrders());
                sellerStats.put("averageRating", s.getAverageRating());
                return sellerStats;
            })
            .toList();
    }
}

