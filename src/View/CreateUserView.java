package View;

import Controller.UserController;
import java.awt.*;
import javax.swing.*;

public class CreateUserView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton createButton, backButton;

    public CreateUserView() {
        setTitle("Create New User");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        createButton = new JButton("Create User");
        backButton = new JButton("Back");

        add(new JLabel("Enter Username:"));
        add(usernameField);
        add(new JLabel("Enter Password:"));
        add(passwordField);
        add(createButton);
        add(backButton);

        // Action to create a new user
        createButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty!");
                return;
            }

            if (UserController.createUser(username, password)) {
                JOptionPane.showMessageDialog(this, "User created successfully!");
                new UserSelectionView().setVisible(true); // Go back to user selection

                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create user!");
            }
        });

        // Back button to return to login
        backButton.addActionListener(e -> {
            new UserSelectionView().setVisible(true);

            dispose();
        });

        setVisible(true);
        setLocationRelativeTo(null);
    }
}
