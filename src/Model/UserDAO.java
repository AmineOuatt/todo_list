package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    // Method to fetch all users with IDs
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        // Query to get all users
        // Retrieves only user_id and username for security
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
            // Query to update user information
            // Can update both username and password, or just username
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
        // Query to get a user by their username
        // Retrieves complete user information including password hash
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

    // Méthode pour ajouter une collaboration
    public static boolean addCollaboration(int userId, int collaboratorId) {
        if (userId == collaboratorId) {
            return false; // On ne peut pas collaborer avec soi-même
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO user_collaborations (user_id, collaborator_id) VALUES (?, ?)")) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, collaboratorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error adding collaboration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // Méthode pour supprimer une collaboration
    public static boolean removeCollaboration(int userId, int collaboratorId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM user_collaborations WHERE user_id = ? AND collaborator_id = ?")) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, collaboratorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error removing collaboration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // Méthode pour récupérer les collaborateurs d'un utilisateur
    public static List<User> getCollaborators(int userId) {
        List<User> collaborators = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.user_id, u.username FROM users u " +
                     "JOIN user_collaborations uc ON u.user_id = uc.collaborator_id " +
                     "WHERE uc.user_id = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                collaborators.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching collaborators: " + e.getMessage());
            e.printStackTrace();
        }
        return collaborators;
    }
    
    // Méthode pour vérifier si une collaboration existe déjà
    public static boolean isCollaborator(int userId, int collaboratorId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM user_collaborations WHERE user_id = ? AND collaborator_id = ?")) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, collaboratorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking collaboration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    // Méthode pour rechercher des utilisateurs par nom d'utilisateur (pour la recherche de collaborateurs)
    public static List<User> searchUsersByUsername(String searchTerm) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT user_id, username FROM users WHERE username LIKE ? LIMIT 20")) {
            
            stmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Get a user by ID
     * @param userId The ID of the user to retrieve
     * @return The user with the specified ID, or null if not found
     */
    public static User getUserById(int userId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM Users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
