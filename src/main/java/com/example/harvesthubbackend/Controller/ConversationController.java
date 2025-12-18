package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Conversation;
import com.example.harvesthubbackend.Models.Conversation.Message;
import com.example.harvesthubbackend.Service.ConversationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/api/messages", "/api/v1/messages"})
@CrossOrigin(origins = "*")
public class ConversationController {
    @Autowired
    private ConversationService conversationService;

    @GetMapping("/seller/{sellerId}")
    public List<Conversation> getSellerConversations(@PathVariable String sellerId) {
        return conversationService.getConversationsBySeller(sellerId);
    }
    @GetMapping("/customer/{customerId}")
    public List<Conversation> getCustomerConversations(@PathVariable String customerId) {
        return conversationService.getConversationsByCustomer(customerId);
    }
    @GetMapping("/{conversationId}")
    public Conversation getConversation(@PathVariable String conversationId) {
        return conversationService.getById(conversationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
    }
    @PostMapping("/{conversationId}/send")
    public Conversation sendMessage(@PathVariable String conversationId, @RequestBody Message message) {
        return conversationService.sendMessage(conversationId, message);
    }
    @PostMapping("/create")
    public Conversation createConversation(@RequestParam String sellerId, @RequestParam String customerId, @RequestParam String orderId) {
        return conversationService.createConversation(sellerId, customerId, orderId);
    }
}
