package com.example.harvesthubbackend.Config;

import com.example.harvesthubbackend.Service.VoucherAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class VoucherAutomationScheduler {
    
    @Autowired
    private VoucherAutomationService automationService;
    
    // Run daily at 2 AM to check for birthdays
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2 AM
    public void grantBirthdayVouchers() {
        System.out.println("Running birthday voucher automation...");
        automationService.grantBirthdayVouchers();
    }
    
    // Run every hour to check for expired vouchers and update status
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    public void updateExpiredVouchers() {
        System.out.println("Checking for expired vouchers...");
        // This can be implemented in VoucherService
        // For now, we'll handle it in the validation logic
    }
}

