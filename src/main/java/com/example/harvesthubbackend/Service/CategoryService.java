package com.example.harvesthubbackend.Service;

import com.example.harvesthubbackend.Models.Category;
import com.example.harvesthubbackend.Models.Product;
import com.example.harvesthubbackend.Repository.CategoryRepository;
import com.example.harvesthubbackend.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get all categories
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        // Calculate productCount for each category
        calculateProductCounts(categories);
        
        return categories;
    }
    
    // Helper method to calculate product counts for categories
    private void calculateProductCounts(List<Category> categories) {
        if (productRepository != null && categories != null) {
            for (Category category : categories) {
                int count = 0;
                try {
                    String categoryName = category.getName();
                    String categoryId = category.getId();
                    
                    // Count products by category name
                    if (categoryName != null && !categoryName.trim().isEmpty()) {
                        List<Product> productsByName = productRepository.findByCategory(categoryName);
                        if (productsByName != null && !productsByName.isEmpty()) {
                            count = productsByName.size();
                        }
                    }
                    
                    // If no products found by name, try by ID
                    if (count == 0 && categoryId != null && !categoryId.trim().isEmpty()) {
                        List<Product> productsById = productRepository.findByCategory(categoryId);
                        if (productsById != null && !productsById.isEmpty()) {
                            count = productsById.size();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating productCount for category " + category.getId() + ": " + e.getMessage());
                    // Continue with count = 0 if there's an error
                }
                
                category.setProductCount(count);
            }
        }
    }

    // Get active categories only
    public List<Category> getActiveCategories() {
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        
        // Calculate productCount for each category
        calculateProductCounts(categories);
        
        return categories;
    }

    // Get category by ID
    public Optional<Category> getCategoryById(String id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            calculateProductCounts(List.of(category));
            return Optional.of(category);
        }
        return categoryOpt;
    }

    // Get category by name
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    // Get category by slug
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    // Create new category
    public Category createCategory(Category category) {
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // Generate slug from name if not provided
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(generateSlug(category.getName()));
        }

        return categoryRepository.save(category);
    }

    // Update category
    public Category updateCategory(String id, Category categoryDetails) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);

        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            if (categoryDetails.getName() != null) {
                category.setName(categoryDetails.getName());
            }
            if (categoryDetails.getDescription() != null) {
                category.setDescription(categoryDetails.getDescription());
            }
            if (categoryDetails.getIcon() != null) {
                category.setIcon(categoryDetails.getIcon());
            }
            if (categoryDetails.getImage() != null) {
                category.setImage(categoryDetails.getImage());
            }
            if (categoryDetails.getColor() != null) {
                category.setColor(categoryDetails.getColor());
            }
            if (categoryDetails.getSlug() != null) {
                category.setSlug(categoryDetails.getSlug());
            }

            category.setUpdatedAt(LocalDateTime.now());

            return categoryRepository.save(category);
        }

        return null;
    }

    // Delete category
    public boolean deleteCategory(String id) {
        System.out.println("=== CategoryService.deleteCategory START ===");
        System.out.println("Category ID: " + id);
        
        if (id == null || id.trim().isEmpty()) {
            System.err.println("ERROR: Category ID is null or empty");
            throw new IllegalArgumentException("ID danh mục là bắt buộc");
        }

        if (categoryRepository == null) {
            System.err.println("ERROR: CategoryRepository is null");
            throw new IllegalStateException("CategoryRepository chưa được khởi tạo");
        }

        // Check if category exists
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (!categoryOpt.isPresent()) {
            System.err.println("ERROR: Category with ID " + id + " not found");
            throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + id);
        }

        Category category = categoryOpt.get();
        System.out.println("Found category: ID=" + id + ", Name=" + category.getName());
        
        // Check if category has subcategories (by subcategoryIds field)
        if (category.getSubcategoryIds() != null && !category.getSubcategoryIds().isEmpty()) {
            int subCount = category.getSubcategoryIds().size();
            System.err.println("ERROR: Category has " + subCount + " subcategories in subcategoryIds");
            throw new IllegalStateException("Không thể xóa danh mục này vì có " + subCount + " danh mục con. Vui lòng xóa các danh mục con trước.");
        }

        // Check if any categories have this category as parent
        try {
            List<Category> childCategories = categoryRepository.findByParentId(id);
            if (childCategories != null && !childCategories.isEmpty()) {
                int childCount = childCategories.size();
                System.err.println("ERROR: Found " + childCount + " child category(ies) with parentId=" + id);
                throw new IllegalStateException("Không thể xóa danh mục này vì có " + childCount + " danh mục con đang sử dụng danh mục này làm danh mục cha. Vui lòng xóa các danh mục con trước.");
            }
            System.out.println("No child categories found with parentId=" + id);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("WARNING: Error checking child categories: " + e.getMessage());
            // Continue with deletion attempt
        }

        // Check if any products are using this category
        if (productRepository != null) {
            try {
                String categoryName = category.getName();
                String categoryId = category.getId();
                
                // Check by category name
                if (categoryName != null && !categoryName.trim().isEmpty()) {
                    List<Product> productsByName = productRepository.findByCategory(categoryName);
                    if (productsByName != null && !productsByName.isEmpty()) {
                        int productCount = productsByName.size();
                        System.err.println("ERROR: " + productCount + " product(s) are using category name: " + categoryName);
                        throw new IllegalStateException("Không thể xóa danh mục này vì có " + productCount + " sản phẩm đang sử dụng. Vui lòng xóa hoặc cập nhật các sản phẩm đó trước.");
                    }
                    System.out.println("No products found with category name: " + categoryName);
                }
                
                // Check by category ID
                if (categoryId != null && !categoryId.trim().isEmpty()) {
                    List<Product> productsById = productRepository.findByCategory(categoryId);
                    if (productsById != null && !productsById.isEmpty()) {
                        int productCount = productsById.size();
                        System.err.println("ERROR: " + productCount + " product(s) are using category ID: " + categoryId);
                        throw new IllegalStateException("Không thể xóa danh mục này vì có " + productCount + " sản phẩm đang sử dụng. Vui lòng xóa hoặc cập nhật các sản phẩm đó trước.");
                    }
                    System.out.println("No products found with category ID: " + categoryId);
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                System.err.println("WARNING: Error checking products: " + e.getMessage());
                e.printStackTrace();
                // Continue with deletion attempt
            }
        } else {
            System.err.println("WARNING: ProductRepository is null, skipping product check");
        }

        // Delete the category
        try {
            System.out.println("Attempting to delete category from database: " + id);
            categoryRepository.deleteById(id);
            System.out.println("Delete operation completed. Verifying...");
            
            // Verify deletion
            Optional<Category> verifyCategory = categoryRepository.findById(id);
            if (verifyCategory.isPresent()) {
                System.err.println("WARNING: Category still exists after deletion attempt!");
                throw new IllegalStateException("Không thể xóa danh mục. Vui lòng thử lại.");
            }
            
            System.out.println("SUCCESS: Category deleted successfully: " + id);
            System.out.println("=== CategoryService.deleteCategory END ===");
            return true;
        } catch (IllegalStateException e) {
            System.err.println("IllegalStateException during deletion: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("ERROR during category deletion: " + e.getMessage());
            e.printStackTrace();
            throw new IllegalStateException("Lỗi khi xóa danh mục từ cơ sở dữ liệu: " + e.getMessage());
        }
    }

    // Helper method to generate slug from name
    private String generateSlug(String name) {
        if (name == null)
            return "";
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}
