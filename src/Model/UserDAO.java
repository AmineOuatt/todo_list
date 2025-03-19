package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // Method to fetch all users with IDs
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, username FROM Users")) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                users.add(new User(userId, username)); // Store user ID too
            }
        } catch (SQLException e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    // Method to verify user credentials
    public static boolean authenticateUser(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT password_hash FROM Users WHERE username = ?")) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");
                return storedPassword.equals(password); // Add hashing if needed
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return false;
    }

    // Method to create a new user
    public static boolean createUser(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Users (username, password_hash) VALUES (?, ?)")) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Hash password if needed
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
        }
        return false;
    }
}
