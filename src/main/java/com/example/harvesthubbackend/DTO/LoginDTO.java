package com.example.harvesthubbackend.DTO;

import jakarta.validation.constraints.*;

public class LoginDTO {
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    private String username;
    
    @NotBlank(message = "Mật khẩu là bắt buộc")
    private String password;
    
    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

