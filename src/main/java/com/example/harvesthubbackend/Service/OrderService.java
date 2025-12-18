package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Cart;
import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.Utils.OrderStatusValidator;
import com.example.harvesthubbackend.Exception.ApiException;
import com.example.harvesthubbackend.Exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Repository
@Repository
interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
}

// Service
@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private com.example.harvesthubbackend.Service.VoucherAutomationService voucherAutomationService;
    
    @Autowired
    private FlashSaleService flashSaleService;

    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order getById(String id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order create(Order order) {
        return orderRepository.save(order);
    }

    public Order update(String id, Order order) {
        order.setId(id);
        return orderRepository.save(order);
    }

    public void delete(String id) {
        orderRepository.deleteById(id);
    }

    public List<Order> getByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getBySellerId(String sellerId) {
        List<Order> all = orderRepository.findAll();
        List<Order> result = new ArrayList<>();
        for (Order o : all) {
            if (o.getItems() == null) continue;
            boolean belongs = false;
            for (Order.OrderItem item : o.getItems()) {
                try {
                    Product p = productService.getById(item.getProductId());
                    if (p != null && sellerId.equals(p.getSellerId())) {
                        belongs = true;
                        break;
                    }
                } catch (Exception ignored) {}
            }
            if (belongs) result.add(o);
        }
        return result;
    }

    public Order updateStatus(String orderId, String newStatus) {
        Order order = getById(orderId);
        if (order == null) {
            throw new ApiException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        // Validate status transition
        OrderStatusValidator.ValidationResult validation = OrderStatusValidator.validateTransition(
            order.getStatus(), newStatus
        );
        
        if (!validation.isValid()) {
            throw new ApiException(ErrorCode.ORDER_STATUS_INVALID, validation.getErrorMessage());
        }
        
        order.setStatus(newStatus);
        order.setUpdatedAt(System.currentTimeMillis());
        Order saved = orderRepository.save(order);
        notificationService.pushOrderStatusNotification(saved, newStatus);
        updateUserVipTier(saved);
        
        // Grant purchase reward voucher when order is delivered
        if ("delivered".equalsIgnoreCase(newStatus) && saved.getUserId() != null) {
            try {
                voucherAutomationService.grantPurchaseRewardVoucher(saved.getUserId(), saved.getId());
            } catch (Exception e) {
                // Log but don't fail status update
                System.err.println("Failed to grant purchase reward voucher: " + e.getMessage());
            }
        }
        
        return saved;
    }
    
    /**
     * Update order status with cancellation reason
     */
    public Order cancelWithReason(String orderId, String reason, String cancelledBy) {
        Order order = getById(orderId);
        if (order == null) {
            throw new ApiException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        // Validate that order can be cancelled
        if (!OrderStatusValidator.canCancel(order.getStatus())) {
            throw new ApiException(
                ErrorCode.ORDER_CANNOT_CANCEL,
                String.format("Cannot cancel order with status '%s'. Order may have already been delivered or cancelled.", 
                    order.getStatus())
            );
        }
        
        // Validate transition
        OrderStatusValidator.ValidationResult validation = OrderStatusValidator.validateTransition(
            order.getStatus(), OrderStatusValidator.CANCELLED
        );
        
        if (!validation.isValid()) {
            throw new ApiException(ErrorCode.ORDER_STATUS_INVALID, validation.getErrorMessage());
        }
        
        order.setStatus(OrderStatusValidator.CANCELLED);
        order.setCancellationReason(reason);
        order.setCancelledBy(cancelledBy);
        order.setCancelledAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());
        
        Order saved = orderRepository.save(order);
        
        // Refund voucher if used
        if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) {
            try {
                voucherService.refundVoucher(orderId);
            } catch (Exception e) {
                // Log error but don't fail cancellation
                System.err.println("Failed to refund voucher: " + e.getMessage());
            }
        }
        
        return saved;
    }

    public Order confirm(String orderId) {
        // confirmed = approved by admin/seller
        return updateStatus(orderId, "confirmed");
    }

    public Order pack(String orderId) {
        // packed stage (intermediate). We'll just set a known value
        return updateStatus(orderId, "packed");
    }

    public Order handover(String orderId) {
        // seller handed over to carrier
        return updateStatus(orderId, "shipping");
    }

    public Order deliver(String orderId) {
        return updateStatus(orderId, "delivered");
    }

    public Order cancel(String orderId) {
        return cancelWithReason(orderId, "Cancelled by user", null);
    }
    
    /**
     * Return order (from delivered status)
     */
    public Order returnOrder(String orderId, String returnReason) {
        Order order = getById(orderId);
        if (order == null) {
            throw new ApiException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        // Only delivered orders can be returned
        if (!OrderStatusValidator.DELIVERED.equalsIgnoreCase(order.getStatus())) {
            throw new ApiException(ErrorCode.ORDER_CANNOT_RETURN);
        }
        
        OrderStatusValidator.ValidationResult validation = OrderStatusValidator.validateTransition(
            order.getStatus(), OrderStatusValidator.RETURNED
        );
        
        if (!validation.isValid()) {
            throw new ApiException(ErrorCode.ORDER_STATUS_INVALID, validation.getErrorMessage());
        }
        
        order.setStatus(OrderStatusValidator.RETURNED);
        order.setReturnReason(returnReason);
        order.setUpdatedAt(System.currentTimeMillis());
        
        return orderRepository.save(order);
    }
    
    /**
     * Refund order
     */
    public Order refundOrder(String orderId, String refundReason) {
        Order order = getById(orderId);
        if (order == null) {
            throw new ApiException(ErrorCode.ORDER_NOT_FOUND);
        }
        
        // Only cancelled, returned, or delivered orders can be refunded
        String currentStatus = order.getStatus().toLowerCase();
        if (!OrderStatusValidator.CANCELLED.equals(currentStatus) && 
            !OrderStatusValidator.RETURNED.equals(currentStatus) &&
            !OrderStatusValidator.DELIVERED.equals(currentStatus)) {
            throw new ApiException(ErrorCode.ORDER_CANNOT_REFUND);
        }
        
        OrderStatusValidator.ValidationResult validation = OrderStatusValidator.validateTransition(
            order.getStatus(), OrderStatusValidator.REFUNDED
        );
        
        if (!validation.isValid()) {
            throw new ApiException(ErrorCode.ORDER_STATUS_INVALID, validation.getErrorMessage());
        }
        
        order.setStatus(OrderStatusValidator.REFUNDED);
        order.setRefundReason(refundReason);
        order.setPaymentStatus("refunded");
        order.setUpdatedAt(System.currentTimeMillis());
        
        return orderRepository.save(order);
    }

    public List<Order> checkout(String userId) {
        return checkout(userId, "cod", "standard", null);
    }
    
    public List<Order> checkout(String userId, String paymentMethod, String shippingMethod, java.util.Map<String, String> shippingAddress) {
        return checkout(userId, paymentMethod, shippingMethod, shippingAddress, null);
    }
    
    public List<Order> checkout(String userId, String paymentMethod, String shippingMethod, 
                               java.util.Map<String, String> shippingAddress, String voucherCode) {
        Cart cart = cartService.getOrCreateCartForUser(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ApiException(ErrorCode.ORDER_EMPTY_CART);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("processing");
        
        // Set payment method
        if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
            order.setPaymentMethod(paymentMethod);
            // Set payment status based on method
            if ("cod".equals(paymentMethod)) {
                order.setPaymentStatus("unpaid"); // Will be paid on delivery
            } else {
                order.setPaymentStatus("unpaid"); // Will be paid online
            }
        } else {
            order.setPaymentMethod("cod");
            order.setPaymentStatus("unpaid");
        }
        
        // Calculate subtotal
        double subtotal = 0;
        java.util.List<Order.OrderItem> orderItems = new java.util.ArrayList<>();
        for (Cart.CartItem item : cart.getItems()) {
            Order.OrderItem oi = new Order.OrderItem();
            oi.setProductId(item.getProductId());
            oi.setQuantity(item.getQuantity());
            oi.setUnitPrice(item.getPrice());

            // Optionally hydrate snapshots
            try {
                Product p = productService.getById(item.getProductId());
                if (p != null) {
                    oi.setNameSnapshot(p.getName());
                    if (p.getImages() != null && !p.getImages().isEmpty()) {
                        oi.setImageSnapshot(p.getImages().get(0));
                    }
                }
            } catch (Exception ignored) {}

            subtotal += item.getPrice() * item.getQuantity();
            orderItems.add(oi);
        }
        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        
        // Apply voucher if provided (use cart's voucher code if not provided)
        String voucherToUse = voucherCode != null ? voucherCode : cart.getVoucherCode();
        double discountAmount = 0;
        String voucherId = null;
        
        if (voucherToUse != null && !voucherToUse.isEmpty()) {
            // Get product and category IDs for validation
            List<String> productIds = cart.getItems().stream()
                .map(Cart.CartItem::getProductId)
                .collect(Collectors.toList());
            List<String> categoryIds = new ArrayList<>(); // Simplified
            
            // Validate voucher
            if (voucherService.validateVoucherForOrder(
                voucherToUse, userId, subtotal, null, productIds, categoryIds)) {
                
                // Get voucher for ID
                java.util.Optional<com.example.harvesthubbackend.Models.Voucher> voucherOpt = 
                    voucherService.getVoucherByCode(voucherToUse);
                if (voucherOpt.isPresent()) {
                    voucherId = voucherOpt.get().getId();
                    
                    // Calculate discount (including free shipping)
                    double shippingFee = 0;
                    if ("express".equals(shippingMethod)) {
                        shippingFee = 60000;
                    } else {
                        shippingFee = 30000;
                    }
                    
                    discountAmount = voucherService.calculateDiscount(voucherToUse, subtotal, shippingFee);
                    
                    // If free shipping, set shipping fee to 0
                    if (voucherOpt.get().getType().equals("free_shipping")) {
                        shippingFee = 0;
                    }
                    order.setShippingFee(shippingFee);
                }
            }
        } else {
            // Set shipping fee based on method
            double shippingFee = 0;
            if ("express".equals(shippingMethod)) {
                shippingFee = 60000; // 60,000 VND for express
            } else {
                shippingFee = 30000; // 30,000 VND for standard
            }
            order.setShippingFee(shippingFee);
        }
        
        order.setVoucherCode(voucherToUse);
        order.setVoucherId(voucherId);
        order.setDiscountAmount(discountAmount);
        
        // Set shipping address
        if (shippingAddress != null) {
            StringBuilder addressBuilder = new StringBuilder();
            if (shippingAddress.get("fullName") != null) {
                addressBuilder.append(shippingAddress.get("fullName"));
            }
            if (shippingAddress.get("address") != null) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(shippingAddress.get("address"));
            }
            if (shippingAddress.get("ward") != null) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append("Phường/Xã: ").append(shippingAddress.get("ward"));
            }
            if (shippingAddress.get("district") != null) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append("Quận/Huyện: ").append(shippingAddress.get("district"));
            }
            if (shippingAddress.get("city") != null) {
                if (addressBuilder.length() > 0) addressBuilder.append(", ");
                addressBuilder.append(shippingAddress.get("city"));
            }
            if (addressBuilder.length() > 0) {
                order.setShippingAddress(addressBuilder.toString());
            }
        }

        // Calculate total price
        double total = subtotal - discountAmount + order.getShippingFee();
        order.setTotalPrice(total);
        order.setUpdatedAt(System.currentTimeMillis());

        Order saved = orderRepository.save(order);
        
        // Apply voucher to order (create usage record)
        if (voucherToUse != null && !voucherToUse.isEmpty() && discountAmount > 0) {
            try {
                voucherService.applyVoucherToOrder(userId, saved.getId(), voucherToUse, 
                                                   subtotal, discountAmount);
            } catch (Exception e) {
                // Log error but don't fail order creation
                System.err.println("Failed to apply voucher to order: " + e.getMessage());
            }
        }
        
        // Update flash sale sold count for products in this order
        try {
            System.out.println("Attempting to update flash sale sold count for order: " + saved.getId());
            updateFlashSaleSoldCount(saved);
        } catch (Exception e) {
            // Log error but don't fail order creation
            System.err.println("Failed to update flash sale sold count: " + e.getMessage());
            e.printStackTrace();
        }
        
        cartService.clearCart(userId);
        return java.util.List.of(saved);
    }

    private void updateUserVipTier(Order order) {
        if (order == null || order.getUserId() == null) return;
        List<Order> userOrders = orderRepository.findByUserId(order.getUserId());
        double totalSpent = 0;
        for (Order o : userOrders) {
            if (o == null) continue;
            String status = o.getStatus() != null ? o.getStatus().toLowerCase() : "";
            if ("delivered".equals(status) || "completed".equals(status)) {
                Double price = o.getTotalPrice();
                totalSpent += price != null ? price : 0;
            }
        }
        String tier = determineVipTier(totalSpent);
        User existing = userService.getById(order.getUserId());
        if (existing == null) return;
        String current = existing.getMembershipType() != null ? existing.getMembershipType() : "STANDARD";
        if (!current.equalsIgnoreCase(tier)) {
            userService.updateMembershipType(order.getUserId(), tier);
        }
    }

    private String determineVipTier(double totalSpent) {
        if (totalSpent >= 5_000_000) return "VIP3";
        if (totalSpent >= 3_000_000) return "VIP2";
        if (totalSpent >= 1_000_000) return "VIP1";
        return "STANDARD";
    }
    
    // Update flash sale sold count when order is created
    private void updateFlashSaleSoldCount(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }
        
        // Get all active flash sales
        List<com.example.harvesthubbackend.Models.FlashSale> activeFlashSales = flashSaleService.getActiveFlashSales();
        if (activeFlashSales == null || activeFlashSales.isEmpty()) {
            return;
        }
        
        // For each item in the order
        for (Order.OrderItem item : order.getItems()) {
            String productId = item.getProductId();
            int quantity = item.getQuantity();
            
            // Check each active flash sale
            for (com.example.harvesthubbackend.Models.FlashSale flashSale : activeFlashSales) {
                if (flashSale.getProducts() == null) continue;
                
                // Find the product in flash sale
                for (com.example.harvesthubbackend.Models.FlashSale.FlashSaleProduct flashSaleProduct : flashSale.getProducts()) {
                    if (flashSaleProduct.getProductId().equals(productId)) {
                        // Check if the order item price matches flash sale price
                        // This ensures we only count items bought at flash sale price
                        double unitPrice = item.getUnitPrice();
                        double flashSalePrice = flashSaleProduct.getFlashSalePrice();
                        // Use a more lenient comparison (within 100 VND) to account for rounding
                        if (Math.abs(unitPrice - flashSalePrice) < 100.0) {
                            // Update sold count
                            int currentSoldCount = flashSaleProduct.getSoldCount();
                            int newSoldCount = currentSoldCount + quantity;
                            try {
                                flashSaleService.updateProductSoldCount(
                                    flashSale.getId(), 
                                    productId, 
                                    newSoldCount
                                );
                                System.out.println("Updated flash sale sold count for product " + productId + 
                                                 " in flash sale " + flashSale.getId() + 
                                                 ": " + currentSoldCount + " -> " + newSoldCount);
                            } catch (Exception e) {
                                System.err.println("Failed to update flash sale sold count: " + e.getMessage());
                            }
                        } else {
                            System.out.println("Price mismatch for product " + productId + 
                                             ": order price=" + unitPrice + 
                                             ", flash sale price=" + flashSalePrice);
                        }
                        break; // Found the product, no need to check other flash sales
                    }
                }
            }
        }
    }
}