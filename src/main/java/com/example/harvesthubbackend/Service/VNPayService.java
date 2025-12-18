package com.example.harvesthubbackend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {
    
    @Value("${vnpay.tmnCode:DEMO}")
    private String tmnCode;
    
    @Value("${vnpay.hashSecret:DEMO}")
    private String hashSecret;
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpUrl;
    
    @Value("${vnpay.returnUrl:http://localhost:5173/payment/return}")
    private String vnpReturnUrl;
    
    /**
     * Tạo URL thanh toán VNPay
     */
    public String createPaymentUrl(String orderId, double amount, String orderInfo, String ipAddress) {
        try {
            String vnp_TxnRef = orderId + "_" + System.currentTimeMillis();
            String vnp_Amount = String.valueOf((long)(amount * 100)); // Convert to cents
            
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", tmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
            vnp_Params.put("vnp_IpAddr", ipAddress);
            
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
            
            // Sort params
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            
            String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(vnp_SecureHash);
            
            String paymentUrl = vnpUrl + "?" + query.toString();
            return paymentUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Xác thực callback từ VNPay
     */
    public boolean verifyPayment(Map<String, String> params) {
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            
            String checkSum = hmacSHA512(hashSecret, hashData.toString());
            return checkSum.equals(vnp_SecureHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hash = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Lấy transaction ID từ response
     */
    public String getTransactionId(Map<String, String> params) {
        return params.get("vnp_TransactionNo");
    }
    
    /**
     * Lấy response code từ VNPay
     */
    public String getResponseCode(Map<String, String> params) {
        return params.get("vnp_ResponseCode");
    }
    
    /**
     * Kiểm tra thanh toán thành công (ResponseCode = "00")
     */
    public boolean isPaymentSuccess(Map<String, String> params) {
        String responseCode = getResponseCode(params);
        return "00".equals(responseCode);
    }
}

