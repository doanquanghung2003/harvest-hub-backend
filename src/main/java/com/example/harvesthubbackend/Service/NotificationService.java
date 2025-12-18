package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Notification;
import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    public Notification pushNotification(String userId, String title, String message, String type) {
        if (userId == null) return null;
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setTitle(title != null ? title : "Thông báo");
        notif.setMessage(message != null ? message : "");
        notif.setType(type != null ? type : "INFO");
        notif.setRead(false);
        notif.setCreatedAt(Instant.now().toEpochMilli());
        return notificationRepository.save(notif);
    }

    public Notification pushOrderNotification(String userId, String message) {
        return pushNotification(userId, "Đơn hàng mới", message, "ORDER");
    }

    public Notification pushInventoryNotification(String userId, String message) {
        return pushNotification(userId, "Tồn kho", message, "INVENTORY");
    }

    public Notification pushReviewNotification(String userId, String message) {
        return pushNotification(userId, "Đánh giá mới", message, "REVIEW");
    }

    public Notification pushSystemNotification(String userId, String message) {
        return pushNotification(userId, "Thông báo hệ thống", message, "SYSTEM");
    }
    
    public Notification pushVoucherNotification(String userId, String title, String message) {
        return pushNotification(userId, title, message, "VOUCHER");
    }

    public Notification pushOrderStatusNotification(Order order, String status) {
        if (order == null || order.getUserId() == null) return null;
        String shortId = buildOrderShortId(order.getId());
        String normalized = status != null ? status.toLowerCase() : "";
        String title;
        String message;
        switch (normalized) {
            case "confirmed":
                title = "Đơn hàng đã được xác nhận";
                message = String.format("Đơn #%s đã được người bán xác nhận và chuẩn bị đóng gói.", shortId);
                break;
            case "packed":
                title = "Đơn hàng đang đóng gói";
                message = String.format("Đơn #%s đang được đóng gói cẩn thận để bàn giao cho đơn vị vận chuyển.", shortId);
                break;
            case "shipping":
                title = "Đơn hàng đang giao";
                message = String.format("Đơn #%s đã rời kho và đang trên đường giao đến bạn.", shortId);
                break;
            case "delivered":
                title = "Đơn hàng đã giao thành công";
                message = String.format("Đơn #%s đã được giao thành công. Cảm ơn bạn đã mua sắm!", shortId);
                break;
            case "cancelled":
                title = "Đơn hàng đã bị hủy";
                message = String.format("Đơn #%s đã được hủy theo yêu cầu. Nếu cần hỗ trợ, vui lòng liên hệ chúng tôi.", shortId);
                break;
            case "returned":
                title = "Đơn hàng đã được hoàn trả";
                message = String.format("Đơn #%s đã chuyển sang trạng thái hoàn trả. Chúng tôi sẽ cập nhật khi có thông tin mới.", shortId);
                break;
            case "refunded":
                title = "Hoàn tiền đơn hàng";
                message = String.format("Đơn #%s đã được hoàn tiền. Vui lòng kiểm tra phương thức thanh toán của bạn.", shortId);
                break;
            default:
                title = "Đơn hàng cập nhật trạng thái";
                message = String.format("Đơn #%s đã chuyển sang trạng thái '%s'.", shortId, status);
                break;
        }
        return pushNotification(order.getUserId(), title, message, "ORDER");
    }

    private String buildOrderShortId(String id) {
        if (id == null) return "";
        String trimmed = id.replaceAll("[^A-Za-z0-9]", "");
        if (trimmed.length() <= 6) return trimmed.toUpperCase();
        return trimmed.substring(trimmed.length() - 6).toUpperCase();
    }
}


