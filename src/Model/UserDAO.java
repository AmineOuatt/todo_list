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

    // Method to update user
    public static boolean updateUser(int userId, String newUsername, String newPassword) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (newPassword != null && !newPassword.isEmpty()) {
                // Update both username and password
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Users SET username = ?, password_hash = ? WHERE user_id = ?");
                stmt.setString(1, newUsername);
                stmt.setString(2, newPassword);
                stmt.setInt(3, userId);
                return stmt.executeUpdate() > 0;
            } else {
                // Update only username
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE Users SET username = ? WHERE user_id = ?");
                stmt.setString(1, newUsername);
                stmt.setInt(2, userId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error updating user: " + e.getMessage());
        }
        return false;
    }

    // Method to delete user
    public static boolean deleteUser(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM Users WHERE user_id = ?")) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }

    public static User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
