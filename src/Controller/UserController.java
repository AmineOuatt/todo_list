package Controller;

import Model.User;
import Model.UserDAO;
import java.util.List;

public class UserController {
    public static List<User> getAllUsers() {
        return UserDAO.getAllUsers();
    }

    public static boolean authenticateUser(String username, String password) {
        return UserDAO.authenticateUser(username, password);
    }

    public static boolean createUser(String username, String password) {
        return UserDAO.createUser(username, password);
    }

    public static boolean updateUser(int userId, String newUsername, String newPassword) {
        return UserDAO.updateUser(userId, newUsername, newPassword);
    }

    public static boolean deleteUser(int userId) {
        return UserDAO.deleteUser(userId);
    }

    public static User getUserByUsername(String username) {
        return UserDAO.getUserByUsername(username);
    }
}
