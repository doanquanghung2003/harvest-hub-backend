package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.SellerFinancial;
import com.example.harvesthubbackend.Models.WithdrawalRequest;
import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Repository.SellerFinancialRepository;
import com.example.harvesthubbackend.Repository.WithdrawalRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SellerFinancialService {
    
    @Autowired
    private SellerFinancialRepository financialRepository;
    
    @Autowired
    private WithdrawalRequestRepository withdrawalRepository;
    
    // Tạo hoặc lấy financial record cho seller
    public SellerFinancial getOrCreateFinancial(String sellerId, String userId) {
        Optional<SellerFinancial> existing = financialRepository.findBySellerId(sellerId);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        SellerFinancial financial = new SellerFinancial();
        financial.setSellerId(sellerId);
        financial.setUserId(userId);
        financial.setCreatedAt(LocalDateTime.now());
        financial.setUpdatedAt(LocalDateTime.now());
        
        return financialRepository.save(financial);
    }
    
    // Cập nhật doanh thu khi đơn hàng hoàn thành
    @Transactional
    public void updateRevenueFromOrder(String sellerId, Order order) {
        SellerFinancial financial = getOrCreateFinancial(sellerId, order.getUserId());
        
        // Tính doanh thu (trừ hoa hồng)
        double orderTotal = order.getTotalPrice();
        double commission = orderTotal * (financial.getCommissionRate() / 100.0);
        double sellerRevenue = orderTotal - commission;
        
        financial.setTotalRevenue(financial.getTotalRevenue() + orderTotal);
        financial.setTotalCommission(financial.getTotalCommission() + commission);
        
        // Cập nhật số dư
        if ("delivered".equals(order.getStatus())) {
            // Đơn hàng đã giao → chuyển từ pending sang available sau withdrawalDays
            financial.setPendingBalance(financial.getPendingBalance() - sellerRevenue);
            financial.setAvailableBalance(financial.getAvailableBalance() + sellerRevenue);
        } else {
            // Đơn hàng mới → thêm vào pending
            financial.setPendingBalance(financial.getPendingBalance() + sellerRevenue);
        }
        
        // Cập nhật thống kê
        financial.setTotalOrders(financial.getTotalOrders() + 1);
        if ("delivered".equals(order.getStatus())) {
            financial.setCompletedOrders(financial.getCompletedOrders() + 1);
        } else {
            financial.setPendingOrders(financial.getPendingOrders() + 1);
        }
        
        // Tính giá trị đơn hàng trung bình
        if (financial.getCompletedOrders() > 0) {
            financial.setAverageOrderValue(financial.getTotalRevenue() / financial.getCompletedOrders());
        }
        
        financial.setUpdatedAt(LocalDateTime.now());
        financialRepository.save(financial);
    }
    
    // Yêu cầu rút tiền
    @Transactional
    public WithdrawalRequest requestWithdrawal(String sellerId, double amount, String bankName, 
                                                String bankAccountNumber, String bankAccountName, String bankBranch) {
        SellerFinancial financial = financialRepository.findBySellerId(sellerId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi tài chính"));
        
        // Kiểm tra số dư khả dụng
        if (financial.getAvailableBalance() < amount) {
            throw new RuntimeException("Số dư không đủ. Số dư khả dụng: " + financial.getAvailableBalance());
        }
        
        // Kiểm tra số tiền tối thiểu
        if (amount < financial.getMinWithdrawalAmount()) {
            throw new RuntimeException("Số tiền rút tối thiểu là " + financial.getMinWithdrawalAmount());
        }
        
        // Tạo yêu cầu rút tiền
        WithdrawalRequest request = new WithdrawalRequest();
        request.setSellerId(sellerId);
        request.setSellerFinancialId(financial.getId());
        request.setAmount(amount);
        request.setBankName(bankName);
        request.setBankAccountNumber(bankAccountNumber);
        request.setBankAccountName(bankAccountName);
        request.setBankBranch(bankBranch);
        request.setStatus("pending");
        
        // Trừ số dư khả dụng (tạm thời)
        financial.setAvailableBalance(financial.getAvailableBalance() - amount);
        financialRepository.save(financial);
        
        return withdrawalRepository.save(request);
    }
    
    // Xử lý yêu cầu rút tiền (Admin)
    @Transactional
    public WithdrawalRequest processWithdrawal(String requestId, String status, String adminId, 
                                                String transactionReference, String rejectionReason) {
        WithdrawalRequest request = withdrawalRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu rút tiền"));
        
        SellerFinancial financial = financialRepository.findById(request.getSellerFinancialId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi tài chính"));
        
        if ("completed".equals(status)) {
            request.setStatus("completed");
            request.setProcessedBy(adminId);
            request.setProcessedAt(LocalDateTime.now());
            request.setTransactionReference(transactionReference);
            
            // Cập nhật số tiền đã rút
            financial.setWithdrawnAmount(financial.getWithdrawnAmount() + request.getAmount());
            financial.setLastWithdrawalAt(LocalDateTime.now());
            
        } else if ("rejected".equals(status)) {
            request.setStatus("rejected");
            request.setProcessedBy(adminId);
            request.setProcessedAt(LocalDateTime.now());
            request.setRejectionReason(rejectionReason);
            
            // Hoàn lại số dư
            financial.setAvailableBalance(financial.getAvailableBalance() + request.getAmount());
        }
        
        financial.setUpdatedAt(LocalDateTime.now());
        financialRepository.save(financial);
        
        return withdrawalRepository.save(request);
    }
    
    // Lấy financial theo seller ID
    public Optional<SellerFinancial> getBySellerId(String sellerId) {
        return financialRepository.findBySellerId(sellerId);
    }
    
    // Lấy financial theo user ID
    public Optional<SellerFinancial> getByUserId(String userId) {
        return financialRepository.findByUserId(userId);
    }
    
    // Lấy tất cả financial
    public List<SellerFinancial> getAll() {
        return financialRepository.findAll();
    }
    
    // Cập nhật financial
    public SellerFinancial update(String id, SellerFinancial financial) {
        financial.setId(id);
        financial.setUpdatedAt(LocalDateTime.now());
        return financialRepository.save(financial);
    }
    
    // Lấy yêu cầu rút tiền theo seller
    public List<WithdrawalRequest> getWithdrawalRequestsBySeller(String sellerId) {
        return withdrawalRepository.findBySellerId(sellerId);
    }
    
    // Lấy yêu cầu rút tiền theo trạng thái
    public List<WithdrawalRequest> getWithdrawalRequestsByStatus(String status) {
        return withdrawalRepository.findByStatus(status);
    }
    
    // Lấy tất cả yêu cầu rút tiền
    public List<WithdrawalRequest> getAllWithdrawalRequests() {
        return withdrawalRepository.findAll();
    }
}

