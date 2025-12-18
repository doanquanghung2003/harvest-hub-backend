package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Voucher;
import com.example.harvesthubbackend.Models.VoucherUsage;
import com.example.harvesthubbackend.Repository.VoucherRepository;
import com.example.harvesthubbackend.Repository.VoucherUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoucherStatisticsService {
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private VoucherUsageRepository voucherUsageRepository;
    
    // Get comprehensive voucher statistics
    public Map<String, Object> getVoucherStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Voucher> allVouchers = voucherRepository.findAll();
        
        // Basic counts
        stats.put("totalVouchers", allVouchers.size());
        stats.put("activeVouchers", voucherRepository.countByStatus("active"));
        stats.put("inactiveVouchers", voucherRepository.countByStatus("inactive"));
        stats.put("expiredVouchers", voucherRepository.countByStatus("expired"));
        stats.put("platformVouchers", voucherRepository.findPlatformVouchers().size());
        
        // Vouchers by type
        Map<String, Long> byType = new HashMap<>();
        byType.put("percentage", (long) voucherRepository.findByType("percentage").size());
        byType.put("fixed_amount", (long) voucherRepository.findByType("fixed_amount").size());
        byType.put("free_shipping", (long) voucherRepository.findByType("free_shipping").size());
        stats.put("vouchersByType", byType);
        
        // Total usage
        int totalUsage = allVouchers.stream()
            .mapToInt(Voucher::getUsedCount)
            .sum();
        stats.put("totalUsage", totalUsage);
        
        // Total discount given (from voucher usages)
        List<VoucherUsage> allUsages = voucherUsageRepository.findAll();
        double totalDiscount = allUsages.stream()
            .filter(u -> "used".equals(u.getStatus()))
            .mapToDouble(VoucherUsage::getDiscountAmount)
            .sum();
        stats.put("totalDiscountGiven", totalDiscount);
        
        // Most used vouchers
        List<Map<String, Object>> mostUsed = allVouchers.stream()
            .filter(v -> v.getUsedCount() > 0)
            .sorted((a, b) -> Integer.compare(b.getUsedCount(), a.getUsedCount()))
            .limit(10)
            .map(v -> {
                Map<String, Object> voucherStat = new HashMap<>();
                voucherStat.put("id", v.getId());
                voucherStat.put("code", v.getCode());
                voucherStat.put("name", v.getName());
                voucherStat.put("usedCount", v.getUsedCount());
                voucherStat.put("usageLimit", v.getUsageLimit());
                return voucherStat;
            })
            .collect(Collectors.toList());
        stats.put("mostUsedVouchers", mostUsed);
        
        // Vouchers expiring in next 7 days
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekFromNow = now.plusDays(7);
        long expiringSoon = allVouchers.stream()
            .filter(v -> {
                LocalDateTime endDate = v.getEndDate();
                return endDate != null && 
                       endDate.isAfter(now) && 
                       endDate.isBefore(weekFromNow) &&
                       "active".equals(v.getStatus());
            })
            .count();
        stats.put("expiringSoon", expiringSoon);
        
        // Usage by month (last 6 months)
        Map<String, Long> usageByMonth = new HashMap<>();
        LocalDateTime sixMonthsAgo = now.minusMonths(6);
        List<VoucherUsage> recentUsages = voucherUsageRepository.findAll().stream()
            .filter(u -> u.getUsedAt() != null && u.getUsedAt().isAfter(sixMonthsAgo))
            .collect(Collectors.toList());
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            String monthKey = (monthStart.getMonth() != null ? monthStart.getMonth().toString() : "") + " " + monthStart.getYear();
            
            long count = recentUsages.stream()
                .filter(u -> {
                    LocalDateTime usedAt = u.getUsedAt();
                    return usedAt != null && 
                           usedAt.isAfter(monthStart) && 
                           usedAt.isBefore(monthEnd);
                })
                .count();
            usageByMonth.put(monthKey, count);
        }
        stats.put("usageByMonth", usageByMonth);
        
        return stats;
    }
    
    // Get shop voucher statistics
    public Map<String, Object> getShopVoucherStatistics(String shopId) {
        Map<String, Object> stats = new HashMap<>();
        List<Voucher> shopVouchers = voucherRepository.findByShopId(shopId);
        
        stats.put("totalVouchers", shopVouchers.size());
        
        long activeCount = shopVouchers.stream()
            .filter(v -> "active".equals(v.getStatus()))
            .count();
        stats.put("activeVouchers", activeCount);
        
        long inactiveCount = shopVouchers.stream()
            .filter(v -> "inactive".equals(v.getStatus()))
            .count();
        stats.put("inactiveVouchers", inactiveCount);
        
        long expiredCount = shopVouchers.stream()
            .filter(v -> "expired".equals(v.getStatus()))
            .count();
        stats.put("expiredVouchers", expiredCount);
        
        // Total usage
        int totalUsage = shopVouchers.stream()
            .mapToInt(Voucher::getUsedCount)
            .sum();
        stats.put("totalUsage", totalUsage);
        
        // Total discount given
        List<VoucherUsage> shopUsages = voucherUsageRepository.findAll().stream()
            .filter(u -> {
                Optional<Voucher> voucherOpt = voucherRepository.findById(u.getVoucherId());
                if (!voucherOpt.isPresent()) return false;
                Voucher v = voucherOpt.get();
                return v.getShopId() != null && shopId.equals(v.getShopId());
            })
            .collect(Collectors.toList());
        
        double totalDiscount = shopUsages.stream()
            .filter(u -> "used".equals(u.getStatus()))
            .mapToDouble(VoucherUsage::getDiscountAmount)
            .sum();
        stats.put("totalDiscountGiven", totalDiscount);
        
        // Vouchers by type
        Map<String, Long> byType = new HashMap<>();
        byType.put("percentage", 
            shopVouchers.stream().filter(v -> "percentage".equals(v.getType())).count());
        byType.put("fixed_amount", 
            shopVouchers.stream().filter(v -> "fixed_amount".equals(v.getType())).count());
        byType.put("free_shipping", 
            shopVouchers.stream().filter(v -> "free_shipping".equals(v.getType())).count());
        stats.put("vouchersByType", byType);
        
        return stats;
    }
}

