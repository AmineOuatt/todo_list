package Controller;

import java.util.ArrayList;
import java.util.List;

import Model.CollaborationRequest;
import Model.User;
import Model.UserDAO;

public class UserController {
    private static User currentUser = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static List<User> getAllUsers() {
        return UserDAO.getAllUsers();
    }

    /**
     * Get a user by ID
     * @param userId The ID of the user to retrieve
     * @return The user with the specified ID, or null if not found
     */
    public static User getUserById(int userId) {
        return UserDAO.getUserById(userId);
    }

    public static boolean authenticateUser(String username, String password) {
        User user = UserDAO.getUserByUsername(username);
        if (user != null && UserDAO.authenticateUser(username, password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public static boolean createUser(String username, String password) {
        if (UserDAO.getUserByUsername(username) != null) {
            return false; // Username already exists
        }
        return UserDAO.createUser(username, password);
    }

    public static boolean updateUser(int userId, String newUsername, String newPassword) {
        if (!newUsername.equals(currentUser.getUsername()) && 
            UserDAO.getUserByUsername(newUsername) != null) {
            return false; // New username already exists
        }
        return UserDAO.updateUser(userId, newUsername, newPassword);
    }

    public static boolean deleteUser(int userId) {
        return UserDAO.deleteUser(userId);
    }

    public static User getUserByUsername(String username) {
        return UserDAO.getUserByUsername(username);
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean addCollaboration(int userId, int collaboratorId) {
        return UserDAO.addCollaboration(userId, collaboratorId);
    }

    public static boolean removeCollaboration(int userId, int collaboratorId) {
        return UserDAO.removeCollaboration(userId, collaboratorId);
    }

    public static List<User> getCollaborators(int userId) {
        return UserDAO.getCollaborators(userId);
    }

    public static boolean isCollaborator(int userId, int collaboratorId) {
        return UserDAO.isCollaborator(userId, collaboratorId);
    }

    public static List<User> searchUsersByUsername(String searchTerm) {
        return UserDAO.searchUsersByUsername(searchTerm);
    }

    public static boolean sendCollaborationRequest(int senderId, int receiverId) {
        if (senderId == receiverId) {
            return false; // Can't send request to self
        }
        return UserDAO.sendCollaborationRequest(senderId, receiverId);
    }

    public static List<CollaborationRequest> getPendingRequests(int userId) {
        return UserDAO.getPendingRequests(userId);
    }

    public static List<CollaborationRequest> getSentRequests(int userId) {
        return UserDAO.getSentRequests(userId);
    }

    public static boolean respondToRequest(int requestId, String status) {
        return UserDAO.respondToRequest(requestId, status);
    }

    public static boolean resendRequest(int requestId) {
        return UserDAO.resendRequest(requestId);
    }

    public static List<CollaborationRequest> getAllRequests(int userId) {
        List<CollaborationRequest> allRequests = new ArrayList<>();
        
        // Get sent requests
        List<CollaborationRequest> sentRequests = UserDAO.getSentRequests(userId);
        allRequests.addAll(sentRequests);
        
        // Get all received requests (not just pending ones)
        List<CollaborationRequest> receivedRequests = UserDAO.getReceivedRequests(userId);
        allRequests.addAll(receivedRequests);
        
        return allRequests;
    }

    public static boolean removeRequest(int requestId) {
        return UserDAO.removeRequest(requestId);
    }
}
