package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.VerificationCode;
import com.example.harvesthubbackend.Repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VerificationCodeService {
    
    @Autowired
    private VerificationCodeRepository codeRepository;
    
    private static final int CODE_EXPIRATION_MINUTES = 10; // Mã hết hạn sau 10 phút
    private static final int CODE_LENGTH = 6;
    
    /**
     * Tạo mã xác minh 6 chữ số cho email
     */
    public String generateVerificationCode(String email) {
        // Xóa tất cả mã cũ của email này - sử dụng findAllByEmail và deleteAll để đảm bảo xóa hết
        try {
            List<VerificationCode> oldCodes = codeRepository.findAllByEmail(email);
            if (!oldCodes.isEmpty()) {
                codeRepository.deleteAll(oldCodes);
                System.out.println("Đã xóa " + oldCodes.size() + " mã xác minh cũ cho email: " + email);
            }
        } catch (Exception e) {
            // Nếu có lỗi, thử cách fallback là deleteByEmail (có thể chỉ xóa 1 record)
            System.err.println("Lỗi khi xóa mã cũ bằng deleteAll, thử deleteByEmail: " + e.getMessage());
            try {
                codeRepository.deleteByEmail(email);
                System.out.println("Đã xóa mã cũ (fallback) cho email: " + email);
            } catch (Exception e2) {
                System.err.println("Lỗi khi xóa mã cũ (fallback): " + e2.getMessage());
                // Tiếp tục tạo mã mới dù có lỗi xóa
            }
        }
        
        // Tạo mã 6 chữ số ngẫu nhiên
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        
        String codeString = code.toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
        
        VerificationCode verificationCode = new VerificationCode(email, codeString, expiresAt);
        codeRepository.save(verificationCode);
        
        System.out.println("Đã tạo mã xác minh mới cho email: " + email + ", mã: " + codeString);
        return codeString;
    }
    
    /**
     * Kiểm tra mã xác minh có hợp lệ không
     */
    public boolean verifyCode(String email, String code) {
        Optional<VerificationCode> codeOpt = codeRepository.findByEmailAndCode(email, code);
        
        if (codeOpt.isEmpty()) {
            return false;
        }
        
        VerificationCode verificationCode = codeOpt.get();
        
        if (verificationCode.isUsed()) {
            return false;
        }
        
        if (verificationCode.isExpired()) {
            return false;
        }
        
        // Đánh dấu mã đã sử dụng
        verificationCode.setUsed(true);
        codeRepository.save(verificationCode);
        
        return true;
    }
    
    /**
     * Kiểm tra email đã được xác minh chưa (có mã hợp lệ chưa sử dụng)
     */
    public boolean isEmailVerified(String email) {
        Optional<VerificationCode> codeOpt = codeRepository.findByEmail(email);
        
        if (codeOpt.isEmpty()) {
            return false;
        }
        
        VerificationCode verificationCode = codeOpt.get();
        return !verificationCode.isUsed() && !verificationCode.isExpired();
    }
}

