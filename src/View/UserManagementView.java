package View;

import Controller.UserController;
import Model.User;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


public class UserManagementView extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color ACCENT_COLOR = new Color(42, 120, 255);
    private static final Color HOVER_COLOR = new Color(230, 240, 255);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    
    private final User user;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton saveButton;
    private final JButton deleteButton;
    
    public UserManagementView(User user) {
        super("Edit Profile"); // Call JFrame constructor with title
        this.user = user;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBackground(BACKGROUND_COLOR);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(CARD_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Title
        JLabel titleLabel = new JLabel("Edit Profile");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Username field
        JPanel usernamePanel = new JPanel(new BorderLayout(10, 0));
        usernamePanel.setBackground(CARD_COLOR);
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(BODY_FONT);
        usernameField = new JTextField(user.getUsername());
        usernameField.setFont(BODY_FONT);
        usernamePanel.add(usernameLabel, BorderLayout.WEST);
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Password field
        JPanel passwordPanel = new JPanel(new BorderLayout(10, 0));
        passwordPanel.setBackground(CARD_COLOR);
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(BODY_FONT);
        passwordField = new JPasswordField();
        passwordField.setFont(BODY_FONT);
        passwordPanel.add(passwordLabel, BorderLayout.WEST);
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createVerticalStrut(25));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBackground(CARD_COLOR);
        
        // Save button
        saveButton = new JButton("Save Changes");
        saveButton.setFont(BODY_FONT);
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(ACCENT_COLOR);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setFocusPainted(false);
        styleButton(saveButton, ACCENT_COLOR);
        
        // Delete button
        deleteButton = new JButton("Delete User");
        deleteButton.setFont(BODY_FONT);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBackground(DANGER_COLOR);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setFocusPainted(false);
        styleButton(deleteButton, DANGER_COLOR);
        
        buttonsPanel.add(saveButton);
        buttonsPanel.add(deleteButton);
        mainPanel.add(buttonsPanel);
        
        // Add action listeners
        saveButton.addActionListener(e -> handleSave());
        deleteButton.addActionListener(e -> handleDelete());
        
        // Add main panel to frame
        getContentPane().add(mainPanel);
        
        // Set up frame
        pack();
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void styleButton(JButton button, Color baseColor) {
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
            }
        });
    }
    
    private void handleSave() {
        String newUsername = usernameField.getText().trim();
        String newPassword = new String(passwordField.getPassword());
        
        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username cannot be empty", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update only if there are changes
        boolean hasChanges = false;
        
        if (!newUsername.equals(user.getUsername())) {
            hasChanges = true;
        }
        
        if (!newPassword.isEmpty()) {
            hasChanges = true;
        }
        
        if (hasChanges) {
            try {
                if (UserController.updateUser(user.getUserId(), newUsername, newPassword)) {
                    JOptionPane.showMessageDialog(this, 
                        "Changes saved successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Error saving changes: Username might already exist", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving changes: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            dispose();
        }
    }
    
    private void handleDelete() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this user?\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (choice == JOptionPane.YES_OPTION) {
            try {
                if (UserController.deleteUser(user.getUserId())) {
                    JOptionPane.showMessageDialog(this,
                        "User deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting user",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting user: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 