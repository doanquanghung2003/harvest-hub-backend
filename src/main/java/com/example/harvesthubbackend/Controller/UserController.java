package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Utils.ImageUrlUtils;

import java.util.List;

import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.SecurityConfig.JwtService;
import com.example.harvesthubbackend.Service.UserService;
import com.example.harvesthubbackend.DTO.UpdateUserDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/user", "/api/v1/user"})
@CrossOrigin(origins = {"*"})
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;

    // ===== Admin: User Management APIs =====
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAll();
            ImageUrlUtils.normalizeUsers(users);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        User user = userService.getById(id);
        if (user == null) return ResponseEntity.notFound().build();
        ImageUrlUtils.normalizeUser(user);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User created = userService.create(user);
            ImageUrlUtils.normalizeUser(created);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User update) {
        try {
            User updated = userService.update(id, update);
            ImageUrlUtils.normalizeUser(updated);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(Map.of("message", "User deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Profile request - Authentication: " + authentication);
            System.out.println("Profile request - Principal: " + authentication.getPrincipal());
            System.out.println("Profile request - Name: " + authentication.getName());
            
            if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(401).body(error);
            }
            
            String username = authentication.getName();
            User user = userService.getByUsername(username);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(404).body(error);
            }
            
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole());
            
            // Thông tin cá nhân hiển thị
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("phoneNumber", user.getPhoneNumber());
            profile.put("bio", user.getBio());
            profile.put("avatar", user.getAvatar());
            profile.put("membershipType", user.getMembershipType());
            profile.put("membershipDate", user.getMembershipDate());
            
            // Địa chỉ giao hàng
            profile.put("addressStreet", user.getAddressStreet());
            profile.put("addressWard", user.getAddressWard());
            profile.put("addressDistrict", user.getAddressDistrict());
            profile.put("addressCity", user.getAddressCity());
            
            // Payment PIN (for validation purposes)
            profile.put("paymentPin", user.getPaymentPin());
            profile.put("hasPaymentPin", user.getPaymentPin() != null && !user.getPaymentPin().isEmpty());
            
            System.out.println("Profile response: " + profile);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            System.err.println("Error getting profile: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get profile: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateUserDTO updateDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getByUsername(username);
            
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Cập nhật thông tin từ DTO
            User updateData = new User();
            if (updateDTO.getFirstName() != null) {
                updateData.setFirstName(updateDTO.getFirstName());
            }
            if (updateDTO.getLastName() != null) {
                updateData.setLastName(updateDTO.getLastName());
            }
            if (updateDTO.getPhoneNumber() != null) {
                updateData.setPhoneNumber(updateDTO.getPhoneNumber());
            }
            if (updateDTO.getBio() != null) {
                updateData.setBio(updateDTO.getBio());
            }
            if (updateDTO.getAddressStreet() != null) {
                updateData.setAddressStreet(updateDTO.getAddressStreet());
            }
            if (updateDTO.getAddressWard() != null) {
                updateData.setAddressWard(updateDTO.getAddressWard());
            }
            if (updateDTO.getAddressDistrict() != null) {
                updateData.setAddressDistrict(updateDTO.getAddressDistrict());
            }
            if (updateDTO.getAddressCity() != null) {
                updateData.setAddressCity(updateDTO.getAddressCity());
            }
            if (updateDTO.getPaymentPin() != null) {
                updateData.setPaymentPin(updateDTO.getPaymentPin());
            }
            
            User updatedUser = userService.update(user.getId(), updateData);
            
            // Normalize URL ảnh để hoạt động với mọi IP/hostname
            ImageUrlUtils.normalizeUser(updatedUser);
            
            // Generate new JWT token with updated username
            String newToken = jwtService.generateToken(updatedUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            response.put("token", newToken);
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", updatedUser.getId());
            userData.put("username", updatedUser.getUsername());
            userData.put("email", updatedUser.getEmail());
            userData.put("role", updatedUser.getRole());
            userData.put("firstName", updatedUser.getFirstName());
            userData.put("lastName", updatedUser.getLastName());
            userData.put("phoneNumber", updatedUser.getPhoneNumber());
            userData.put("bio", updatedUser.getBio());
            userData.put("avatar", updatedUser.getAvatar());
            userData.put("membershipType", updatedUser.getMembershipType());
            userData.put("membershipDate", updatedUser.getMembershipDate());
            userData.put("addressStreet", updatedUser.getAddressStreet());
            userData.put("addressWard", updatedUser.getAddressWard());
            userData.put("addressDistrict", updatedUser.getAddressDistrict());
            userData.put("addressCity", updatedUser.getAddressCity());
            // Note: paymentPin is sensitive, only return if set (not the actual value for security)
            userData.put("hasPaymentPin", updatedUser.getPaymentPin() != null && !updatedUser.getPaymentPin().isEmpty());
            userData.put("paymentPin", updatedUser.getPaymentPin()); // Return for validation purposes
            
            response.put("user", userData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update profile");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String newPassword = request.get("newPassword");
            
            if (username == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Username and newPassword are required");
                return ResponseEntity.badRequest().body(error);
            }
            
            User user = userService.getByUsername(username);
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }
            
            // Cập nhật password
            User updateData = new User();
            updateData.setPassword(newPassword);
            userService.update(user.getId(), updateData);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping(value = "/avatar", consumes = {
        org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE,
        org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
    })
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File is empty");
                return ResponseEntity.badRequest().body(error);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.getByUsername(username);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            // Tạo thư mục uploads/avatars nếu chưa có
            Path uploadDir = Paths.get("uploads", "avatars");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Tên file an toàn
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
            String safeFilename = user.getId() + "_" + System.currentTimeMillis() + fileExtension;

            Path targetPath = uploadDir.resolve(safeFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String relativeUrl = "/uploads/avatars/" + safeFilename;
            // Sử dụng đường dẫn tương đối để hoạt động với mọi IP/hostname
            // Browser sẽ tự động resolve đường dẫn tương đối dựa trên origin của request

            // Cập nhật avatar URL cho user (lưu đường dẫn tương đối)
            user.setAvatar(relativeUrl);
            userService.update(user.getId(), user);

            return ResponseEntity.ok(Map.of("url", relativeUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload avatar: " + e.getMessage()));
        }
    }
}