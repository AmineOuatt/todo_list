package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Model.DatabaseConnection;
import Model.SubTask;

public class SubTaskController {
    
    // Get all subtasks for a specific task
    public static List<SubTask> getSubTasksByTaskId(int taskId) {
        List<SubTask> subTasks = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM subtasks WHERE task_id = ? ORDER BY subtask_id ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SubTask subTask = new SubTask(
                    rs.getInt("subtask_id"),
                    rs.getInt("task_id"),
                    rs.getString("description"),
                    rs.getBoolean("completed")
                );
                subTasks.add(subTask);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return subTasks;
    }
    
    // Create a new subtask
    public static boolean createSubTask(int taskId, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO subtasks (task_id, description, completed) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            stmt.setString(2, description);
            stmt.setBoolean(3, false); // Default to not completed
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Create a new subtask and return the created subtask with its ID
    public static SubTask createAndGetSubTask(int taskId, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO subtasks (task_id, description, completed) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, taskId);
            stmt.setString(2, description);
            stmt.setBoolean(3, false); // Default to not completed
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int subTaskId = rs.getInt(1);
                    return new SubTask(subTaskId, taskId, description, false);
                }
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Update a subtask's completion status
    public static boolean updateSubTaskStatus(int subTaskId, boolean completed) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE subtasks SET completed = ? WHERE subtask_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, completed);
            stmt.setInt(2, subTaskId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update a subtask's description
    public static boolean updateSubTaskDescription(int subTaskId, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE subtasks SET description = ? WHERE subtask_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, description);
            stmt.setInt(2, subTaskId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete a subtask
    public static boolean deleteSubTask(int subTaskId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM subtasks WHERE subtask_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subTaskId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete all subtasks for a task
    public static boolean deleteSubTasksByTaskId(int taskId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Query to delete all subtasks associated with a specific task
            // Used when deleting a parent task to clean up related subtasks
            String sql = "DELETE FROM subtasks WHERE task_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, taskId);
            
            int rowsAffected = stmt.executeUpdate();
            return true; // Return true even if no rows were affected
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 