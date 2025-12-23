package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ChatService {
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Kết quả xử lý tin nhắn
     */
    public static class ChatResponse {
        private String text;
        private List<Product> products;
        
        public ChatResponse(String text, List<Product> products) {
            this.text = text;
            this.products = products;
        }
        
        public String getText() { return text; }
        public List<Product> getProducts() { return products; }
    }
    
    /**
     * Xử lý tin nhắn từ người dùng và trả về phản hồi
     */
    public ChatResponse processMessageWithProducts(String message, List<Map<String, String>> history) {
        if (message == null || message.trim().isEmpty()) {
            return new ChatResponse("Xin lỗi, tôi không hiểu. Bạn có thể nhập lại câu hỏi không?", null);
        }
        
        String lowerMessage = message.toLowerCase().trim();
        
        // Kiểm tra câu hỏi về sản phẩm cụ thể (có [tên sản phẩm] không?)
        if (lowerMessage.contains("có") && (lowerMessage.contains("không") || lowerMessage.contains("không?"))) {
            String productName = extractProductName(message);
            if (productName != null && !productName.isEmpty()) {
                return searchAndRespondAboutProductWithData(productName);
            }
        }
        
        // Kiểm tra câu hỏi tìm kiếm sản phẩm
        if (lowerMessage.contains("tìm") || lowerMessage.contains("bán") || lowerMessage.contains("có bán")) {
            String productName = extractProductName(message);
            if (productName != null && !productName.isEmpty()) {
                return searchAndRespondAboutProductWithData(productName);
            }
        }
        
        // Các câu hỏi khác không có sản phẩm
        return new ChatResponse(processMessage(message, history), null);
    }
    
    /**
     * Xử lý tin nhắn từ người dùng và trả về phản hồi (backward compatibility)
     */
    public String processMessage(String message, List<Map<String, String>> history) {
        if (message == null || message.trim().isEmpty()) {
            return "Xin lỗi, tôi không hiểu. Bạn có thể nhập lại câu hỏi không?";
        }
        
        String lowerMessage = message.toLowerCase().trim();
        
        // Kiểm tra câu hỏi về sản phẩm cụ thể (có [tên sản phẩm] không?)
        if (lowerMessage.contains("có") && (lowerMessage.contains("không") || lowerMessage.contains("không?"))) {
            // Tìm tên sản phẩm trong câu hỏi
            String productName = extractProductName(message);
            if (productName != null && !productName.isEmpty()) {
                return searchAndRespondAboutProduct(productName);
            }
        }
        
        // Kiểm tra câu hỏi tìm kiếm sản phẩm
        if (lowerMessage.contains("tìm") || lowerMessage.contains("bán") || lowerMessage.contains("có bán")) {
            String productName = extractProductName(message);
            if (productName != null && !productName.isEmpty()) {
                return searchAndRespondAboutProduct(productName);
            }
        }
        
        // Phát hiện các chủ đề phổ biến và trả về phản hồi phù hợp
        if (lowerMessage.contains("chào") || lowerMessage.contains("hello") || lowerMessage.contains("xin chào")) {
            return "Xin chào! Tôi là trợ lý AI của Harvest Hub. Tôi có thể giúp bạn tìm hiểu về sản phẩm nông sản, đặt hàng, hoặc giải đáp thắc mắc. Bạn cần hỗ trợ gì hôm nay?";
        }
        
        if (lowerMessage.contains("giá") || lowerMessage.contains("price") || lowerMessage.contains("cost")) {
            return "Bạn có thể xem giá sản phẩm trực tiếp trên trang sản phẩm. Nếu bạn đang quan tâm đến một sản phẩm cụ thể, vui lòng cho tôi biết tên sản phẩm để tôi có thể hỗ trợ tốt hơn.";
        }
        
        if (lowerMessage.contains("đặt hàng") || lowerMessage.contains("mua") || lowerMessage.contains("order") || lowerMessage.contains("checkout")) {
            return "Để đặt hàng, bạn có thể:\n1. Thêm sản phẩm vào giỏ hàng\n2. Chọn phương thức thanh toán\n3. Xác nhận đơn hàng\n\nBạn cần hỗ trợ thêm về bước nào không?";
        }
        
        if (lowerMessage.contains("vận chuyển") || lowerMessage.contains("ship") || lowerMessage.contains("giao hàng") || lowerMessage.contains("phí ship")) {
            return "Chúng tôi hỗ trợ giao hàng toàn quốc. Thời gian giao hàng thường từ 2-5 ngày làm việc tùy theo khu vực. Phí vận chuyển sẽ được tính dựa trên địa chỉ giao hàng của bạn.";
        }
        
        if (lowerMessage.contains("đổi trả") || lowerMessage.contains("hoàn") || lowerMessage.contains("refund") || lowerMessage.contains("trả hàng")) {
            return "Chúng tôi chấp nhận đổi trả trong vòng 7 ngày kể từ ngày nhận hàng nếu sản phẩm còn nguyên vẹn, chưa sử dụng. Vui lòng liên hệ hotline hoặc email để được hỗ trợ đổi trả.";
        }
        
        if (lowerMessage.contains("sản phẩm") || lowerMessage.contains("product") || lowerMessage.contains("hàng") || lowerMessage.contains("mặt hàng")) {
            return "Chúng tôi có nhiều loại sản phẩm nông sản như rau củ tươi, trái cây, hạt giống, và dụng cụ nông nghiệp. Bạn có thể duyệt danh mục sản phẩm trên trang chủ để tìm sản phẩm phù hợp.";
        }
        
        if (lowerMessage.contains("tài khoản") || lowerMessage.contains("account") || lowerMessage.contains("đăng nhập") || lowerMessage.contains("đăng ký")) {
            return "Bạn có thể đăng nhập hoặc đăng ký tài khoản mới ở góc trên bên phải trang web. Nếu gặp vấn đề, vui lòng liên hệ bộ phận hỗ trợ.";
        }
        
        if (lowerMessage.contains("voucher") || lowerMessage.contains("mã giảm giá") || lowerMessage.contains("khuyến mãi") || lowerMessage.contains("giảm giá")) {
            return "Chúng tôi thường xuyên có các chương trình khuyến mãi và voucher giảm giá. Bạn có thể xem các voucher hiện có trong phần \"Voucher của tôi\" sau khi đăng nhập.";
        }
        
        if (lowerMessage.contains("thanh toán") || lowerMessage.contains("payment") || lowerMessage.contains("pay")) {
            return "Chúng tôi hỗ trợ nhiều phương thức thanh toán:\n- Thanh toán khi nhận hàng (COD)\n- Thanh toán qua ví điện tử\n- Thanh toán qua thẻ ngân hàng\n- Thanh toán online qua VNPay";
        }
        
        if (lowerMessage.contains("giỏ hàng") || lowerMessage.contains("cart")) {
            return "Bạn có thể thêm sản phẩm vào giỏ hàng bằng cách nhấn nút \"Thêm vào giỏ hàng\" trên trang sản phẩm. Sau đó vào giỏ hàng để xem và chỉnh sửa các sản phẩm trước khi đặt hàng.";
        }
        
        if (lowerMessage.contains("đơn hàng") || lowerMessage.contains("order") || lowerMessage.contains("tracking")) {
            return "Bạn có thể xem trạng thái đơn hàng trong phần \"Đơn hàng của tôi\" sau khi đăng nhập. Chúng tôi sẽ cập nhật trạng thái đơn hàng theo thời gian thực.";
        }
        
        // Phản hồi mặc định
        return "Cảm ơn bạn đã liên hệ! Tôi hiểu bạn đang hỏi về: \"" + message + "\". Để tôi có thể hỗ trợ tốt hơn, bạn có thể:\n" +
                "- Mô tả chi tiết hơn về vấn đề\n" +
                "- Liên hệ hotline: 1900-xxxx\n" +
                "- Gửi email: support@harvesthub.com\n\n" +
                "Tôi có thể giúp bạn về sản phẩm, đặt hàng, vận chuyển, thanh toán, hoặc các vấn đề khác. Bạn cần hỗ trợ gì cụ thể?";
    }
    
    /**
     * Trích xuất tên sản phẩm từ câu hỏi
     */
    private String extractProductName(String message) {
        // Danh sách các từ bỏ qua
        String[] skipWords = {"có", "không", "bán", "tìm", "mua", "giá", "của", "về", "cho", "với", "và", "hoặc", "hay", "là", "gì", "nào", "đâu", "bao nhiêu"};
        
        // Loại bỏ các từ bỏ qua và lấy từ còn lại
        String[] words = message.split("\\s+");
        List<String> productWords = new ArrayList<>();
        
        for (String word : words) {
            String lowerWord = word.toLowerCase().trim();
            boolean shouldSkip = false;
            for (String skip : skipWords) {
                if (lowerWord.equals(skip) || lowerWord.equals(skip + "?")) {
                    shouldSkip = true;
                    break;
                }
            }
            if (!shouldSkip && !lowerWord.isEmpty() && lowerWord.length() > 1) {
                productWords.add(word);
            }
        }
        
        if (productWords.isEmpty()) {
            return null;
        }
        
        // Lấy từ đầu tiên hoặc 2-3 từ đầu làm tên sản phẩm
        int maxWords = Math.min(productWords.size(), 3);
        return String.join(" ", productWords.subList(0, maxWords));
    }
    
    /**
     * Tìm kiếm sản phẩm và trả về phản hồi kèm dữ liệu sản phẩm
     */
    private ChatResponse searchAndRespondAboutProductWithData(String productName) {
        try {
            if (productRepository == null) {
                return new ChatResponse(
                    "Hiện tại tôi chưa thể tìm kiếm sản phẩm trong hệ thống. Bạn có thể tìm kiếm sản phẩm \"" + productName + "\" trên trang chủ hoặc trong danh mục sản phẩm.",
                    null
                );
            }
            
            // Tìm kiếm sản phẩm theo tên (không phân biệt hoa thường)
            List<Product> products = productRepository.findAll();
            List<Product> matchedProducts = new ArrayList<>();
            
            String lowerProductName = productName.toLowerCase();
            for (Product product : products) {
                if (product.getName() != null && product.getName().toLowerCase().contains(lowerProductName)) {
                    // Chỉ lấy sản phẩm đang active và còn hàng
                    if ("active".equals(product.getStatus()) && product.getStock() != null && product.getStock() > 0) {
                        matchedProducts.add(product);
                    }
                }
            }
            
            if (matchedProducts.isEmpty()) {
                return new ChatResponse(
                    "Xin lỗi, hiện tại chúng tôi chưa có sản phẩm \"" + productName + "\" trong kho. Bạn có thể:\n" +
                    "- Tìm kiếm các sản phẩm tương tự trên trang chủ\n" +
                    "- Xem các danh mục sản phẩm khác\n" +
                    "- Liên hệ hotline: 1900-xxxx để được tư vấn thêm",
                    null
                );
            }
            
            // Giới hạn số lượng sản phẩm trả về
            List<Product> productsToReturn = matchedProducts.size() > 5 
                ? matchedProducts.subList(0, 5) 
                : matchedProducts;
            
            if (matchedProducts.size() == 1) {
                Product product = matchedProducts.get(0);
                String responseText = "Có! Chúng tôi có sản phẩm \"" + product.getName() + "\".\n\n" +
                       "Thông tin sản phẩm:\n" +
                       "- Giá: " + formatPrice(product.getPrice()) + "\n" +
                       "- Số lượng còn lại: " + product.getStock() + " " + (product.getUnit() != null ? product.getUnit() : "sản phẩm") + "\n" +
                       "- Danh mục: " + (product.getCategory() != null ? product.getCategory() : "Nông sản") + "\n\n" +
                       "Bạn có thể xem chi tiết và đặt mua sản phẩm này bên dưới.";
                return new ChatResponse(responseText, productsToReturn);
            } else {
                StringBuilder response = new StringBuilder();
                response.append("Có! Chúng tôi có ").append(matchedProducts.size()).append(" sản phẩm liên quan đến \"").append(productName).append("\":\n\n");
                
                int count = 0;
                for (Product product : productsToReturn) {
                    response.append("- ").append(product.getName())
                           .append(" - ").append(formatPrice(product.getPrice()))
                           .append(" (Còn ").append(product.getStock()).append(" ").append(product.getUnit() != null ? product.getUnit() : "sản phẩm").append(")\n");
                    count++;
                }
                
                if (matchedProducts.size() > 5) {
                    response.append("\n... và ").append(matchedProducts.size() - 5).append(" sản phẩm khác.\n");
                }
                
                response.append("\nBạn có thể xem chi tiết và đặt mua các sản phẩm này bên dưới.");
                return new ChatResponse(response.toString(), productsToReturn);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
            return new ChatResponse(
                "Có! Chúng tôi có nhiều loại sản phẩm nông sản. Bạn có thể tìm kiếm sản phẩm \"" + productName + "\" trên trang chủ hoặc trong danh mục sản phẩm để xem chi tiết.",
                null
            );
        }
    }
    
    /**
     * Tìm kiếm sản phẩm và trả về phản hồi (backward compatibility)
     */
    private String searchAndRespondAboutProduct(String productName) {
        try {
            if (productRepository == null) {
                return "Hiện tại tôi chưa thể tìm kiếm sản phẩm trong hệ thống. Bạn có thể tìm kiếm sản phẩm \"" + productName + "\" trên trang chủ hoặc trong danh mục sản phẩm.";
            }
            
            // Tìm kiếm sản phẩm theo tên (không phân biệt hoa thường)
            List<Product> products = productRepository.findAll();
            List<Product> matchedProducts = new ArrayList<>();
            
            String lowerProductName = productName.toLowerCase();
            for (Product product : products) {
                if (product.getName() != null && product.getName().toLowerCase().contains(lowerProductName)) {
                    // Chỉ lấy sản phẩm đang active và còn hàng
                    if ("active".equals(product.getStatus()) && product.getStock() != null && product.getStock() > 0) {
                        matchedProducts.add(product);
                    }
                }
            }
            
            if (matchedProducts.isEmpty()) {
                return "Xin lỗi, hiện tại chúng tôi chưa có sản phẩm \"" + productName + "\" trong kho. Bạn có thể:\n" +
                       "- Tìm kiếm các sản phẩm tương tự trên trang chủ\n" +
                       "- Xem các danh mục sản phẩm khác\n" +
                       "- Liên hệ hotline: 1900-xxxx để được tư vấn thêm";
            }
            
            if (matchedProducts.size() == 1) {
                Product product = matchedProducts.get(0);
                return "Có! Chúng tôi có sản phẩm \"" + product.getName() + "\".\n\n" +
                       "Thông tin sản phẩm:\n" +
                       "- Giá: " + formatPrice(product.getPrice()) + "\n" +
                       "- Số lượng còn lại: " + product.getStock() + " " + (product.getUnit() != null ? product.getUnit() : "sản phẩm") + "\n" +
                       "- Danh mục: " + (product.getCategory() != null ? product.getCategory() : "Nông sản") + "\n\n" +
                       "Bạn có thể xem chi tiết và đặt mua sản phẩm này trên trang chủ của chúng tôi.";
            } else {
                StringBuilder response = new StringBuilder();
                response.append("Có! Chúng tôi có ").append(matchedProducts.size()).append(" sản phẩm liên quan đến \"").append(productName).append("\":\n\n");
                
                int count = 0;
                for (Product product : matchedProducts) {
                    if (count >= 5) break; // Chỉ hiển thị tối đa 5 sản phẩm
                    response.append("- ").append(product.getName())
                           .append(" - ").append(formatPrice(product.getPrice()))
                           .append(" (Còn ").append(product.getStock()).append(" ").append(product.getUnit() != null ? product.getUnit() : "sản phẩm").append(")\n");
                    count++;
                }
                
                if (matchedProducts.size() > 5) {
                    response.append("\n... và ").append(matchedProducts.size() - 5).append(" sản phẩm khác.\n");
                }
                
                response.append("\nBạn có thể xem chi tiết và đặt mua các sản phẩm này trên trang chủ.");
                return response.toString();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm sản phẩm: " + e.getMessage());
            return "Có! Chúng tôi có nhiều loại sản phẩm nông sản. Bạn có thể tìm kiếm sản phẩm \"" + productName + "\" trên trang chủ hoặc trong danh mục sản phẩm để xem chi tiết.";
        }
    }
    
    /**
     * Format giá tiền
     */
    private String formatPrice(Double price) {
        if (price == null) {
            return "Liên hệ";
        }
        return String.format("%,.0f", price) + " đ";
    }
    
    /**
     * Kiểm tra health của service
     */
    public boolean isHealthy() {
        return true;
    }
}

