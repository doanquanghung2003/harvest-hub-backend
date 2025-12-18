package com.example.harvesthubbackend.DTO;

import jakarta.validation.constraints.*;

public class UpdateUserDTO {
    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    private String firstName;
    
    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String lastName;
    
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10 hoặc 11 chữ số")
    private String phoneNumber;
    
    @Size(max = 500, message = "Giới thiệu không được vượt quá 500 ký tự")
    private String bio;
    
    @Size(max = 200, message = "Địa chỉ đường không được vượt quá 200 ký tự")
    private String addressStreet;
    
    @Size(max = 100, message = "Phường/Xã không được vượt quá 100 ký tự")
    private String addressWard;
    
    @Size(max = 100, message = "Quận/Huyện không được vượt quá 100 ký tự")
    private String addressDistrict;
    
    @Size(max = 100, message = "Tỉnh/Thành phố không được vượt quá 100 ký tự")
    private String addressCity;
    
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã khóa xác thực phải có đúng 6 chữ số")
    private String paymentPin;
    
    // Getters and setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    
    public String getAddressStreet() { return addressStreet; }
    public void setAddressStreet(String addressStreet) { this.addressStreet = addressStreet; }
    
    public String getAddressWard() { return addressWard; }
    public void setAddressWard(String addressWard) { this.addressWard = addressWard; }
    
    public String getAddressDistrict() { return addressDistrict; }
    public void setAddressDistrict(String addressDistrict) { this.addressDistrict = addressDistrict; }
    
    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }
    
    public String getPaymentPin() { return paymentPin; }
    public void setPaymentPin(String paymentPin) { this.paymentPin = paymentPin; }
}

