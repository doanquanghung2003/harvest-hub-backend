package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "users")
public class User implements UserDetails {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private String role;
    
    // Thông tin cá nhân hiển thị
    private String firstName; // Tên
    private String lastName;  // Họ
    private String phoneNumber; // Số điện thoại
    private String bio; // Giới thiệu
    private String avatar; // URL ảnh đại diện
    private String membershipType; // Loại thành viên (VIP, NORMAL, etc.)
    private String membershipDate; // Ngày tham gia
    
    // Địa chỉ giao hàng
    private String addressStreet; // Đường
    private String addressWard; // Phường/Xã
    private String addressDistrict; // Quận/Huyện
    private String addressCity; // Tỉnh/Thành phố
    
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private String accountStatus; // ACTIVE, VIOLATION, RESTRICTED, SUSPENDED

    // Chính sách mật khẩu
    private LocalDateTime passwordChangedAt; // Thời điểm thay đổi mật khẩu gần nhất
    private List<String> passwordHistory; // Danh sách hash các mật khẩu trước đây để chặn tái sử dụng
    
    // Account lockout
    private int failedLoginAttempts = 0; // Số lần đăng nhập sai
    private LocalDateTime lockedUntil; // Thời điểm mở khóa tài khoản (null nếu không bị khóa)
    
    // Payment security
    private String paymentPin; // Mã khóa xác thực 6 số cho thanh toán

    public User() {}

    public User(String username, String password, String email, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null || role.isEmpty()) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // Getters and Setters cho thông tin cá nhân
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getMembershipType() { return membershipType; }
    public void setMembershipType(String membershipType) { this.membershipType = membershipType; }

    public String getMembershipDate() { return membershipDate; }
    public void setMembershipDate(String membershipDate) { this.membershipDate = membershipDate; }

    // Getters and Setters cho địa chỉ
    public String getAddressStreet() { return addressStreet; }
    public void setAddressStreet(String addressStreet) { this.addressStreet = addressStreet; }

    public String getAddressWard() { return addressWard; }
    public void setAddressWard(String addressWard) { this.addressWard = addressWard; }

    public String getAddressDistrict() { return addressDistrict; }
    public void setAddressDistrict(String addressDistrict) { this.addressDistrict = addressDistrict; }

    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNonExpired(boolean accountNonExpired) { this.accountNonExpired = accountNonExpired; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public void setCredentialsNonExpired(boolean credentialsNonExpired) { this.credentialsNonExpired = credentialsNonExpired; }

    public String getAccountStatus() { return accountStatus != null && !accountStatus.trim().isEmpty() ? accountStatus : "ACTIVE"; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    // Getters/Setters cho chính sách mật khẩu
    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public List<String> getPasswordHistory() { return passwordHistory; }
    public void setPasswordHistory(List<String> passwordHistory) { this.passwordHistory = passwordHistory; }

    // Tiện ích an toàn để thêm vào lịch sử
    public void addPasswordToHistory(String encodedPassword) {
        if (this.passwordHistory == null) {
            this.passwordHistory = new ArrayList<>();
        }
        this.passwordHistory.add(encodedPassword);
    }
    
    // Getters/Setters cho account lockout
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
    
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
    
    public boolean isAccountLocked() {
        if (lockedUntil == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockedUntil);
    }
    
    // Getters/Setters cho payment pin
    public String getPaymentPin() {
        return paymentPin;
    }
    
    public void setPaymentPin(String paymentPin) {
        this.paymentPin = paymentPin;
    }
}