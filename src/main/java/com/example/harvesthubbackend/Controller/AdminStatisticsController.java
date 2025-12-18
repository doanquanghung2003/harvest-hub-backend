package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Service.AdminStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
@CrossOrigin(origins = "*")
public class AdminStatisticsController {
    
    @Autowired
    private AdminStatisticsService statisticsService;
    
    // Lấy thống kê tổng quan dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        return ResponseEntity.ok(statisticsService.getDashboardStatistics());
    }
    
    // Lấy thống kê doanh thu
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueStatistics(
            @RequestParam(defaultValue = "month") String period) {
        return ResponseEntity.ok(statisticsService.getRevenueStatistics(period));
    }
    
    // Lấy top sản phẩm bán chạy
    @GetMapping("/top-products")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getTopSellingProducts(limit));
    }
    
    // Lấy top seller
    @GetMapping("/top-sellers")
    public ResponseEntity<List<Map<String, Object>>> getTopSellers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getTopSellers(limit));
    }
}

