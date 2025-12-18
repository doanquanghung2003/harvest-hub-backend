package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.PasswordResetToken;
import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.SecurityConfig.JwtService;
import com.example.harvesthubbackend.Service.AccountLockoutService;
import com.example.harvesthubbackend.Service.EmailService;
import com.example.harvesthubbackend.Service.PasswordResetService;
import com.example.harvesthubbackend.Service.UserService;
import com.example.harvesthubbackend.Utils.PasswordValidator;
import com.example.harvesthubbackend.Utils.ImageUrlUtils;
import com.example.harvesthubbackend.DTO.RegisterDTO;
import com.example.harvesthubbackend.DTO.LoginDTO;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "http://localhost:8082", "http://127.0.0.1:8082"})
@Tag(name = "Authentication", description = "API endpoints for user authentication and authorization")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AccountLockoutService accountLockoutService;
    
    @Autowired
    private com.example.harvesthubbackend.Service.VerificationCodeService verificationCodeService;
    
    @Autowired
    private com.example.harvesthubbackend.Service.VoucherAutomationService voucherAutomationService;

    // Cho phép cấu hình URL frontend qua application.properties
    @Value("${frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;
    

    @Operation(summary = "Register new user", description = "Create a new user account with username, password, and email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            System.out.println("Register request received for user: " + registerDTO.getUsername());
            
            // Validate password strength
            PasswordValidator.ValidationResult passwordValidation = PasswordValidator.validate(registerDTO.getPassword());
            if (!passwordValidation.isValid()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Mật khẩu không hợp lệ");
                error.put("errors", passwordValidation.getErrors());
                error.put("message", "Mật khẩu không đáp ứng yêu cầu: " + String.join(", ", passwordValidation.getErrors()));
                return ResponseEntity.badRequest().body(error);
            }
            
            // Convert DTO to User
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(registerDTO.getPassword());
            user.setEmail(registerDTO.getEmail());
            user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "USER");
            
            User createdUser = userService.create(user);
            
            // Grant welcome voucher to new user
            try {
                voucherAutomationService.grantWelcomeVoucher(createdUser.getId());
            } catch (Exception e) {
                // Log but don't fail registration
                System.err.println("Failed to grant welcome voucher: " + e.getMessage());
            }
            
            // Tạo token cho user mới đăng ký
            String token = jwtService.generateToken(createdUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("token", token);
            response.put("user", Map.of(
                "id", createdUser.getId(),
                "username", createdUser.getUsername(),
                "email", createdUser.getEmail(),
                "role", createdUser.getRole()
            ));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Registration error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @Operation(summary = "User login", description = "Authenticate user with username and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "400", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            System.out.println("=== Login request received ===");
            System.out.println("Username: " + loginDTO.getUsername());
            System.out.println("Password length: " + (loginDTO.getPassword() != null ? loginDTO.getPassword().length() : "null"));
            
            String username = loginDTO.getUsername();
            String password = loginDTO.getPassword();

            // Check if user exists and account is locked
            User user = userService.getByUsername(username);
            if (user != null) {
                if (accountLockoutService.isAccountLocked(user)) {
                    long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(user);
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Tài khoản đã bị khóa");
                    error.put("errorCode", "ACCOUNT_LOCKED");
                    error.put("message", "Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau " + remainingMinutes + " phút.");
                    error.put("remainingMinutes", remainingMinutes);
                    return ResponseEntity.status(423).body(error); // 423 Locked
                }
            }

            System.out.println("Attempting authentication...");
            
            try {
                // Xác thực user
                Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
                );

                System.out.println("Authentication successful for user: " + username);

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User authenticatedUser = userService.getByUsername(username);
                
                // Reset failed login attempts on successful login
                accountLockoutService.resetFailedAttempts(authenticatedUser);
                
                System.out.println("User found: " + authenticatedUser.getUsername() + ", role: " + authenticatedUser.getRole());

                // Kiểm tra hạn mật khẩu 6 tháng
                LocalDateTime changedAt = authenticatedUser.getPasswordChangedAt();
                if (changedAt != null && changedAt.plusMonths(6).isBefore(LocalDateTime.now())) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "PASSWORD_EXPIRED");
                    error.put("message", "Password expired. Please reset your password.");
                    return ResponseEntity.status(403).body(error);
                }
                
                // Tạo token
                String token = jwtService.generateToken(userDetails);
                System.out.println("JWT token generated successfully");
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("token", token);
                response.put("user", Map.of(
                    "id", authenticatedUser.getId(),
                    "username", authenticatedUser.getUsername(),
                    "email", authenticatedUser.getEmail(),
                    "role", authenticatedUser.getRole()
                ));
                
                System.out.println("Login successful for user: " + username);
                return ResponseEntity.ok(response);
            } catch (org.springframework.security.core.AuthenticationException e) {
                // Authentication failed - record failed attempt
                if (user != null) {
                    accountLockoutService.recordFailedAttempt(user);
                    int remainingAttempts = accountLockoutService.getRemainingAttempts(user);
                    
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Sai tên đăng nhập hoặc mật khẩu");
                    error.put("errorCode", "INVALID_CREDENTIALS");
                    error.put("remainingAttempts", remainingAttempts);
                    
                    if (accountLockoutService.isAccountLocked(user)) {
                        long remainingMinutes = accountLockoutService.getRemainingLockoutMinutes(user);
                        error.put("accountLocked", true);
                        error.put("error", "Tài khoản đã bị khóa");
                        error.put("errorCode", "ACCOUNT_LOCKED");
                        error.put("message", "Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần. Vui lòng thử lại sau " + remainingMinutes + " phút.");
                        error.put("remainingMinutes", remainingMinutes);
                        return ResponseEntity.status(423).body(error);
                    } else {
                        error.put("message", "Sai tên đăng nhập hoặc mật khẩu. Còn " + remainingAttempts + " lần thử.");
                        error.put("detail", "Thông tin xác thực không đúng, vui lòng kiểm tra lại.");
                    }
                    
                    return ResponseEntity.badRequest().body(error);
                } else {
                    // User doesn't exist - don't reveal this information
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Sai tên đăng nhập hoặc mật khẩu");
                    error.put("errorCode", "INVALID_CREDENTIALS");
                    error.put("message", "Sai tên đăng nhập hoặc mật khẩu.");
                    return ResponseEntity.badRequest().body(error);
                }
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Với JWT, logout được xử lý ở client (xóa token)
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user", description = "Get information about the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "No token provided"));
            }
            
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            User user = userService.getByUsername(username);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            // Normalize URL ảnh để hoạt động với mọi IP/hostname
            ImageUrlUtils.normalizeUser(user);
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid token"));
        }
    }

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuthentication() {
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", authentication != null && !"anonymousUser".equals(authentication.getName()));
            response.put("username", authentication != null ? authentication.getName() : "anonymous");
            response.put("authorities", authentication != null ? authentication.getAuthorities() : "none");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to check authentication: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String emailOrUsername = request.get("emailOrUsername");
            
            if (emailOrUsername == null || emailOrUsername.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email or username is required"));
            }
            
            PasswordResetToken resetToken = passwordResetService.createPasswordResetToken(emailOrUsername);
            
            String resetLink = frontendBaseUrl + "/reset-password?token=" + resetToken.getToken();

            // Lấy email người dùng từ userId của token
            User targetUser = userService.getById(resetToken.getUserId());
            if (targetUser == null || targetUser.getEmail() == null || targetUser.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User email not found"));
            }

            // Gửi email chứa link đặt lại mật khẩu
            emailService.sendPasswordResetEmail(targetUser.getEmail(), resetLink);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset email has been sent");
            // Không trả token/link ở production; để trống để an toàn
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Forgot password error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Unexpected error during forgot password: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
            }
            
            // Validate password strength
            PasswordValidator.ValidationResult passwordValidation = PasswordValidator.validate(newPassword);
            if (!passwordValidation.isValid()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Mật khẩu không hợp lệ");
                error.put("errors", passwordValidation.getErrors());
                error.put("message", "Mật khẩu không đáp ứng yêu cầu: " + String.join(", ", passwordValidation.getErrors()));
                return ResponseEntity.badRequest().body(error);
            }
            
            passwordResetService.resetPassword(token, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Reset password error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            System.err.println("Unexpected error during reset password: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @Operation(summary = "Send verification code", description = "Send a 6-digit verification code to the user's email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification code sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email")
    })
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            // Validate email format
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!email.matches(emailRegex)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
            }
            
            // Generate verification code
            String code = verificationCodeService.generateVerificationCode(email);
            
            // Send email with verification code
            emailService.sendVerificationCodeEmail(email, code);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Verification code has been sent to your email");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Send verification code error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @Operation(summary = "Verify code", description = "Verify the 6-digit code sent to the user's email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Code verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired code")
    })
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String code = request.get("code");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Verification code is required"));
            }
            
            // Verify code
            boolean isValid = verificationCodeService.verifyCode(email, code);
            
            if (isValid) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Email verified successfully");
                response.put("verified", true);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid or expired verification code");
                error.put("verified", false);
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            System.err.println("Verify code error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
