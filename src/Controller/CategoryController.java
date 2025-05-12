package Controller;

import java.util.List;

import Model.Category;
import Model.CategoryDAO;

/**
 * Controller class for managing categories
 */
public class CategoryController {
    
    /**
     * Get all categories
     * @return List of all categories
     */
    public static List<Category> getAllCategories() {
        return CategoryDAO.getAllCategories();
    }
    
    /**
     * Get a category by ID
     * @param categoryId The ID of the category to retrieve
     * @return The category with the specified ID, or null if not found
     */
    public static Category getCategoryById(int categoryId) {
        return CategoryDAO.getCategoryById(categoryId);
    }
    
    /**
     * Get a category by name
     * @param categoryName The name of the category to retrieve
     * @return The category with the specified name, or null if not found
     */
    public static Category getCategoryByName(String categoryName) {
        return CategoryDAO.getCategoryByName(categoryName);
    }
    
    /**
     * Create a new category
     * @param name The name of the category to create
     * @return The newly created category, or null if creation failed
     */
    public static Category createCategory(String name) {
        return CategoryDAO.insertCategory(name);
    }
    
    /**
     * Update an existing category
     * @param category The category to update
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateCategory(Category category) {
        return CategoryDAO.updateCategory(category);
    }
    
    /**
     * Delete a category
     * @param categoryId The ID of the category to delete
     * @return true if the deletion was successful, false otherwise
     */
    public static boolean deleteCategory(int categoryId) {
        return CategoryDAO.deleteCategory(categoryId);
    }
} 