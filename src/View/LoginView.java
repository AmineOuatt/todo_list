package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import Controller.UserController;
import Model.User;

public class LoginView extends JFrame {
    private JComboBox<User> userDropdown;
    private JPasswordField passwordField;
    private JButton loginButton, createUserButton;
    private JTextField newUsernameField;
    private JPasswordField newPasswordField;
    private JPanel createUserPanel;
    
    // Modern Microsoft-style colors
    private static final Color PRIMARY_COLOR = new Color(42, 120, 255);    // Microsoft Blue
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243); // Light Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = new Color(225, 225, 225);

    public LoginView(String selectedUsername) {
        setTitle("To-Do List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Set to fullscreen mode
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Remove window decorations for true fullscreen
        setUndecorated(true);
        // Set size to match screen dimensions
        setSize(screenSize);
        setPreferredSize(screenSize);

        // Main container using BorderLayout for better horizontal layout
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        
        // Use GridBagLayout for perfect centering
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setBackground(BACKGROUND_COLOR);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Logo and Title
        // Create avatar-style logo like in UserSelectionView
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get dimensions for circular avatar
                int size = Math.min(getWidth(), getHeight()) - 30;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Create outer glow/shadow
                g2d.setColor(new Color(0, 0, 0, 15));
                g2d.fillOval(x + 4, y + 4, size, size);
                
                // Draw main circular background
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillOval(x, y, size, size);
                
                // Create highlight for 3D effect
                g2d.setClip(new Ellipse2D.Float(x, y, size, size));
                g2d.setPaint(new Color(255, 255, 255, 60));
                g2d.fillOval(x + size/4, y - size/8, size/2, size/3);
                g2d.setClip(null);
                
                // Draw the "T" character for "Todo" in the center
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2d.getFontMetrics();
                String letter = "T";
                int textX = x + (size - fm.stringWidth(letter)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(letter, textX, textY);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(120, 120);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logoPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        JLabel titleLabel = new JLabel("Welcome to To-Do", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel subtitleLabel = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(117, 117, 117));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));

        // Login Panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBackground(CARD_COLOR);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            new EmptyBorder(60, 60, 60, 60) // Equal padding on all sides for square shape
        ));
        loginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Set fixed square dimensions
        loginPanel.setPreferredSize(new Dimension(600, 600));
        loginPanel.setMinimumSize(new Dimension(600, 600));
        loginPanel.setMaximumSize(new Dimension(600, 600));

        // User Selection
        JLabel userLabel = new JLabel("Select User");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(userLabel);
        loginPanel.add(Box.createVerticalStrut(10));

        List<User> users = UserController.getAllUsers();
        userDropdown = new JComboBox<>(users.toArray(new User[0]));
        styleComboBox(userDropdown);
        userDropdown.setAlignmentX(Component.CENTER_ALIGNMENT);
        userDropdown.setPreferredSize(new Dimension(500, 45));
        userDropdown.setMaximumSize(new Dimension(500, 45));
        if (selectedUsername != null) {
            for (int i = 0; i < userDropdown.getItemCount(); i++) {
                if (userDropdown.getItemAt(i).getUsername().equals(selectedUsername)) {
                    userDropdown.setSelectedIndex(i);
                    break;
                }
            }
        }
        loginPanel.add(userDropdown);
        loginPanel.add(Box.createVerticalStrut(30));

        // Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginPanel.add(passwordLabel);
        loginPanel.add(Box.createVerticalStrut(10));

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setPreferredSize(new Dimension(500, 45));
        passwordField.setMaximumSize(new Dimension(500, 45));
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(40));

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(CARD_COLOR);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        // Login Button - primary action
        loginButton = new JButton("Sign In");
        styleButton(loginButton, true);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setPreferredSize(new Dimension(400, 50));
        loginButton.setMaximumSize(new Dimension(400, 50));
        loginButton.addActionListener(e -> handleLogin());
        buttonsPanel.add(loginButton);
        buttonsPanel.add(Box.createVerticalStrut(20));

        // Back Button - secondary action
        JButton backButton = new JButton("Back");
        styleButton(backButton, false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setPreferredSize(new Dimension(400, 50));
        backButton.setMaximumSize(new Dimension(400, 50));
        backButton.addActionListener(e -> {
            dispose();
            new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
        });
        buttonsPanel.add(backButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Create Account Button - secondary action
        createUserButton = new JButton("Create New Account");
        styleButton(createUserButton, false);
        createUserButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createUserButton.setPreferredSize(new Dimension(400, 50));
        createUserButton.setMaximumSize(new Dimension(400, 50));
        createUserButton.addActionListener(e -> {
            dispose();
            new CreateUserView().setVisible(true);
        });
        buttonsPanel.add(createUserButton);
        buttonsPanel.add(Box.createVerticalStrut(20));

        // Manage User Button - secondary action
        JButton manageButton = new JButton("Manage User");
        styleButton(manageButton, false);
        manageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageButton.setPreferredSize(new Dimension(400, 50));
        manageButton.setMaximumSize(new Dimension(400, 50));
        manageButton.addActionListener(e -> {
            User selectedUser = (User) userDropdown.getSelectedItem();
            if (selectedUser != null) {
                new UserManagementView(selectedUser).setVisible(true);
            }
        });
        buttonsPanel.add(manageButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Exit button - secondary action with red text
        JButton exitButton = new JButton("Exit");
        styleButton(exitButton, false);
        exitButton.setForeground(new Color(220, 53, 69)); // Red for exit
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setPreferredSize(new Dimension(400, 50));
        exitButton.setMaximumSize(new Dimension(400, 50));
        exitButton.addActionListener(e -> System.exit(0));
        buttonsPanel.add(exitButton);

        loginPanel.add(buttonsPanel);
        
        // Add some extra space at the bottom to ensure visibility
        loginPanel.add(Box.createVerticalStrut(20));
        
        mainPanel.add(loginPanel);

        // Create User Panel (initially hidden)
        createUserPanel = createUserPanel();
        createUserPanel.setVisible(false);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createUserPanel);

        // Add action listeners
        passwordField.addActionListener(e -> handleLogin());

        // Update manage button state when user selection changes
        userDropdown.addActionListener(e -> {
            manageButton.setEnabled(userDropdown.getSelectedItem() != null);
        });

        // Add the main panel to the center of the frame
        centeringPanel.add(mainPanel);
        container.add(centeringPanel, BorderLayout.CENTER);
        add(container);
        
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            new EmptyBorder(60, 60, 60, 60) // Equal padding on all sides for square shape
        ));
        // Set fixed square dimensions
        panel.setPreferredSize(new Dimension(600, 600));
        panel.setMinimumSize(new Dimension(600, 600));
        panel.setMaximumSize(new Dimension(600, 600));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        JLabel subtitleLabel = new JLabel("Enter your details below", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(117, 117, 117));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(40));

        // Username
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(10));

        newUsernameField = new JTextField();
        styleTextField(newUsernameField);
        newUsernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        newUsernameField.setMaximumSize(new Dimension(500, 45));
        panel.add(newUsernameField);
        panel.add(Box.createVerticalStrut(30));

        // Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(10));

        newPasswordField = new JPasswordField();
        styleTextField(newPasswordField);
        newPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        newPasswordField.setMaximumSize(new Dimension(500, 45));
        panel.add(newPasswordField);
        panel.add(Box.createVerticalStrut(40));

        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(CARD_COLOR);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        
        // Create Button
        JButton createButton = new JButton("Create Account");
        styleButton(createButton, true);
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.setPreferredSize(new Dimension(400, 50));
        createButton.setMaximumSize(new Dimension(400, 50));
        createButton.addActionListener(e -> handleCreateUser());
        buttonsPanel.add(createButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, false);
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.setPreferredSize(new Dimension(300, 40));
        cancelButton.setMaximumSize(new Dimension(300, 40));
        cancelButton.addActionListener(e -> toggleCreateUserPanel());
        buttonsPanel.add(cancelButton);
        
        panel.add(buttonsPanel);

        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(400, 40));
        field.setMaximumSize(new Dimension(400, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleButton(JButton button, boolean isPrimary) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(300, 40));
        button.setMaximumSize(new Dimension(300, 40));
        button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
        button.setForeground(isPrimary ? Color.WHITE : PRIMARY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPrimary ? PRIMARY_COLOR : BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(new Color(0, 99, 177));
                } else {
                    button.setBackground(new Color(230, 240, 255));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
            }
        });
    }

    private void styleComboBox(JComboBox<User> comboBox) {
        comboBox.setPreferredSize(new Dimension(400, 40));
        comboBox.setMaximumSize(new Dimension(400, 40));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_COLOR);
        ((JComponent) comboBox.getRenderer()).setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void toggleCreateUserPanel() {
        createUserPanel.setVisible(!createUserPanel.isVisible());
        createUserButton.setText(createUserPanel.isVisible() ? "Cancel" : "Create New Account");
    }

    private void handleCreateUser() {
        String username = newUsernameField.getText();
        String password = new String(newPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all fields",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (UserController.createUser(username, password)) {
            JOptionPane.showMessageDialog(this,
                "Account created successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            newUsernameField.setText("");
            newPasswordField.setText("");
            toggleCreateUserPanel();
            
            // Refresh user dropdown
            List<User> users = UserController.getAllUsers();
            userDropdown.setModel(new DefaultComboBoxModel<>(users.toArray(new User[0])));
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to create account. Username might already exist.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogin() {
        User selectedUser = (User) userDropdown.getSelectedItem();
        String password = new String(passwordField.getPassword());

        if (selectedUser != null) {
            if (UserController.authenticateUser(selectedUser.getUsername(), password)) {
                new TaskFrame(selectedUser.getUserId()).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid password!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
