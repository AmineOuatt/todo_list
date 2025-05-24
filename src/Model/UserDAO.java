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
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete the collaboration
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM user_collaborations WHERE (user_id = ? AND collaborator_id = ?) OR (user_id = ? AND collaborator_id = ?)")) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, collaboratorId);
                    stmt.setInt(3, collaboratorId);
                    stmt.setInt(4, userId);
                    stmt.executeUpdate();
                }

                // Update any existing collaboration requests to declined
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE collaboration_requests SET status = 'declined' WHERE " +
                        "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                        "AND status = 'accepted'")) {
                    stmt.setInt(1, userId);
                    stmt.setInt(2, collaboratorId);
                    stmt.setInt(3, collaboratorId);
                    stmt.setInt(4, userId);
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
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

    // New methods for collaboration requests
    
    public static boolean sendCollaborationRequest(int senderId, int receiverId) {
        // Check if there's already a pending request
        if (hasPendingRequest(senderId, receiverId)) {
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO collaboration_requests (sender_id, receiver_id) VALUES (?, ?)")) {
            
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error sending collaboration request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean hasPendingRequest(int senderId, int receiverId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM collaboration_requests " +
                     "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                     "AND status = 'pending'")) {
            
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking pending request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public static List<CollaborationRequest> getPendingRequests(int userId) {
        List<CollaborationRequest> requests = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT cr.*, u.username as sender_name " +
                     "FROM collaboration_requests cr " +
                     "JOIN users u ON cr.sender_id = u.user_id " +
                     "WHERE cr.receiver_id = ? AND cr.status = 'pending'")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(new CollaborationRequest(
                    rs.getInt("request_id"),
                    rs.getInt("sender_id"),
                    rs.getInt("receiver_id"),
                    rs.getString("sender_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error getting pending requests: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }
    
    public static List<CollaborationRequest> getSentRequests(int userId) {
        List<CollaborationRequest> requests = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT cr.*, u.username as receiver_name " +
                     "FROM collaboration_requests cr " +
                     "JOIN users u ON cr.receiver_id = u.user_id " +
                     "WHERE cr.sender_id = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(new CollaborationRequest(
                    rs.getInt("request_id"),
                    rs.getInt("sender_id"),
                    rs.getInt("receiver_id"),
                    rs.getString("receiver_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error getting sent requests: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }
    
    public static boolean respondToRequest(int requestId, String status) {
        if (!status.equals("accepted") && !status.equals("declined")) {
            return false;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update request status
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE collaboration_requests SET status = ? WHERE request_id = ?")) {
                    stmt.setString(1, status);
                    stmt.setInt(2, requestId);
                    stmt.executeUpdate();
                }
                
                // If accepted, create collaboration
                if (status.equals("accepted")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT sender_id, receiver_id FROM collaboration_requests WHERE request_id = ?")) {
                        stmt.setInt(1, requestId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            int senderId = rs.getInt("sender_id");
                            int receiverId = rs.getInt("receiver_id");
                            
                            // Add bidirectional collaboration
                            addCollaboration(senderId, receiverId);
                            addCollaboration(receiverId, senderId);
                        }
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error responding to request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean resendRequest(int requestId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE collaboration_requests SET status = 'pending', updated_at = CURRENT_TIMESTAMP " +
                     "WHERE request_id = ? AND status = 'declined'")) {
            
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error resending request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static List<CollaborationRequest> getReceivedRequests(int userId) {
        List<CollaborationRequest> requests = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT cr.*, u.username as sender_name " +
                     "FROM collaboration_requests cr " +
                     "JOIN users u ON cr.sender_id = u.user_id " +
                     "WHERE cr.receiver_id = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                requests.add(new CollaborationRequest(
                    rs.getInt("request_id"),
                    rs.getInt("sender_id"),
                    rs.getInt("receiver_id"),
                    rs.getString("sender_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error getting received requests: " + e.getMessage());
            e.printStackTrace();
        }
        return requests;
    }

    public static boolean removeRequest(int requestId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM collaboration_requests WHERE request_id = ? AND status = 'declined'")) {
            
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error removing request: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
