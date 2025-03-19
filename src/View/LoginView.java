package View;

import Controller.UserController;
import Model.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class LoginView extends JFrame {
    private JComboBox<User> userDropdown;
    private JPasswordField passwordField;
    private JButton loginButton, createUserButton;

    public LoginView() {
        setTitle("Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        // Fetch existing users
        List<User> users = UserController.getAllUsers();
        userDropdown = new JComboBox<>(users.toArray(new User[0]));

        passwordField = new JPasswordField();
        loginButton = new JButton("Login");
        createUserButton = new JButton("Create New User");

        add(new JLabel("Select User:"));
        add(userDropdown);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(createUserButton);

        // Login action
        loginButton.addActionListener(e -> {
            User selectedUser = (User) userDropdown.getSelectedItem();
            String password = new String(passwordField.getPassword());

            if (selectedUser != null) {
                int userId = selectedUser.getUserId(); // Get user ID
                if (UserController.authenticateUser(selectedUser.getUsername(), password)) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose();
                    new TaskFrame(userId).setVisible(true); // Open TaskFrame with userId
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid credentials!");
                }
            }
        });

        // Create new user action
        createUserButton.addActionListener(e -> {
            new CreateUserView().setVisible(true);
            dispose();
        });

        setVisible(true);
        setLocationRelativeTo(null);
    }
    
}
