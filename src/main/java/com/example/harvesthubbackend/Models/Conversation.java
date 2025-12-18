package com.example.harvesthubbackend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "conversations")
public class Conversation {
    @Id
    private String id;
    private String sellerId;
    private String customerId;
    private String orderId;
    private List<Message> messages;
    private long createdAt;
    private long updatedAt;

    public static class Message {
        private String senderId;
        private String senderName;
        private String role; // "customer" hoặc "seller"
        private String content;
        private long timestamp;

        public Message() {}
        public Message(String senderId, String senderName, String role, String content, long timestamp) {
            this.senderId = senderId;
            this.senderName = senderName;
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }
        public String getSenderId() { return senderId; }
        public void setSenderId(String senderId) { this.senderId = senderId; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    // getter/setter for Conversation
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
