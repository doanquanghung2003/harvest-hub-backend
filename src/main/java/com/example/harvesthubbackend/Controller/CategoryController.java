package com.example.harvesthubbackend.Controller;

import com.example.harvesthubbackend.Models.Category;
import com.example.harvesthubbackend.Service.CategoryService;
import com.example.harvesthubbackend.Utils.ImageUrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Get all categories
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            ImageUrlUtils.normalizeCategories(categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get active categories only
    @GetMapping("/active")
    public ResponseEntity<List<Category>> getActiveCategories() {
        try {
            List<Category> categories = categoryService.getActiveCategories();
            ImageUrlUtils.normalizeCategories(categories);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get category by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable String id) {
        try {
            Optional<Category> category = categoryService.getCategoryById(id);
            if (category.isPresent()) {
                ImageUrlUtils.normalizeCategory(category.get());
                return ResponseEntity.ok(category.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Category not found", "data", (Object) null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error retrieving category: " + e.getMessage(), "data",
                            (Object) null));
        }
    }

    // Create new category
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            Category createdCategory = categoryService.createCategory(category);
            ImageUrlUtils.normalizeCategory(createdCategory);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error creating category: " + e.getMessage(), "data",
                            (Object) null));
        }
    }

    // Update category
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable String id, @RequestBody Category categoryDetails) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
            if (updatedCategory != null) {
                ImageUrlUtils.normalizeCategory(updatedCategory);
                return ResponseEntity.ok(updatedCategory);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Category not found", "data", (Object) null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error updating category: " + e.getMessage(), "data",
                            (Object) null));
        }
    }

    // Delete category
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable String id) {
        System.out.println("=== DELETE CATEGORY REQUEST ===");
        System.out.println("Category ID: " + id);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (id == null || id.trim().isEmpty()) {
                System.err.println("ERROR: Category ID is null or empty");
                response.put("success", false);
                response.put("message", "Category ID is required");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            System.out.println("Calling categoryService.deleteCategory(" + id + ")");
            boolean deleted = categoryService.deleteCategory(id);
            
            if (deleted) {
                System.out.println("SUCCESS: Category deleted successfully");
                response.put("success", true);
                response.put("message", "Đã xóa danh mục thành công");
                response.put("data", null);
                return ResponseEntity.ok(response);
            } else {
                System.err.println("ERROR: Category not found or deletion returned false");
                response.put("success", false);
                response.put("message", "Không tìm thấy danh mục");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (IllegalArgumentException e) {
            // Invalid arguments (null ID, not found, etc.)
            System.err.println("IllegalArgumentException: " + e.getMessage());
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Dữ liệu không hợp lệ";
            response.put("success", false);
            response.put("message", errorMessage);
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalStateException e) {
            // Category has subcategories or products - return clear error message
            System.err.println("IllegalStateException: " + e.getMessage());
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Không thể xóa danh mục";
            response.put("success", false);
            response.put("message", errorMessage);
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // All other exceptions
            System.err.println("Exception: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống nội bộ";
            response.put("success", false);
            response.put("message", errorMessage);
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } finally {
            System.out.println("=== END DELETE CATEGORY REQUEST ===");
        }
    }
}
