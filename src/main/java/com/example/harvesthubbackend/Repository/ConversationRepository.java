package com.example.harvesthubbackend.Repository;

import com.example.harvesthubbackend.Models.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findBySellerIdOrderByUpdatedAtDesc(String sellerId);
    List<Conversation> findByCustomerIdOrderByUpdatedAtDesc(String customerId);
    Conversation findByOrderId(String orderId);
}
