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
}
