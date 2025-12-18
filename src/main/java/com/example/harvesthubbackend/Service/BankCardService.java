package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.BankCard;
import com.example.harvesthubbackend.Repository.BankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BankCardService {
    
    @Autowired
    private BankCardRepository bankCardRepository;
    
    /**
     * Get all bank cards for a user
     */
    public List<BankCard> getBankCardsByUserId(String userId) {
        return bankCardRepository.findByUserIdAndStatus(userId, "active");
    }
    
    /**
     * Get bank card by ID and user ID
     */
    public Optional<BankCard> getBankCardById(String id, String userId) {
        return bankCardRepository.findByIdAndUserId(id, userId);
    }
    
    /**
     * Get default bank card for a user
     */
    public Optional<BankCard> getDefaultBankCard(String userId) {
        return bankCardRepository.findByUserIdAndIsDefaultTrue(userId);
    }
    
    /**
     * Add a new bank card
     */
    @Transactional
    public BankCard addBankCard(String userId, BankCard bankCard) {
        // Mask card number (only show last 4 digits)
        String cardNumber = bankCard.getCardNumber();
        if (cardNumber != null && cardNumber.length() >= 4) {
            String last4 = cardNumber.substring(cardNumber.length() - 4);
            bankCard.setCardNumberMasked("**** **** **** " + last4);
            // Only store last 4 digits for security
            bankCard.setCardNumber(last4);
        }
        
        bankCard.setUserId(userId);
        bankCard.setStatus("active");
        bankCard.setCreatedAt(LocalDateTime.now());
        bankCard.setUpdatedAt(LocalDateTime.now());
        
        // If this is set as default, unset other default cards
        if (bankCard.isDefault()) {
            setAsDefaultCard(userId, null);
        }
        
        return bankCardRepository.save(bankCard);
    }
    
    /**
     * Update bank card
     */
    @Transactional
    public BankCard updateBankCard(String userId, String cardId, BankCard updatedCard) {
        Optional<BankCard> cardOpt = bankCardRepository.findByIdAndUserId(cardId, userId);
        if (!cardOpt.isPresent()) {
            throw new IllegalArgumentException("Thẻ ngân hàng không tồn tại");
        }
        
        BankCard card = cardOpt.get();
        
        // Update fields
        if (updatedCard.getCardHolderName() != null) {
            card.setCardHolderName(updatedCard.getCardHolderName());
        }
        if (updatedCard.getBankName() != null) {
            card.setBankName(updatedCard.getBankName());
        }
        if (updatedCard.getBankCode() != null) {
            card.setBankCode(updatedCard.getBankCode());
        }
        if (updatedCard.getExpiryMonth() != null) {
            card.setExpiryMonth(updatedCard.getExpiryMonth());
        }
        if (updatedCard.getExpiryYear() != null) {
            card.setExpiryYear(updatedCard.getExpiryYear());
        }
        if (updatedCard.getCardType() != null) {
            card.setCardType(updatedCard.getCardType());
        }
        
        // Handle default card
        if (updatedCard.isDefault() && !card.isDefault()) {
            setAsDefaultCard(userId, cardId);
            card.setDefault(true);
        } else if (!updatedCard.isDefault() && card.isDefault()) {
            card.setDefault(false);
        }
        
        card.setUpdatedAt(LocalDateTime.now());
        
        return bankCardRepository.save(card);
    }
    
    /**
     * Set a card as default (unset others)
     */
    @Transactional
    public void setAsDefaultCard(String userId, String cardId) {
        // Unset all default cards for this user
        List<BankCard> allCards = bankCardRepository.findByUserId(userId);
        for (BankCard card : allCards) {
            if (card.isDefault() && (cardId == null || !card.getId().equals(cardId))) {
                card.setDefault(false);
                card.setUpdatedAt(LocalDateTime.now());
                bankCardRepository.save(card);
            }
        }
        
        // Set the specified card as default
        if (cardId != null) {
            Optional<BankCard> cardOpt = bankCardRepository.findByIdAndUserId(cardId, userId);
            if (cardOpt.isPresent()) {
                BankCard card = cardOpt.get();
                card.setDefault(true);
                card.setUpdatedAt(LocalDateTime.now());
                bankCardRepository.save(card);
            }
        }
    }
    
    /**
     * Delete bank card (soft delete by setting status to inactive)
     */
    @Transactional
    public boolean deleteBankCard(String userId, String cardId) {
        Optional<BankCard> cardOpt = bankCardRepository.findByIdAndUserId(cardId, userId);
        if (!cardOpt.isPresent()) {
            return false;
        }
        
        BankCard card = cardOpt.get();
        card.setStatus("inactive");
        card.setDefault(false); // Can't be default if inactive
        card.setUpdatedAt(LocalDateTime.now());
        bankCardRepository.save(card);
        
        return true;
    }
    
    /**
     * Check if user owns the card
     */
    public boolean userOwnsCard(String userId, String cardId) {
        return bankCardRepository.existsByIdAndUserId(cardId, userId);
    }
}

