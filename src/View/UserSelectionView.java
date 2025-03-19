package View;

import Controller.UserController;
import Model.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class UserSelectionView extends JFrame {
    public UserSelectionView() {
        setTitle("Select User");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Select Your Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Fetch existing users
        List<User> users = UserController.getAllUsers(); // Make sure this returns List<User>

        JPanel usersPanel = new JPanel();
        usersPanel.setLayout(new FlowLayout());

        // Create user selection buttons
        for (User user : users) {
            JButton userButton = new JButton(user.getUsername()); // Show username on button
            userButton.setPreferredSize(new Dimension(120, 120));
            userButton.setFont(new Font("Arial", Font.BOLD, 14));

            userButton.addActionListener(e -> {
                dispose();
                new LoginView().setVisible(true); // Open LoginView without user ID


 // Pass correct user ID
            });

            usersPanel.add(userButton);
        }

        add(usersPanel, BorderLayout.CENTER);

        // Add "Create New User" button
        JButton createUserButton = new JButton("Create New User");
        createUserButton.setFont(new Font("Arial", Font.BOLD, 16));
        createUserButton.addActionListener(e -> {
            dispose();
            new CreateUserView().setVisible(true);
        });

        add(createUserButton, BorderLayout.SOUTH);

        setVisible(true);
        setLocationRelativeTo(null); // Center on screen
    }
}
