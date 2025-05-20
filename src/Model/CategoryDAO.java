package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // Get all categories
    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        // Query to get all categories
        // Simple SELECT to retrieve all category records
        String query = "SELECT * FROM categories";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching categories: " + e.getMessage());
        }
        return categories;
    }

    // Get category by ID
    public static Category getCategoryById(int categoryId) {
        // Query to get a specific category by its ID
        String query = "SELECT * FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Category(
                    rs.getInt("id"),
                    rs.getString("name")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching category: " + e.getMessage());
        }
        return null;
    }

    // Get category by name
    public static Category getCategoryByName(String categoryName) {
        // Query to get a category by its name
        // Useful for checking if a category already exists
        String query = "SELECT * FROM categories WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Category(
                    rs.getInt("id"),
                    rs.getString("name")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching category by name: " + e.getMessage());
        }
        return null;
    }

    // Insert a new category
    public static Category insertCategory(String name) {
        // Query to insert a new category
        // Only requires the category name
        String query = "INSERT INTO categories (name) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new Category(id, name);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error inserting category: " + e.getMessage());
        }
        return null;
    }

    // Update category
    public static boolean updateCategory(Category category) {
        // Query to update an existing category
        // Updates only the name field for a specific category ID
        String query = "UPDATE categories SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating category: " + e.getMessage());
        }
        return false;
    }

    // Delete category
    public static boolean deleteCategory(int categoryId) {
        // Query to delete a category by its ID
        String query = "DELETE FROM categories WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting category: " + e.getMessage());
        }
        return false;
    }
} 