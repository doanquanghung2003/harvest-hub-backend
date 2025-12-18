package com.example.harvesthubbackend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock Payment Service để test thanh toán online
 * Sử dụng khi không thể truy cập VNPay sandbox hoặc chưa có tài khoản
 */
@Service
public class MockPaymentService {
    
    @Value("${mock.payment.returnUrl:http://localhost:5173/payment/return}")
    private String mockReturnUrl;
    
    @Value("${mock.payment.baseUrl:http://localhost:5173}")
    private String mockBaseUrl;
    
    /**
     * Tạo URL thanh toán mock (giả lập VNPay)
     */
    public String createMockPaymentUrl(String orderId, double amount, String orderInfo, String ipAddress) {
        return createMockPaymentUrl(orderId, amount, orderInfo, ipAddress, null);
    }
    
    /**
     * Tạo URL thanh toán mock (giả lập VNPay) với bankCardId
     */
    public String createMockPaymentUrl(String orderId, double amount, String orderInfo, String ipAddress, String bankCardId) {
        try {
            // Tạo URL mock với các tham số cần thiết
            Map<String, String> params = new HashMap<>();
            params.put("vnp_Amount", String.valueOf((long)(amount * 100)));
            params.put("vnp_Command", "pay");
            params.put("vnp_CreateDate", String.valueOf(System.currentTimeMillis()));
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_IpAddr", ipAddress);
            params.put("vnp_Locale", "vn");
            params.put("vnp_OrderInfo", orderInfo);
            params.put("vnp_OrderType", "other");
            params.put("vnp_ReturnUrl", mockReturnUrl);
            params.put("vnp_TmnCode", "DEMO");
            params.put("vnp_TxnRef", orderId + "_" + System.currentTimeMillis());
            params.put("vnp_Version", "2.1.0");
            
            // Add bankCardId if provided
            if (bankCardId != null && !bankCardId.isEmpty()) {
                params.put("bankCardId", bankCardId);
            }
            
            // Build query string
            StringBuilder query = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (query.length() > 0) {
                    query.append("&");
                }
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.toString()));
                query.append("=");
                query.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString()));
            }
            
            // Tạo URL mock - redirect đến trang mock payment
            // Sử dụng baseUrl từ config hoặc fallback
            String mockUrl = mockBaseUrl + "/payment/mock?" + query.toString();
            return mockUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Xác thực payment mock (luôn trả về true cho test)
     */
    public boolean verifyMockPayment(Map<String, String> params) {
        // Mock verification - luôn trả về true
        return true;
    }
    
    /**
     * Kiểm tra thanh toán thành công (mock)
     */
    public boolean isPaymentSuccess(Map<String, String> params) {
        // Mock - luôn trả về true nếu có vnp_ResponseCode = "00"
        String responseCode = params.get("vnp_ResponseCode");
        return "00".equals(responseCode) || responseCode == null;
    }
    
    /**
     * Lấy transaction ID từ mock payment
     */
    public String getTransactionId(Map<String, String> params) {
        return params.getOrDefault("vnp_TransactionNo", "MOCK_" + System.currentTimeMillis());
    }
}

