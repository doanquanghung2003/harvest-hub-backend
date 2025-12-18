package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.BankCard;
import com.example.harvesthubbackend.Service.BankCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bank-cards")
@CrossOrigin(origins = "*")
public class BankCardController {
    
    @Autowired
    private BankCardService bankCardService;
    
    /**
     * Get all bank cards for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBankCards(@PathVariable String userId) {
        try {
            List<BankCard> cards = bankCardService.getBankCardsByUserId(userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy danh sách thẻ: " + e.getMessage()));
        }
    }
    
    /**
     * Get bank card by ID
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getBankCard(
            @PathVariable String cardId,
            @RequestParam String userId) {
        try {
            Optional<BankCard> cardOpt = bankCardService.getBankCardById(cardId, userId);
            if (cardOpt.isPresent()) {
                return ResponseEntity.ok(cardOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy thẻ ngân hàng"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi lấy thông tin thẻ: " + e.getMessage()));
        }
    }
    
    /**
     * Add a new bank card
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> addBankCard(
            @PathVariable String userId,
            @RequestBody BankCard bankCard) {
        try {
            BankCard savedCard = bankCardService.addBankCard(userId, bankCard);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thêm thẻ ngân hàng thành công");
            response.put("data", savedCard);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Lỗi khi thêm thẻ: " + e.getMessage()));
        }
    }
    
    /**
     * Update bank card
     */
    @PutMapping("/{cardId}")
    public ResponseEntity<?> updateBankCard(
            @PathVariable String cardId,
            @RequestParam String userId,
            @RequestBody BankCard bankCard) {
        try {
            BankCard updatedCard = bankCardService.updateBankCard(userId, cardId, bankCard);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật thẻ ngân hàng thành công");
            response.put("data", updatedCard);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi cập nhật thẻ: " + e.getMessage()));
        }
    }
    
    /**
     * Set card as default
     */
    @PutMapping("/{cardId}/set-default")
    public ResponseEntity<?> setDefaultCard(
            @PathVariable String cardId,
            @RequestParam String userId) {
        try {
            bankCardService.setAsDefaultCard(userId, cardId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã đặt thẻ làm mặc định");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi đặt thẻ mặc định: " + e.getMessage()));
        }
    }
    
    /**
     * Delete bank card
     */
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteBankCard(
            @PathVariable String cardId,
            @RequestParam String userId) {
        try {
            boolean deleted = bankCardService.deleteBankCard(userId, cardId);
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Xóa thẻ ngân hàng thành công");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Không tìm thấy thẻ ngân hàng"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi xóa thẻ: " + e.getMessage()));
        }
    }
}

