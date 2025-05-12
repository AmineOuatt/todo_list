package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // Fetch tasks for a user
    public static List<Task> getTasksByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT t.*, c.id as category_id, c.name as category_name " +
                      "FROM Tasks t " +
                      "LEFT JOIN categories c ON t.category_id = c.id " +
                      "WHERE t.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = null;
                if (rs.getObject("category_id") != null) {
                    category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                    );
                }
                
                Task task = new Task(
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                );
                
                task.setCategory(category);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }

    // Get task by ID
    public static Task getTaskById(int taskId) {
        String query = "SELECT t.*, c.id as category_id, c.name as category_name " +
                      "FROM Tasks t " +
                      "LEFT JOIN categories c ON t.category_id = c.id " +
                      "WHERE t.task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Category category = null;
                if (rs.getObject("category_id") != null) {
                    category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                    );
                }
                
                Task task = new Task(
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                );
                
                task.setCategory(category);
                return task;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching task: " + e.getMessage());
        }
        return null;
    }

    // Insert a new task
    public static boolean insertTask(Task task) {
        String query = "INSERT INTO Tasks (user_id, title, description, due_date, status, category_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setDate(4, new java.sql.Date(task.getDueDate().getTime()));
            stmt.setString(5, task.getStatus());
            
            if (task.getCategory() != null) {
                stmt.setInt(6, task.getCategory().getId());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error inserting task: " + e.getMessage());
        }
        return false;
    }

    // Update task
    public static boolean updateTask(Task task) {
        String query = "UPDATE Tasks SET title = ?, description = ?, due_date = ?, status = ?, category_id = ? WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setDate(3, new java.sql.Date(task.getDueDate().getTime()));
            stmt.setString(4, task.getStatus());
            
            if (task.getCategory() != null) {
                stmt.setInt(5, task.getCategory().getId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            stmt.setInt(6, task.getTaskId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating task: " + e.getMessage());
        }
        return false;
    }

    // Delete task
    public static boolean deleteTask(int taskId) {
        String query = "DELETE FROM Tasks WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting task: " + e.getMessage());
        }
        return false;
    }

    // Update task status
    public static boolean updateTaskStatus(int taskId, String status) {
        String query = "UPDATE Tasks SET status = ? WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, taskId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating task status: " + e.getMessage());
        }
        return false;
    }
    
    // Get tasks by category
    public static List<Task> getTasksByCategory(int userId, int categoryId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT t.*, c.id as category_id, c.name as category_name " +
                      "FROM Tasks t " +
                      "LEFT JOIN categories c ON t.category_id = c.id " +
                      "WHERE t.user_id = ? AND t.category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                );
                
                Task task = new Task(
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                );
                
                task.setCategory(category);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching tasks by category: " + e.getMessage());
        }
        return tasks;
    }
    
    // Update task category
    public static boolean updateTaskCategory(int taskId, Integer categoryId) {
        String query = "UPDATE Tasks SET category_id = ? WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (categoryId != null) {
                stmt.setInt(1, categoryId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setInt(2, taskId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating task category: " + e.getMessage());
        }
        return false;
    }
}
