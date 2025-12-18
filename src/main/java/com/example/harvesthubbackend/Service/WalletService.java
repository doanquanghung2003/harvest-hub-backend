package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Wallet;
import com.example.harvesthubbackend.Models.WalletTransaction;
import com.example.harvesthubbackend.Repository.WalletRepository;
import com.example.harvesthubbackend.Repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private WalletTransactionRepository transactionRepository;
    
    /**
     * Get or create wallet for user
     */
    public Wallet getOrCreateWallet(String userId) {
        Optional<Wallet> walletOpt = walletRepository.findByUserId(userId);
        if (walletOpt.isPresent()) {
            return walletOpt.get();
        }
        
        // Create new wallet
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(0.0);
        wallet.setCurrency("VND");
        wallet.setStatus("active");
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        
        return walletRepository.save(wallet);
    }
    
    /**
     * Get wallet by user ID
     */
    public Optional<Wallet> getWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId);
    }
    
    /**
     * Deposit money to wallet
     */
    @Transactional
    public WalletTransaction deposit(String userId, double amount, String paymentMethod, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
        }
        
        Wallet wallet = getOrCreateWallet(userId);
        double balanceBefore = wallet.getBalance();
        
        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(userId);
        transaction.setType("deposit");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore + amount);
        transaction.setStatus("pending");
        transaction.setDescription(description != null ? description : "Nạp tiền vào ví");
        transaction.setPaymentMethod(paymentMethod);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        // If payment method is wallet or immediate, complete immediately
        if ("wallet".equals(paymentMethod) || paymentMethod == null) {
            wallet.deposit(amount);
            wallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(wallet);
            
            transaction.setStatus("completed");
            transaction.setBalanceAfter(wallet.getBalance());
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Withdraw money from wallet
     */
    @Transactional
    public WalletTransaction withdraw(String userId, double amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0");
        }
        
        Wallet wallet = getOrCreateWallet(userId);
        
        if (!wallet.hasSufficientBalance(amount)) {
            throw new IllegalStateException("Số dư không đủ để thực hiện giao dịch");
        }
        
        double balanceBefore = wallet.getBalance();
        
        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(userId);
        transaction.setType("withdraw");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore - amount);
        transaction.setStatus("completed");
        transaction.setDescription(description != null ? description : "Rút tiền từ ví");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        // Update wallet
        wallet.withdraw(amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        
        transaction.setBalanceAfter(wallet.getBalance());
        return transactionRepository.save(transaction);
    }
    
    /**
     * Pay with wallet
     */
    @Transactional
    public WalletTransaction payWithWallet(String userId, double amount, String orderId, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }
        
        Wallet wallet = getOrCreateWallet(userId);
        
        if (!wallet.hasSufficientBalance(amount)) {
            throw new IllegalStateException("Số dư ví không đủ để thanh toán");
        }
        
        double balanceBefore = wallet.getBalance();
        
        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(userId);
        transaction.setType("payment");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore - amount);
        transaction.setStatus("completed");
        transaction.setDescription(description != null ? description : "Thanh toán đơn hàng " + orderId);
        transaction.setReferenceId(orderId);
        transaction.setReferenceType("order");
        transaction.setPaymentMethod("wallet");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        // Update wallet
        wallet.withdraw(amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        
        transaction.setBalanceAfter(wallet.getBalance());
        return transactionRepository.save(transaction);
    }
    
    /**
     * Refund to wallet
     */
    @Transactional
    public WalletTransaction refundToWallet(String userId, double amount, String orderId, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0");
        }
        
        Wallet wallet = getOrCreateWallet(userId);
        double balanceBefore = wallet.getBalance();
        
        // Create transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setUserId(userId);
        transaction.setType("refund");
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceBefore + amount);
        transaction.setStatus("completed");
        transaction.setDescription(description != null ? description : "Hoàn tiền đơn hàng " + orderId);
        transaction.setReferenceId(orderId);
        transaction.setReferenceType("order");
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        // Update wallet
        wallet.deposit(amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        
        transaction.setBalanceAfter(wallet.getBalance());
        return transactionRepository.save(transaction);
    }
    
    /**
     * Complete pending deposit transaction
     */
    @Transactional
    public WalletTransaction completeDeposit(String transactionId) {
        System.out.println("=== WalletService.completeDeposit ===");
        System.out.println("Transaction ID: " + transactionId);
        
        Optional<WalletTransaction> transactionOpt = transactionRepository.findById(transactionId);
        if (!transactionOpt.isPresent()) {
            System.err.println("Transaction not found with ID: " + transactionId);
            // Try to find by other means
            List<WalletTransaction> allPending = transactionRepository.findByStatus("pending");
            System.out.println("All pending transactions: " + allPending.size());
            for (WalletTransaction t : allPending) {
                System.out.println("  - Transaction ID: " + t.getId() + ", Type: " + t.getType() + ", Amount: " + t.getAmount());
            }
            throw new IllegalArgumentException("Transaction not found with ID: " + transactionId);
        }
        
        WalletTransaction transaction = transactionOpt.get();
        System.out.println("Transaction found:");
        System.out.println("  - ID: " + transaction.getId());
        System.out.println("  - Status: " + transaction.getStatus());
        System.out.println("  - Type: " + transaction.getType());
        System.out.println("  - Amount: " + transaction.getAmount());
        System.out.println("  - Wallet ID: " + transaction.getWalletId());
        
        if (!"pending".equals(transaction.getStatus())) {
            System.out.println("Transaction is not pending, current status: " + transaction.getStatus());
            return transaction;
        }
        
        Wallet wallet = walletRepository.findById(transaction.getWalletId())
            .orElseThrow(() -> {
                System.err.println("Wallet not found with ID: " + transaction.getWalletId());
                return new IllegalArgumentException("Wallet not found");
            });
        
        System.out.println("Wallet found:");
        System.out.println("  - ID: " + wallet.getId());
        System.out.println("  - Current balance: " + wallet.getBalance());
        System.out.println("  - Amount to deposit: " + transaction.getAmount());
        
        double balanceBefore = wallet.getBalance();
        wallet.deposit(transaction.getAmount());
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet savedWallet = walletRepository.save(wallet);
        
        System.out.println("Wallet updated:");
        System.out.println("  - New balance: " + savedWallet.getBalance());
        
        transaction.setStatus("completed");
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(savedWallet.getBalance());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        WalletTransaction savedTransaction = transactionRepository.save(transaction);
        System.out.println("Transaction completed successfully!");
        System.out.println("  - Final balance: " + savedTransaction.getBalanceAfter());
        
        return savedTransaction;
    }
    
    /**
     * Get transaction history
     */
    public List<WalletTransaction> getTransactionHistory(String userId, int limit) {
        List<WalletTransaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (limit > 0 && transactions.size() > limit) {
            return transactions.subList(0, limit);
        }
        return transactions;
    }
    
    /**
     * Get all transactions for user
     */
    public List<WalletTransaction> getAllTransactions(String userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get transaction by ID
     */
    public Optional<WalletTransaction> getTransactionById(String transactionId) {
        return transactionRepository.findById(transactionId);
    }
}

