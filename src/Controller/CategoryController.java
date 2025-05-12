package Controller;

import java.util.List;

import Model.Category;
import Model.CategoryDAO;

public class CategoryController {
    
    // Get all categories
    public static List<Category> getAllCategories() {
        return CategoryDAO.getAllCategories();
    }
    
    // Get category by ID
    public static Category getCategoryById(int categoryId) {
        return CategoryDAO.getCategoryById(categoryId);
    }
    
    // Get category by name
    public static Category getCategoryByName(String categoryName) {
        return CategoryDAO.getCategoryByName(categoryName);
    }
    
    // Create a new category
    public static Category createCategory(String categoryName) {
        // First check if a category with this name already exists
        Category existingCategory = getCategoryByName(categoryName);
        if (existingCategory != null) {
            return existingCategory;
        }
        
        // If not, create a new one
        return CategoryDAO.insertCategory(categoryName);
    }
    
    // Update a category
    public static boolean updateCategory(int categoryId, String newName) {
        Category category = getCategoryById(categoryId);
        if (category == null) return false;
        
        category.setName(newName);
        return CategoryDAO.updateCategory(category);
    }
    
    // Delete a category
    public static boolean deleteCategory(int categoryId) {
        return CategoryDAO.deleteCategory(categoryId);
    }
} 