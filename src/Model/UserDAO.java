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
             // Vérifier d'abord si l'utilisateur existe déjà
             PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Username already exists: " + username);
                return false;
            }
            
            // Si l'utilisateur n'existe pas, l'insérer
            PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO users (username, password_hash) VALUES (?, ?)");
            insertStmt.setString(1, username);
            insertStmt.setString(2, password); // Hash password if needed
            int rowsInserted = insertStmt.executeUpdate();
            insertStmt.close();
            
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.out.println("Error creating user: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for debugging
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
            e.printStackTrace(); // Add stack trace for debugging
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
            e.printStackTrace(); // Add stack trace for debugging
        }
        return false;
    }

    public static User getUserByUsername(String username) {
        String query = "SELECT user_id, username, password_hash FROM Users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password_hash")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error getting user by username: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for debugging
        }
        return null;
    }
}
