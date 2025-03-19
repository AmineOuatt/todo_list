package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // Fetch tasks for a user
    public static List<Task> getTasksByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT * FROM Tasks WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(new Task(
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }

    // Insert a new task
    public static boolean insertTask(Task task) {
        String query = "INSERT INTO Tasks (user_id, title, description, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, task.getUserId());
            stmt.setString(2, task.getTitle());
            stmt.setString(3, task.getDescription());
            stmt.setString(4, task.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error inserting task: " + e.getMessage());
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
}
