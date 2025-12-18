package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Order;
import com.example.harvesthubbackend.Service.OrderService;
import com.example.harvesthubbackend.Utils.PageResponse;
import com.example.harvesthubbackend.Utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/orders", "/api/v1/orders"})
@Tag(name = "Orders", description = "API endpoints for order management")
public class OrderController {
    @Autowired
    private OrderService orderService;


    @Operation(summary = "Get all orders", description = "Retrieve all orders with pagination (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    @GetMapping
    public Object getAll(
        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") String page,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") String size,
        @Parameter(description = "Return as array (no pagination)", example = "false") @RequestParam(defaultValue = "false") boolean asArray) {
        List<Order> allOrders = orderService.getAll();
        
        // Nếu frontend yêu cầu array trực tiếp (cho admin dashboard)
        if (asArray) {
            return ResponseEntity.ok(allOrders);
        }
        
        // Mặc định trả về PageResponse (có pagination)
        int pageNum = PaginationUtils.parsePage(page);
        int pageSize = PaginationUtils.parseSize(size);
        return PaginationUtils.paginate(allOrders, pageNum, pageSize);
    }

    // Endpoint trả về array trực tiếp (không pagination) cho admin dashboard
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public Order getById(@Parameter(description = "Order ID") @PathVariable String id) {
        return orderService.getById(id);
    }

    @Operation(summary = "Get orders by user", description = "Retrieve all orders for a specific user with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    @GetMapping("/user/{userId}")
    public PageResponse<Order> getByUser(
        @Parameter(description = "User ID") @PathVariable String userId,
        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") String page,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") String size) {
        List<Order> userOrders = orderService.getByUserId(userId);
        int pageNum = PaginationUtils.parsePage(page);
        int pageSize = PaginationUtils.parseSize(size);
        return PaginationUtils.paginate(userOrders, pageNum, pageSize);
    }

    @Operation(summary = "Get orders by seller", description = "Retrieve all orders for a specific seller with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    })
    @GetMapping("/seller/{sellerId}")
    public PageResponse<Order> getBySeller(
        @Parameter(description = "Seller ID") @PathVariable String sellerId,
        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") String page,
        @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") String size) {
        List<Order> sellerOrders = orderService.getBySellerId(sellerId);
        int pageNum = PaginationUtils.parsePage(page);
        int pageSize = PaginationUtils.parseSize(size);
        return PaginationUtils.paginate(sellerOrders, pageNum, pageSize);
    }

    @PostMapping
    public Order create(@RequestBody Order order) {
        return orderService.create(order);
    }

    @PutMapping("/{id}")
    public Order update(@PathVariable String id, @RequestBody Order order) {
        return orderService.update(id, order);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        orderService.delete(id);
    }

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<?> checkout(
            @PathVariable String userId,
            @RequestBody(required = false) java.util.Map<String, Object> checkoutData) {
        try {
            // Extract checkout data
            String paymentMethod = "cod";
            String shippingMethod = "standard";
            String voucherCode = null;
            java.util.Map<String, String> shippingAddress = null;
            
            if (checkoutData != null) {
                Object pm = checkoutData.get("paymentMethod");
                if (pm instanceof String) {
                    paymentMethod = (String) pm;
                }
                Object sm = checkoutData.get("shippingMethod");
                if (sm instanceof String) {
                    shippingMethod = (String) sm;
                }
                Object vc = checkoutData.get("voucherCode");
                if (vc instanceof String) {
                    voucherCode = (String) vc;
                }
                Object sa = checkoutData.get("shippingAddress");
                if (sa instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> saMap = (java.util.Map<String, Object>) sa;
                    shippingAddress = new java.util.HashMap<>();
                    for (java.util.Map.Entry<String, Object> entry : saMap.entrySet()) {
                        if (entry.getValue() instanceof String) {
                            shippingAddress.put(entry.getKey(), (String) entry.getValue());
                        }
                    }
                }
            }
            
            List<Order> orders = orderService.checkout(userId, paymentMethod, shippingMethod, shippingAddress, voucherCode);
            return ResponseEntity.ok(orders);
        } catch (com.example.harvesthubbackend.Exception.ApiException e) {
            // Get HTTP status from GlobalExceptionHandler logic
            int httpStatus = 400; // Default to BAD_REQUEST
            int code = e.getErrorCode().getCode();
            if (code >= 1000 && code < 2000) {
                if (code == 1002) httpStatus = 404; // NOT_FOUND
                else if (code == 1003) httpStatus = 401; // UNAUTHORIZED
                else if (code == 1004) httpStatus = 403; // FORBIDDEN
                else if (code == 1005) httpStatus = 400; // BAD_REQUEST
                else httpStatus = 500; // INTERNAL_SERVER_ERROR
            } else {
                httpStatus = 400; // Default to BAD_REQUEST
            }
            
            return ResponseEntity.status(httpStatus)
                .body(java.util.Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Lỗi khi đặt hàng",
                    "errorCode", code
                ));
        } catch (Exception e) {
            System.err.println("Error in checkout: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of(
                    "success", false,
                    "message", "Lỗi khi đặt hàng: " + e.getMessage()
                ));
        }
    }

    // Seller actions
    @PutMapping("/{id}/confirm")
    public Order confirm(@PathVariable String id) {
        return orderService.confirm(id);
    }

    @PutMapping("/{id}/pack")
    public Order pack(@PathVariable String id) {
        return orderService.pack(id);
    }

    @PutMapping("/{id}/handover")
    public Order handover(@PathVariable String id) {
        return orderService.handover(id);
    }

    @PutMapping("/{id}/deliver")
    public Order deliver(@PathVariable String id) {
        return orderService.deliver(id);
    }

    @Operation(summary = "Cancel order", description = "Cancel an order with optional reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel order with current status")
    })
    @PutMapping("/{id}/cancel")
    public Order cancel(
        @Parameter(description = "Order ID") @PathVariable String id, 
        @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason, 
        @Parameter(description = "User ID who cancelled") @RequestParam(required = false) String cancelledBy) {
        if (reason != null && !reason.trim().isEmpty()) {
            return orderService.cancelWithReason(id, reason, cancelledBy);
        }
        return orderService.cancel(id);
    }
    
    @PutMapping("/{id}/return")
    public Order returnOrder(@PathVariable String id, @RequestParam String returnReason) {
        return orderService.returnOrder(id, returnReason);
    }
    
    @PutMapping("/{id}/refund")
    public Order refundOrder(@PathVariable String id, @RequestParam String refundReason) {
        return orderService.refundOrder(id, refundReason);
    }
}

