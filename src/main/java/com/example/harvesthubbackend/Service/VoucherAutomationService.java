package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.*;
import com.example.harvesthubbackend.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VoucherAutomationService {
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserVoucherRepository userVoucherRepository;
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    // Note: UserRepository and OrderRepository are inner interfaces in Service classes
    // We'll use UserService and OrderService methods instead
    
    @Autowired
    private NotificationService notificationService;
    
    // Note: We'll use service methods instead of direct repository access
    // UserRepository and OrderRepository are inner interfaces in Service classes
    
    // Welcome voucher for new users
    public void grantWelcomeVoucher(String userId) {
        try {
            // Find welcome voucher template (you can create a special voucher with code "WELCOME")
            Optional<Voucher> welcomeVoucherOpt = voucherRepository.findByCode("WELCOME");
            if (!welcomeVoucherOpt.isPresent()) {
                // Create default welcome voucher if not exists
                Voucher welcomeVoucher = createDefaultWelcomeVoucher();
                welcomeVoucherOpt = Optional.of(welcomeVoucher);
            }
            
            Voucher welcomeVoucher = welcomeVoucherOpt.get();
            
            // Check if user already received welcome voucher
            Optional<UserVoucher> existing = userVoucherRepository.findByUserIdAndVoucherId(userId, welcomeVoucher.getId());
            if (existing.isPresent()) {
                return; // Already granted
            }
            
            // Grant voucher
            voucherService.grantVoucherToUser(userId, welcomeVoucher.getId());
            
            // Send notification
            User user = userService.getById(userId);
            if (user != null) {
                notificationService.pushNotification(
                    userId,
                    "Chào mừng bạn đến với Harvest Hub!",
                    "Bạn đã nhận được voucher chào mừng: " + welcomeVoucher.getCode() + ". Hãy sử dụng ngay!",
                    "VOUCHER"
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to grant welcome voucher: " + e.getMessage());
        }
    }
    
    // Birthday voucher
    public void grantBirthdayVouchers() {
        try {
            // Find birthday voucher template
            Optional<Voucher> birthdayVoucherOpt = voucherRepository.findByCode("BIRTHDAY");
            if (!birthdayVoucherOpt.isPresent()) {
                // Create default birthday voucher if not exists
                Voucher birthdayVoucher = createDefaultBirthdayVoucher();
                birthdayVoucherOpt = Optional.of(birthdayVoucher);
            }
            
            // Note: Birthday voucher automation requires birthday field in User model
            // For now, this is a placeholder - implement when birthday field is added
            // You can manually trigger birthday vouchers via API endpoint
            // When birthday field is added, iterate through users and check if birthday matches today
            // Example implementation (when birthday field exists):
            // Voucher birthdayVoucher = birthdayVoucherOpt.get();
            // List<User> allUsers = userService.getAllUsers();
            // for (User user : allUsers) {
            //     if (user.getBirthday() != null && isBirthdayToday(user.getBirthday())) {
            //         grantBirthdayVoucherToUser(user.getId(), birthdayVoucher.getId());
            //     }
            // }
        } catch (Exception e) {
            System.err.println("Failed to grant birthday vouchers: " + e.getMessage());
        }
    }
    
    // Helper method for birthday voucher (will be used when birthday field is added)
    @SuppressWarnings("unused")
    private void grantBirthdayVoucherToUser(String userId, String voucherId) {
        try {
            // Check if already granted this year
            Optional<UserVoucher> existing = userVoucherRepository.findByUserIdAndVoucherId(userId, voucherId);
            if (existing.isPresent()) {
                // Check if granted this year
                UserVoucher uv = existing.get();
                if (uv.getReceivedAt().getYear() == LocalDateTime.now().getYear()) {
                    return; // Already granted this year
                }
            }
            
            // Grant voucher
            voucherService.grantVoucherToUser(userId, voucherId);
            
            // Send notification
            notificationService.pushNotification(
                userId,
                "Chúc mừng sinh nhật!",
                "Bạn đã nhận được voucher sinh nhật đặc biệt! Hãy sử dụng ngay!",
                "VOUCHER"
            );
        } catch (Exception e) {
            System.err.println("Failed to grant birthday voucher to user " + userId + ": " + e.getMessage());
        }
    }
    
    // Purchase reward voucher (after order delivered)
    public void grantPurchaseRewardVoucher(String userId, String orderId) {
        try {
            // Find purchase reward voucher template
            Optional<Voucher> rewardVoucherOpt = voucherRepository.findByCode("PURCHASE_REWARD");
            if (!rewardVoucherOpt.isPresent()) {
                // Create default purchase reward voucher if not exists
                Voucher rewardVoucher = createDefaultPurchaseRewardVoucher();
                rewardVoucherOpt = Optional.of(rewardVoucher);
            }
            
            Voucher rewardVoucher = rewardVoucherOpt.get();
            
            // Check if user already received reward for this order
            Optional<UserVoucher> existing = userVoucherRepository.findByOrderId(orderId);
            if (existing.isPresent()) {
                return; // Already granted
            }
            
            // Grant voucher
            UserVoucher userVoucher = voucherService.grantVoucherToUser(userId, rewardVoucher.getId());
            userVoucher.setOrderId(orderId);
            userVoucherRepository.save(userVoucher);
            
            // Send notification
            notificationService.pushNotification(
                userId,
                "Cảm ơn bạn đã mua hàng!",
                "Bạn đã nhận được voucher cảm ơn: " + rewardVoucher.getCode() + ". Hãy sử dụng cho đơn hàng tiếp theo!",
                "VOUCHER"
            );
        } catch (Exception e) {
            System.err.println("Failed to grant purchase reward voucher: " + e.getMessage());
        }
    }
    
    // Referral voucher (when user refers someone)
    public void grantReferralVoucher(String referrerUserId, String referredUserId) {
        try {
            // Find referral voucher template
            Optional<Voucher> referralVoucherOpt = voucherRepository.findByCode("REFERRAL");
            if (!referralVoucherOpt.isPresent()) {
                // Create default referral voucher if not exists
                Voucher referralVoucher = createDefaultReferralVoucher();
                referralVoucherOpt = Optional.of(referralVoucher);
            }
            
            Voucher referralVoucher = referralVoucherOpt.get();
            
            // Grant voucher to referrer
            voucherService.grantVoucherToUser(referrerUserId, referralVoucher.getId());
            
            // Send notification
            notificationService.pushNotification(
                referrerUserId,
                "Bạn đã giới thiệu thành công!",
                "Bạn đã nhận được voucher giới thiệu: " + referralVoucher.getCode(),
                "VOUCHER"
            );
        } catch (Exception e) {
            System.err.println("Failed to grant referral voucher: " + e.getMessage());
        }
    }
    
    // Create default vouchers
    private Voucher createDefaultWelcomeVoucher() {
        Voucher voucher = new Voucher();
        voucher.setCode("WELCOME");
        voucher.setName("Voucher chào mừng");
        voucher.setDescription("Voucher dành cho thành viên mới");
        voucher.setType("percentage");
        voucher.setValue(10); // 10% off
        voucher.setMinOrderAmount(100000); // 100k minimum
        voucher.setMaxDiscountAmount(50000); // Max 50k
        voucher.setUsageLimit(1); // One time use
        voucher.setMaxUsagePerUser(1);
        voucher.setStartDate(LocalDateTime.now());
        voucher.setEndDate(LocalDateTime.now().plusMonths(1)); // Valid for 1 month
        voucher.setStatus("active");
        voucher.setShopId(null); // Platform voucher
        return voucherRepository.save(voucher);
    }
    
    private Voucher createDefaultBirthdayVoucher() {
        Voucher voucher = new Voucher();
        voucher.setCode("BIRTHDAY");
        voucher.setName("Voucher sinh nhật");
        voucher.setDescription("Voucher đặc biệt cho ngày sinh nhật của bạn");
        voucher.setType("percentage");
        voucher.setValue(20); // 20% off
        voucher.setMinOrderAmount(200000); // 200k minimum
        voucher.setMaxDiscountAmount(100000); // Max 100k
        voucher.setUsageLimit(-1); // Unlimited
        voucher.setMaxUsagePerUser(1); // One per user per year
        voucher.setStartDate(LocalDateTime.now());
        voucher.setEndDate(LocalDateTime.now().plusYears(10)); // Long validity
        voucher.setStatus("active");
        voucher.setShopId(null); // Platform voucher
        return voucherRepository.save(voucher);
    }
    
    private Voucher createDefaultPurchaseRewardVoucher() {
        Voucher voucher = new Voucher();
        voucher.setCode("PURCHASE_REWARD");
        voucher.setName("Voucher cảm ơn");
        voucher.setDescription("Cảm ơn bạn đã mua hàng!");
        voucher.setType("fixed_amount");
        voucher.setValue(50000); // 50k off
        voucher.setMinOrderAmount(300000); // 300k minimum
        voucher.setUsageLimit(-1); // Unlimited
        voucher.setMaxUsagePerUser(-1); // Unlimited per user
        voucher.setStartDate(LocalDateTime.now());
        voucher.setEndDate(LocalDateTime.now().plusMonths(3)); // Valid for 3 months
        voucher.setStatus("active");
        voucher.setShopId(null); // Platform voucher
        return voucherRepository.save(voucher);
    }
    
    private Voucher createDefaultReferralVoucher() {
        Voucher voucher = new Voucher();
        voucher.setCode("REFERRAL");
        voucher.setName("Voucher giới thiệu");
        voucher.setDescription("Voucher dành cho người giới thiệu");
        voucher.setType("fixed_amount");
        voucher.setValue(100000); // 100k off
        voucher.setMinOrderAmount(500000); // 500k minimum
        voucher.setUsageLimit(-1); // Unlimited
        voucher.setMaxUsagePerUser(10); // Max 10 times per user
        voucher.setStartDate(LocalDateTime.now());
        voucher.setEndDate(LocalDateTime.now().plusMonths(6)); // Valid for 6 months
        voucher.setStatus("active");
        voucher.setShopId(null); // Platform voucher
        return voucherRepository.save(voucher);
    }
}

