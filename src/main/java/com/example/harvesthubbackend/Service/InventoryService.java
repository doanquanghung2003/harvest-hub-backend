package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Inventory;
import com.example.harvesthubbackend.Models.InventoryTransaction;
import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Repository.InventoryRepository;
import com.example.harvesthubbackend.Repository.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    @Autowired
    private InventoryTransactionRepository transactionRepository;
    
    @Autowired
    private ProductService productService;
    
    // Tạo inventory mới cho sản phẩm
    public Inventory createInventory(String productId, String sellerId, int initialStock) {
        // Kiểm tra xem đã có inventory chưa
        Optional<Inventory> existing = inventoryRepository.findByProductId(productId);
        if (existing.isPresent()) {
            throw new RuntimeException("Inventory already exists for this product");
        }
        
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setSellerId(sellerId);
        inventory.setCurrentStock(initialStock);
        inventory.setReservedStock(0);
        inventory.calculateAvailableStock();
        inventory.setTotalIn(initialStock);
        inventory.setLastRestockedAt(LocalDateTime.now());
        
        Inventory saved = inventoryRepository.save(inventory);
        
        // Tạo transaction record
        createTransaction(saved.getId(), productId, sellerId, "in", "initial_stock", initialStock, 
            saved.getCurrentStock() - initialStock, saved.getCurrentStock(), "Initial stock", "system");
        
        // Cập nhật stock trong Product
        updateProductStock(productId, saved.getCurrentStock());
        
        return saved;
    }
    
    // Nhập hàng (stock in)
    @Transactional
    public Inventory stockIn(String productId, int quantity, String reason, String notes, String createdBy) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        int quantityBefore = inventory.getCurrentStock();
        inventory.setCurrentStock(quantityBefore + quantity);
        inventory.setTotalIn(inventory.getTotalIn() + quantity);
        inventory.setLastRestockedAt(LocalDateTime.now());
        inventory.calculateAvailableStock();
        
        Inventory saved = inventoryRepository.save(inventory);
        
        // Tạo transaction record
        createTransaction(saved.getId(), productId, inventory.getSellerId(), "in", reason, quantity,
            quantityBefore, saved.getCurrentStock(), notes, createdBy);
        
        // Cập nhật stock trong Product
        updateProductStock(productId, saved.getCurrentStock());
        
        return saved;
    }
    
    // Xuất hàng (stock out) - khi bán
    @Transactional
    public Inventory stockOut(String productId, int quantity, String orderId, String reason, String createdBy) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        if (inventory.getAvailableStock() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + inventory.getAvailableStock() + ", Requested: " + quantity);
        }
        
        int quantityBefore = inventory.getCurrentStock();
        inventory.setCurrentStock(quantityBefore - quantity);
        inventory.setTotalOut(inventory.getTotalOut() + quantity);
        inventory.setTotalSold(inventory.getTotalSold() + quantity);
        inventory.setLastSoldAt(LocalDateTime.now());
        inventory.calculateAvailableStock();
        
        Inventory saved = inventoryRepository.save(inventory);
        
        // Tạo transaction record
        createTransaction(saved.getId(), productId, inventory.getSellerId(), "out", reason, -quantity,
            quantityBefore, saved.getCurrentStock(), "Order: " + orderId, createdBy);
        
        // Cập nhật stock trong Product
        updateProductStock(productId, saved.getCurrentStock());
        
        return saved;
    }
    
    // Reserve stock (khi thêm vào giỏ hàng hoặc đặt hàng)
    @Transactional
    public Inventory reserveStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        if (inventory.getAvailableStock() < quantity) {
            throw new RuntimeException("Insufficient available stock. Available: " + inventory.getAvailableStock() + ", Requested: " + quantity);
        }
        
        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        inventory.calculateAvailableStock();
        
        return inventoryRepository.save(inventory);
    }
    
    // Release reserved stock (khi xóa khỏi giỏ hàng hoặc hủy đơn)
    @Transactional
    public Inventory releaseReservedStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        inventory.setReservedStock(Math.max(0, inventory.getReservedStock() - quantity));
        inventory.calculateAvailableStock();
        
        return inventoryRepository.save(inventory);
    }
    
    // Điều chỉnh stock (adjustment)
    @Transactional
    public Inventory adjustStock(String productId, int newQuantity, String reason, String notes, String createdBy) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        int quantityBefore = inventory.getCurrentStock();
        int difference = newQuantity - quantityBefore;
        
        inventory.setCurrentStock(newQuantity);
        inventory.calculateAvailableStock();
        
        Inventory saved = inventoryRepository.save(inventory);
        
        // Tạo transaction record
        String type = difference > 0 ? "in" : "out";
        createTransaction(saved.getId(), productId, inventory.getSellerId(), "adjustment", reason, difference,
            quantityBefore, saved.getCurrentStock(), notes, createdBy);
        
        // Cập nhật stock trong Product
        updateProductStock(productId, saved.getCurrentStock());
        
        return saved;
    }
    
    // Lấy inventory theo product ID
    public Optional<Inventory> getByProductId(String productId) {
        return inventoryRepository.findByProductId(productId);
    }
    
    // Lấy inventory theo seller ID
    public List<Inventory> getBySellerId(String sellerId) {
        return inventoryRepository.findBySellerId(sellerId);
    }
    
    // Lấy inventory theo trạng thái
    public List<Inventory> getByStatus(String status) {
        return inventoryRepository.findByStatus(status);
    }
    
    // Lấy danh sách sản phẩm sắp hết hàng
    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findByLowStockAlertTrue();
    }
    
    // Lấy tất cả inventory
    public List<Inventory> getAll() {
        return inventoryRepository.findAll();
    }
    
    // Lấy inventory theo ID
    public Optional<Inventory> getById(String id) {
        return inventoryRepository.findById(id);
    }
    
    // Cập nhật inventory
    public Inventory update(String id, Inventory inventory) {
        inventory.setId(id);
        inventory.setUpdatedAt(LocalDateTime.now());
        inventory.calculateAvailableStock();
        return inventoryRepository.save(inventory);
    }
    
    // Xóa inventory
    public void delete(String id) {
        inventoryRepository.deleteById(id);
    }
    
    // Lấy lịch sử giao dịch
    public List<InventoryTransaction> getTransactionHistory(String inventoryId) {
        return transactionRepository.findByInventoryId(inventoryId);
    }
    
    // Lấy lịch sử giao dịch theo product
    public List<InventoryTransaction> getTransactionHistoryByProduct(String productId) {
        return transactionRepository.findByProductId(productId);
    }
    
    // Tạo transaction record
    private InventoryTransaction createTransaction(String inventoryId, String productId, String sellerId,
            String type, String reason, int quantity, int quantityBefore, int quantityAfter,
            String notes, String createdBy) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setInventoryId(inventoryId);
        transaction.setProductId(productId);
        transaction.setSellerId(sellerId);
        transaction.setType(type);
        transaction.setReason(reason);
        transaction.setQuantity(quantity);
        transaction.setQuantityBefore(quantityBefore);
        transaction.setQuantityAfter(quantityAfter);
        transaction.setNotes(notes);
        transaction.setCreatedBy(createdBy);
        
        return transactionRepository.save(transaction);
    }
    
    // Cập nhật stock trong Product model
    private void updateProductStock(String productId, int stock) {
        try {
            Product product = productService.getById(productId);
            if (product != null) {
                product.setStock(stock);
                product.setInStock(stock > 0);
                if (stock == 0) {
                    product.setStatus("out_of_stock");
                } else if (product.getStatus().equals("out_of_stock")) {
                    product.setStatus("active");
                }
                productService.update(productId, product);
            }
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Error updating product stock: " + e.getMessage());
        }
    }
}

