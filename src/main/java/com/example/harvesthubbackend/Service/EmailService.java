package com.example.harvesthubbackend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:no-reply@harvest-hub.local}")
    private String fromEmail;

    @Value("${frontend.base-url:http://localhost:8082}")
    private String frontendBaseUrl;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu - Harvest Hub");
        message.setText("Bạn đã yêu cầu đặt lại mật khẩu. Nhấp vào liên kết sau để đặt lại mật khẩu của bạn:\n\n"
                + resetLink +
                "\n\nLiên kết này sẽ hết hạn sau 1 giờ. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.");
        mailSender.send(message);
    }

    public void sendVerificationCodeEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mã xác minh email - Harvest Hub");
        message.setText("Xin chào,\n\n" +
                "Mã xác minh email của bạn là: " + code +
                "\n\nMã này sẽ hết hạn sau 10 phút. " +
                "Nếu bạn không yêu cầu mã xác minh này, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\nĐội ngũ Harvest Hub");
        mailSender.send(message);
    }

    // Gửi email xác nhận đơn hàng
    public void sendOrderConfirmationEmail(String toEmail, String orderId, Map<String, Object> orderDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đơn hàng #" + orderId + " - Harvest Hub");
            
            String htmlContent = buildOrderConfirmationEmail(orderId, orderDetails);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error sending order confirmation email: " + e.getMessage());
        }
    }

    // Gửi email cập nhật trạng thái đơn hàng
    public void sendOrderStatusUpdateEmail(String toEmail, String orderId, String status, String trackingNumber) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Cập nhật đơn hàng #" + orderId + " - Harvest Hub");
            
            String htmlContent = buildOrderStatusUpdateEmail(orderId, status, trackingNumber);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error sending order status update email: " + e.getMessage());
        }
    }

    // Gửi email thông báo seller được duyệt
    public void sendSellerApprovalEmail(String toEmail, String sellerName, boolean approved, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(approved ? "Đăng ký bán hàng đã được duyệt - Harvest Hub" 
                                      : "Thông báo về đăng ký bán hàng - Harvest Hub");
            
            String htmlContent = buildSellerApprovalEmail(sellerName, approved, reason);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error sending seller approval email: " + e.getMessage());
        }
    }

    // Gửi email thông báo sản phẩm được duyệt
    public void sendProductApprovalEmail(String toEmail, String productName, boolean approved, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(approved ? "Sản phẩm đã được duyệt - Harvest Hub" 
                                      : "Thông báo về sản phẩm - Harvest Hub");
            
            String htmlContent = buildProductApprovalEmail(productName, approved, reason);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error sending product approval email: " + e.getMessage());
        }
    }

    // Gửi email thông báo hết hàng
    public void sendLowStockAlertEmail(String toEmail, String productName, int currentStock) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Cảnh báo: Sản phẩm sắp hết hàng - Harvest Hub");
        message.setText("Sản phẩm '" + productName + "' đang sắp hết hàng. Số lượng còn lại: " + currentStock + 
                       "\n\nVui lòng nhập thêm hàng để tiếp tục bán.");
        mailSender.send(message);
    }

    // Build HTML email templates
    private String buildOrderConfirmationEmail(String orderId, Map<String, Object> orderDetails) {
        return "<html><body style='font-family: Arial, sans-serif;'>" +
               "<h2 style='color: #2d8659;'>Xác nhận đơn hàng #" + orderId + "</h2>" +
               "<p>Cảm ơn bạn đã đặt hàng tại Harvest Hub!</p>" +
               "<p>Đơn hàng của bạn đã được xác nhận và đang được xử lý.</p>" +
               "<p><strong>Mã đơn hàng:</strong> " + orderId + "</p>" +
               "<p><strong>Tổng tiền:</strong> " + orderDetails.getOrDefault("totalPrice", "0") + " VND</p>" +
               "<p>Bạn có thể theo dõi đơn hàng tại: <a href='" + frontendBaseUrl + "/orders/" + orderId + "'>Xem đơn hàng</a></p>" +
               "<p>Trân trọng,<br>Đội ngũ Harvest Hub</p>" +
               "</body></html>";
    }

    private String buildOrderStatusUpdateEmail(String orderId, String status, String trackingNumber) {
        String statusText = switch (status) {
            case "confirmed" -> "đã được xác nhận";
            case "packed" -> "đã được đóng gói";
            case "shipping" -> "đang được vận chuyển";
            case "delivered" -> "đã được giao";
            case "cancelled" -> "đã bị hủy";
            default -> "đã được cập nhật";
        };
        
        String html = "<html><body style='font-family: Arial, sans-serif;'>" +
                     "<h2 style='color: #2d8659;'>Cập nhật đơn hàng #" + orderId + "</h2>" +
                     "<p>Đơn hàng của bạn " + statusText + ".</p>";
        
        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            html += "<p><strong>Mã vận đơn:</strong> " + trackingNumber + "</p>";
        }
        
        html += "<p>Bạn có thể theo dõi đơn hàng tại: <a href='" + frontendBaseUrl + "/orders/" + orderId + "'>Xem đơn hàng</a></p>" +
                "<p>Trân trọng,<br>Đội ngũ Harvest Hub</p>" +
                "</body></html>";
        
        return html;
    }

    private String buildSellerApprovalEmail(String sellerName, boolean approved, String reason) {
        if (approved) {
            return "<html><body style='font-family: Arial, sans-serif;'>" +
                   "<h2 style='color: #2d8659;'>Chúc mừng! Đăng ký bán hàng đã được duyệt</h2>" +
                   "<p>Xin chào " + sellerName + ",</p>" +
                   "<p>Đăng ký bán hàng của bạn đã được duyệt thành công!</p>" +
                   "<p>Bạn có thể bắt đầu bán hàng ngay bây giờ.</p>" +
                   "<p><a href='" + frontendBaseUrl + "/seller/dashboard'>Truy cập Dashboard</a></p>" +
                   "<p>Trân trọng,<br>Đội ngũ Harvest Hub</p>" +
                   "</body></html>";
        } else {
            return "<html><body style='font-family: Arial, sans-serif;'>" +
                   "<h2 style='color: #d32f2f;'>Thông báo về đăng ký bán hàng</h2>" +
                   "<p>Xin chào " + sellerName + ",</p>" +
                   "<p>Rất tiếc, đăng ký bán hàng của bạn đã bị từ chối.</p>" +
                   (reason != null && !reason.isEmpty() ? "<p><strong>Lý do:</strong> " + reason + "</p>" : "") +
                   "<p>Vui lòng kiểm tra lại thông tin và đăng ký lại.</p>" +
                   "<p>Trân trọng,<br>Đội ngũ Harvest Hub</p>" +
                   "</body></html>";
        }
    }

    private String buildProductApprovalEmail(String productName, boolean approved, String reason) {
        if (approved) {
            return "<html><body style='font-family: Arial, sans-serif;'>" +
                   "<h2 style='color: #2d8659;'>Sản phẩm đã được duyệt</h2>" +
                   "<p>Sản phẩm '" + productName + "' của bạn đã được duyệt và đã được đăng bán.</p>" +
                   "<p><a href='" + frontendBaseUrl + "/seller/products'>Xem sản phẩm</a></p>" +
                   "<p>Trân trọng,<br>Đội ngũ Harvest Hub</p>" +
                   "</body></html>";
        } else {
            return "<html><body style='font-family: Arial, sans-serif;'>" +
                   "<h2 style='color: #d32f2f;'>Thông báo về sản phẩm</h2>" +
                   "<p>Sản phẩm '" + productName + "' của bạn đã bị từ chối.</p>" +
                   (reason != null && !reason.isEmpty() ? "<p><strong>Lý do:</strong> " + reason + "</p>" : "") +
                   "<p>Vui lòng chỉnh sửa và gửi lại để được duyệt.</p>" +
                   "<p>Trân trọng,<br>Đội ngũ Harvest Hub</p>" +
                   "</body></html>";
        }
    }
}
