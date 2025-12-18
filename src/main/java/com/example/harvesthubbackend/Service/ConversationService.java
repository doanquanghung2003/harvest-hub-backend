package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Conversation;
import com.example.harvesthubbackend.Models.Conversation.Message;
import com.example.harvesthubbackend.Repository.ConversationRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<Conversation> getConversationsBySeller(String sellerId) {
        return conversationRepository.findBySellerIdOrderByUpdatedAtDesc(sellerId);
    }
    public List<Conversation> getConversationsByCustomer(String customerId) {
        return conversationRepository.findByCustomerIdOrderByUpdatedAtDesc(customerId);
    }
    public Conversation getConversationByOrder(String orderId) {
        return conversationRepository.findByOrderId(orderId);
    }
    public Optional<Conversation> getById(String id) {
        return conversationRepository.findById(id);
    }
    public Conversation save(Conversation conv) {
        conv.setUpdatedAt(Instant.now().toEpochMilli());
        Conversation saved = conversationRepository.save(conv);
        publishConversationUpdate(saved);
        return saved;
    }
    public Conversation sendMessage(String conversationId, Message message) {
        Conversation conv = conversationRepository.findById(conversationId).orElse(null);
        if (conv == null) throw new RuntimeException("Conversation not found");
        if (conv.getMessages() == null) conv.setMessages(new ArrayList<>());
        if (message.getTimestamp() <= 0) {
            message.setTimestamp(Instant.now().toEpochMilli());
        }
        conv.getMessages().add(message);
        conv.setUpdatedAt(message.getTimestamp());
        Conversation saved = conversationRepository.save(conv);
        publishConversationUpdate(saved);
        return saved;
    }
    // Tạo mới conversation nếu chưa có (theo orderId)
    public Conversation createConversation(String sellerId, String customerId, String orderId) {
        Conversation conv = new Conversation();
        conv.setSellerId(sellerId);
        conv.setCustomerId(customerId);
        conv.setOrderId(orderId);
        conv.setMessages(new ArrayList<>());
        long now = Instant.now().toEpochMilli();
        conv.setCreatedAt(now);
        conv.setUpdatedAt(now);
        Conversation saved = conversationRepository.save(conv);
        publishConversationUpdate(saved);
        return saved;
    }

    private void publishConversationUpdate(Conversation conversation) {
        if (conversation == null) {
            return;
        }
        if (conversation.getSellerId() != null) {
            messagingTemplate.convertAndSend("/topic/seller/" + conversation.getSellerId() + "/conversations", conversation);
        }
        if (conversation.getCustomerId() != null) {
            messagingTemplate.convertAndSend("/topic/customer/" + conversation.getCustomerId() + "/conversations", conversation);
        }
    }
}
