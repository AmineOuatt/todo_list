package Controller;

import Model.User;
import Model.UserDAO;
import java.util.List;

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
}
