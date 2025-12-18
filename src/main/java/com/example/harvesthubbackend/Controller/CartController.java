package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Cart;
import com.example.harvesthubbackend.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public List<Cart> getAll() { return cartService.getAll(); }

    @GetMapping("/{id}")
    public Cart getById(@PathVariable String id) { return cartService.getById(id); }

    @GetMapping("/user/{userId}")
    public List<Cart> getByUser(@PathVariable String userId) { return cartService.getByUserId(userId); }

    @PostMapping
    public Cart create(@RequestBody Cart cart) { return cartService.create(cart); }

    @PutMapping("/{id}")
    public Cart update(@PathVariable String id, @RequestBody Cart cart) { return cartService.update(id, cart); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) { cartService.delete(id); }

    // Convenience endpoints for cart operations by userId
    @PostMapping("/user/{userId}/items")
    public Cart addItem(
            @PathVariable String userId,
            @RequestParam String productId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        return cartService.addItem(userId, productId, quantity);
    }

    @PutMapping("/user/{userId}/items/{productId}")
    public Cart updateQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestParam int quantity
    ) {
        return cartService.updateItemQuantity(userId, productId, quantity);
    }

    @DeleteMapping("/user/{userId}/items/{productId}")
    public Cart removeItem(
            @PathVariable String userId,
            @PathVariable String productId
    ) {
        return cartService.removeItem(userId, productId);
    }

    @DeleteMapping("/user/{userId}/clear")
    public Cart clear(@PathVariable String userId) {
        return cartService.clearCart(userId);
    }
    
    // Apply voucher to cart
    @PostMapping("/user/{userId}/apply-voucher")
    public Cart applyVoucher(
            @PathVariable String userId,
            @RequestParam String voucherCode
    ) {
        try {
            return cartService.applyVoucher(userId, voucherCode);
        } catch (Exception e) {
            throw new RuntimeException("Không thể áp dụng voucher: " + e.getMessage());
        }
    }
    
    // Remove voucher from cart
    @DeleteMapping("/user/{userId}/voucher")
    public Cart removeVoucher(@PathVariable String userId) {
        return cartService.removeVoucher(userId);
    }
}


