package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Notification;
import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.Repository.NotificationRepository;
import com.example.harvesthubbackend.Service.NotificationService;
import com.example.harvesthubbackend.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/api/notifications", "/api/v1/notifications"})
@CrossOrigin(origins = "*")
public class NotificationController {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Notification> getAllNotifications() {
        // Admin endpoint - get all notifications
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getNotifications(@PathVariable String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<Notification> getNotificationsUnread(@PathVariable String userId) {
        return notificationRepository.findByUserIdAndReadIsFalseOrderByCreatedAtDesc(userId);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable String id) {
        return notificationRepository.findById(id)
            .map(notification -> {
                notification.setRead(true);
                return ResponseEntity.ok(notificationRepository.save(notification));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        if (!notificationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        notificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-to-all")
    public Map<String, Object> broadcast(@RequestBody BroadcastNotificationRequest request) {
        List<User> users = userService.getAll();
        List<User> targets = users.stream()
            .filter(user -> shouldSendToTarget(user, request.getTarget()))
            .collect(Collectors.toList());
        targets.forEach(user ->
            notificationService.pushNotification(
                user.getId(),
                request.getTitle(),
                request.getMessage(),
                request.getType()
            )
        );
        return Map.of(
            "targetCount", targets.size(),
            "target", request.getTarget()
        );
    }

    private boolean shouldSendToTarget(User user, String target) {
        if (user == null) return false;
        if (target == null || target.equalsIgnoreCase("ALL")) return true;
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
        if (target.equalsIgnoreCase("CUSTOMERS")) {
            return !"SELLER".equals(role) && !"ADMIN".equals(role);
        }
        if (target.equalsIgnoreCase("SELLERS")) {
            return "SELLER".equals(role);
        }
        return true;
    }

    public static class BroadcastNotificationRequest {
        private String title;
        private String message;
        private String type;
        private String target;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }
    }
}


