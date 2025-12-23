package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Exception.ApiResponse;
import com.example.harvesthubbackend.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    /**
     * Endpoint để gửi tin nhắn và nhận phản hồi từ AI
     */
    @PostMapping("/ai")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(
            @RequestBody Map<String, Object> request) {
        try {
            System.out.println("📥 Nhận tin nhắn từ frontend: " + request);
            String message = (String) request.get("message");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");
            
            if (message == null || message.trim().isEmpty()) {
                System.err.println("❌ Tin nhắn trống");
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                        com.example.harvesthubbackend.Exception.ErrorCode.INVALID_REQUEST,
                        "Tin nhắn không được để trống"
                    )
                );
            }
            
            System.out.println("💬 Xử lý tin nhắn: " + message);
            
            // Sử dụng method mới để lấy cả products
            com.example.harvesthubbackend.Service.ChatService.ChatResponse chatResponse = chatService.processMessageWithProducts(message, history);
            System.out.println("✅ Phản hồi: " + chatResponse.getText());
            if (chatResponse.getProducts() != null) {
                System.out.println("📦 Số lượng sản phẩm: " + chatResponse.getProducts().size());
            }
            
            // Tạo response data với text và products
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("response", chatResponse.getText());
            responseData.put("message", chatResponse.getText());
            
            // Chuyển đổi products sang format Map để JSON serialization
            if (chatResponse.getProducts() != null && !chatResponse.getProducts().isEmpty()) {
                List<Map<String, Object>> productsData = new ArrayList<>();
                for (com.example.harvesthubbackend.Models.Product product : chatResponse.getProducts()) {
                    Map<String, Object> productData = new HashMap<>();
                    productData.put("id", product.getId());
                    productData.put("name", product.getName());
                    productData.put("price", product.getPrice());
                    productData.put("originalPrice", product.getOriginalPrice());
                    productData.put("stock", product.getStock());
                    productData.put("unit", product.getUnit());
                    productData.put("category", product.getCategory());
                    productData.put("description", product.getDescription());
                    productData.put("shortDescription", product.getShortDescription());
                    // Lấy ảnh đầu tiên nếu có
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        productData.put("image", product.getImages().get(0));
                    } else {
                        productData.put("image", null);
                    }
                    productsData.add(productData);
                }
                responseData.put("products", productsData);
            }
            
            return ResponseEntity.ok(ApiResponse.success(responseData));
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xử lý tin nhắn: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                ApiResponse.error(
                    com.example.harvesthubbackend.Exception.ErrorCode.INTERNAL_SERVER_ERROR,
                    "Có lỗi xảy ra khi xử lý tin nhắn: " + e.getMessage()
                )
            );
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        boolean isHealthy = chatService.isHealthy();
        if (isHealthy) {
            return ResponseEntity.ok(ApiResponse.success(
                Map.of("status", "healthy", "message", "Chat service is running")
            ));
        } else {
            return ResponseEntity.status(503).body(
                ApiResponse.error(
                    com.example.harvesthubbackend.Exception.ErrorCode.INTERNAL_SERVER_ERROR,
                    "Chat service is not available"
                )
            );
        }
    }
}

