package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.PasswordResetToken;
import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.Repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserService userService;
    
    private static final int TOKEN_EXPIRATION_HOURS = 1; // Token hết hạn sau 1 giờ
    
    /**
     * Tạo token reset password cho user
     */
    public PasswordResetToken createPasswordResetToken(String emailOrUsername) {
        // Tìm user theo email hoặc username
        User user = userService.getByEmail(emailOrUsername);
        if (user == null) {
            user = userService.getByUsername(emailOrUsername);
        }
        
        if (user == null) {
            throw new RuntimeException("User not found with email or username: " + emailOrUsername);
        }
        
        // Xóa token cũ nếu có
        tokenRepository.deleteByUserId(user.getId());
        
        // Tạo token mới
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);
        
        PasswordResetToken resetToken = new PasswordResetToken(user.getId(), token, expiresAt);
        return tokenRepository.save(resetToken);
    }
    
    /**
     * Kiểm tra token có hợp lệ không
     */
    public PasswordResetToken validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired token");
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        if (resetToken.isUsed()) {
            throw new RuntimeException("Token has already been used");
        }
        
        if (resetToken.isExpired()) {
            throw new RuntimeException("Token has expired");
        }
        
        return resetToken;
    }
    
    /**
     * Reset password với token
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = validateToken(token);
        
        User user = userService.getById(resetToken.getUserId());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        // Cập nhật password - tạo User object mới chỉ với password để update
        User updateUser = new User();
        updateUser.setPassword(newPassword); // UserService.update sẽ tự encode
        userService.update(user.getId(), updateUser);
        
        // Đánh dấu token đã sử dụng
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
    
    /**
     * Lấy token theo userId
     */
    public Optional<PasswordResetToken> getTokenByUserId(String userId) {
        return tokenRepository.findByUserId(userId);
    }
}

