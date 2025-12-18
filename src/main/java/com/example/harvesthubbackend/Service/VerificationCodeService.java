package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.VerificationCode;
import com.example.harvesthubbackend.Repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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
        // Xóa mã cũ nếu có
        codeRepository.deleteByEmail(email);
        
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

