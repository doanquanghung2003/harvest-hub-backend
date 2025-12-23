package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Cart;
import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Exception.ApiException;
import com.example.harvesthubbackend.Exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Repository
@Repository
interface CartRepository extends MongoRepository<Cart, String> {
    List<Cart> findByUserId(String userId);
}

// Service
@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private FlashSaleService flashSaleService;
    
    @Autowired
    private InventoryService inventoryService;

    public List<Cart> getAll() {
        return cartRepository.findAll();
    }

    public Cart getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        return cartRepository.findById(id).orElse(null);
    }

    public List<Cart> getByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new RuntimeException("User ID không được để trống");
        }
        List<Cart> carts = cartRepository.findByUserId(userId);
        // Refresh flash sale prices for all items in carts
        if (carts != null) {
            for (Cart cart : carts) {
                refreshFlashSalePrices(cart);
            }
        }
        return carts;
    }
    
    // Refresh flash sale prices for all items in cart
    private void refreshFlashSalePrices(Cart cart) {
        if (cart.getItems() != null) {
            for (Cart.CartItem item : cart.getItems()) {
                Double flashSalePrice = flashSaleService.getFlashSalePriceForProduct(item.getProductId());
                if (flashSalePrice != null) {
                    item.setPrice(flashSalePrice);
                } else {
                    // If no flash sale, use product price
                    Product product = productService.getById(item.getProductId());
                    if (product != null && product.getPrice() != null) {
                        item.setPrice(product.getPrice());
                    }
                }
            }
            recomputeTotals(cart);
            cartRepository.save(cart);
        }
    }

    public Cart create(Cart cart) {
        return cartRepository.save(cart);
    }

    public Cart update(String id, Cart cart) {
        cart.setId(id);
        return cartRepository.save(cart);
    }

    public void delete(String id) {
        cartRepository.deleteById(id);
    }

    // Domain helpers
    public Cart getOrCreateCartForUser(String userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        if (carts != null && !carts.isEmpty()) {
            return carts.get(0);
        }
        Cart newCart = new Cart();
        newCart.setUserId(userId);
        newCart.setItems(new ArrayList<>());
        newCart.setTotalPrice(0);
        long now = System.currentTimeMillis();
        newCart.setCreatedAt(now);
        newCart.setUpdatedAt(now);
        return cartRepository.save(newCart);
    }

    private void recomputeTotals(Cart cart) {
        double subtotal = 0;
        if (cart.getItems() != null) {
            for (Cart.CartItem item : cart.getItems()) {
                subtotal += item.getPrice() * item.getQuantity();
            }
        }
        cart.setSubtotal(subtotal);
        
        // Calculate discount if voucher is applied
        double discountAmount = 0;
        if (cart.getVoucherCode() != null && !cart.getVoucherCode().isEmpty()) {
            try {
                // Get product and category IDs for validation
                List<String> productIds = cart.getItems().stream()
                    .map(Cart.CartItem::getProductId)
                    .collect(Collectors.toList());
                
                // Get category IDs from products (simplified - may need to fetch from ProductService)
                List<String> categoryIds = new ArrayList<>();
                
                // Validate and calculate discount
                if (voucherService.validateVoucherForOrder(
                    cart.getVoucherCode(), cart.getUserId(), subtotal, 
                    null, productIds, categoryIds)) {
                    discountAmount = voucherService.calculateDiscount(cart.getVoucherCode(), subtotal);
                } else {
                    // Voucher invalid, remove it
                    cart.setVoucherCode(null);
                    cart.setDiscountAmount(0);
                }
            } catch (Exception e) {
                // If validation fails, remove voucher
                System.err.println("Error validating voucher " + cart.getVoucherCode() + ": " + e.getMessage());
                cart.setVoucherCode(null);
                cart.setDiscountAmount(0);
            }
        } else {
            // No voucher, ensure discount is 0
            cart.setDiscountAmount(0);
        }
        
        cart.setDiscountAmount(discountAmount);
        cart.setTotalPrice(subtotal - discountAmount);
        cart.setUpdatedAt(System.currentTimeMillis());
    }
    
    // Apply voucher to cart
    public Cart applyVoucher(String userId, String voucherCode) {
        Cart cart = getOrCreateCartForUser(userId);
        
        // Calculate subtotal
        double subtotal = 0;
        if (cart.getItems() != null) {
            for (Cart.CartItem item : cart.getItems()) {
                subtotal += item.getPrice() * item.getQuantity();
            }
        }
        
        // Get product and category IDs for validation
        List<String> productIds = cart.getItems().stream()
            .map(Cart.CartItem::getProductId)
            .collect(Collectors.toList());
        List<String> categoryIds = new ArrayList<>(); // Simplified
        
        // Validate voucher
        if (!voucherService.validateVoucherForOrder(
            voucherCode, userId, subtotal, null, productIds, categoryIds)) {
            // Get voucher details for better error message
            java.util.Optional<com.example.harvesthubbackend.Models.Voucher> voucherOpt = 
                voucherService.getVoucherByCode(voucherCode);
            if (voucherOpt.isPresent()) {
                com.example.harvesthubbackend.Models.Voucher voucher = voucherOpt.get();
                if (subtotal < voucher.getMinOrderAmount()) {
                    throw new RuntimeException("Voucher yêu cầu đơn hàng tối thiểu " + 
                        String.format("%.0f", voucher.getMinOrderAmount()) + "đ. Đơn hàng hiện tại: " + 
                        String.format("%.0f", subtotal) + "đ");
                }
            }
            throw new RuntimeException("Voucher không hợp lệ hoặc không thể áp dụng cho đơn hàng này");
        }
        
        // Calculate discount to verify it's > 0
        double discountAmount = voucherService.calculateDiscount(voucherCode, subtotal);
        if (discountAmount <= 0) {
            throw new RuntimeException("Voucher không thể áp dụng. Giảm giá tính được: 0đ");
        }
        
        // Apply voucher
        cart.setVoucherCode(voucherCode);
        recomputeTotals(cart);
        
        // Verify discount was applied
        if (cart.getDiscountAmount() <= 0) {
            cart.setVoucherCode(null);
            throw new RuntimeException("Không thể tính giảm giá từ voucher này");
        }
        
        return cartRepository.save(cart);
    }
    
    // Remove voucher from cart
    public Cart removeVoucher(String userId) {
        Cart cart = getOrCreateCartForUser(userId);
        cart.setVoucherCode(null);
        recomputeTotals(cart);
        return cartRepository.save(cart);
    }

    public Cart addItem(String userId, String productId, int quantity) {
        if (quantity <= 0) quantity = 1;
        Cart cart = getOrCreateCartForUser(userId);

        // Find price from product
        Product product = productService.getById(productId);
        if (product == null) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Sản phẩm không tồn tại");
        }
        
        // Check stock availability
        Integer productStock = product.getStock();
        if (productStock == null || productStock <= 0) {
            throw new ApiException(ErrorCode.PRODUCT_OUT_OF_STOCK, 
                "Sản phẩm '" + (product.getName() != null ? product.getName() : productId) + "' đã hết hàng");
        }
        
        // Calculate total quantity after adding
        int currentQuantityInCart = cart.getItems().stream()
            .filter(i -> productId.equals(i.getProductId()))
            .mapToInt(Cart.CartItem::getQuantity)
            .sum();
        int totalQuantity = currentQuantityInCart + quantity;
        
        // Check if total quantity exceeds stock
        if (totalQuantity > productStock) {
            throw new ApiException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK, 
                String.format("Sản phẩm '%s' không đủ tồn kho. Còn lại: %d, yêu cầu: %d", 
                    product.getName() != null ? product.getName() : productId,
                    productStock,
                    totalQuantity));
        }
        
        // Also check Inventory if available
        try {
            Optional<com.example.harvesthubbackend.Models.Inventory> inventoryOpt = 
                inventoryService.getByProductId(productId);
            if (inventoryOpt.isPresent()) {
                com.example.harvesthubbackend.Models.Inventory inventory = inventoryOpt.get();
                if (inventory.getAvailableStock() < totalQuantity) {
                    throw new ApiException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK, 
                        String.format("Sản phẩm '%s' không đủ tồn kho. Có thể bán: %d, yêu cầu: %d", 
                            product.getName() != null ? product.getName() : productId,
                            inventory.getAvailableStock(),
                            totalQuantity));
                }
            }
        } catch (ApiException e) {
            throw e; // Re-throw ApiException
        } catch (Exception e) {
            // If inventory check fails, continue with product stock check
            // This allows backward compatibility if inventory is not set up
        }
        
        double price = (product.getPrice() != null) ? product.getPrice() : 0.0;
        
        // Check if product is in an active flash sale
        Double flashSalePrice = flashSaleService.getFlashSalePriceForProduct(productId);
        if (flashSalePrice != null) {
            price = flashSalePrice; // Use flash sale price if available
        }

        // Update quantity if exists
        Optional<Cart.CartItem> existing = cart.getItems().stream()
                .filter(i -> productId.equals(i.getProductId()))
                .findFirst();
        if (existing.isPresent()) {
            Cart.CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            // Update price to flash sale price if available (even if already set)
            if (flashSalePrice != null) {
                item.setPrice(flashSalePrice);
            } else if (item.getPrice() <= 0) {
                item.setPrice(price);
            }
        } else {
            Cart.CartItem item = new Cart.CartItem(productId, quantity, price);
            cart.getItems().add(item);
        }

        recomputeTotals(cart);
        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(String userId, String productId, int quantity) {
        Cart cart = getOrCreateCartForUser(userId);
        if (quantity <= 0) {
            return removeItem(userId, productId);
        }
        
        // Check stock availability before updating quantity
        Product product = productService.getById(productId);
        if (product == null) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND, "Sản phẩm không tồn tại");
        }
        
        Integer productStock = product.getStock();
        if (productStock == null || productStock < quantity) {
            throw new ApiException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK, 
                String.format("Sản phẩm '%s' không đủ tồn kho. Còn lại: %d, yêu cầu: %d", 
                    product.getName() != null ? product.getName() : productId,
                    productStock != null ? productStock : 0,
                    quantity));
        }
        
        // Also check Inventory if available
        try {
            Optional<com.example.harvesthubbackend.Models.Inventory> inventoryOpt = 
                inventoryService.getByProductId(productId);
            if (inventoryOpt.isPresent()) {
                com.example.harvesthubbackend.Models.Inventory inventory = inventoryOpt.get();
                if (inventory.getAvailableStock() < quantity) {
                    throw new ApiException(ErrorCode.PRODUCT_INSUFFICIENT_STOCK, 
                        String.format("Sản phẩm '%s' không đủ tồn kho. Có thể bán: %d, yêu cầu: %d", 
                            product.getName() != null ? product.getName() : productId,
                            inventory.getAvailableStock(),
                            quantity));
                }
            }
        } catch (ApiException e) {
            throw e; // Re-throw ApiException
        } catch (Exception e) {
            // If inventory check fails, continue with product stock check
        }
        
        for (Cart.CartItem item : cart.getItems()) {
            if (productId.equals(item.getProductId())) {
                item.setQuantity(quantity);
                // Update price to flash sale price if available
                Double flashSalePrice = flashSaleService.getFlashSalePriceForProduct(productId);
                if (flashSalePrice != null) {
                    item.setPrice(flashSalePrice);
                } else {
                    // If no flash sale, use product price
                    if (product.getPrice() != null) {
                        item.setPrice(product.getPrice());
                    }
                }
                break;
            }
        }
        recomputeTotals(cart);
        return cartRepository.save(cart);
    }

    public Cart removeItem(String userId, String productId) {
        Cart cart = getOrCreateCartForUser(userId);
        cart.getItems().removeIf(i -> productId.equals(i.getProductId()));
        recomputeTotals(cart);
        return cartRepository.save(cart);
    }

    public Cart clearCart(String userId) {
        Cart cart = getOrCreateCartForUser(userId);
        cart.getItems().clear();
        recomputeTotals(cart);
        return cartRepository.save(cart);
    }
}


