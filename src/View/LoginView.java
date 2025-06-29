package View;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
        setSize(800, 600);  // Wider size for horizontal layout
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Set to fullscreen mode
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Main container using BorderLayout for better horizontal layout
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Logo and Title
        // Replace the Unicode logo with custom drawn checkmark
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw circular background with gradient effect
                int size = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                
                // Create gradient for a 3D effect
                Color lighterPrimary = new Color(
                    Math.min(PRIMARY_COLOR.getRed() + 40, 255),
                    Math.min(PRIMARY_COLOR.getGreen() + 40, 255),
                    Math.min(PRIMARY_COLOR.getBlue() + 40, 255)
                );
                
                g2d.setColor(lighterPrimary);
                g2d.fillOval(x, y, size, size);
                
                // Add slight shadow for depth
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval(x + 3, y + 3, size, size);
                
                // Main circle
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillOval(x, y, size, size);
                
                // Draw improved checkmark with thicker stroke
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Adjust checkmark positioning for better appearance
                int startX = x + size / 4;
                int startY = y + size / 2 + size / 12;
                int middleX = x + size / 2 - size / 12;
                int middleY = y + size * 3/4;
                int endX = x + size * 3/4 + size / 12;
                int endY = y + size / 3;
                
                g2d.drawLine(startX, startY, middleX, middleY);
                g2d.drawLine(middleX, middleY, endX, endY);
                
                // Add highlight for a glossy effect
                g2d.setClip(new Ellipse2D.Float(x, y, size, size));
                g2d.setPaint(new Color(255, 255, 255, 60));
                g2d.fillOval(x + size/4, y - size/6, size, size/3);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(80, 80);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logoPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        JLabel titleLabel = new JLabel("Welcome to To-Do", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(117, 117, 117));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Login Panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBackground(CARD_COLOR);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)
        ));
        loginPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE)); // Limit width for better appearance

        // User Selection
        JLabel userLabel = new JLabel("Select User");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(TEXT_COLOR);
        loginPanel.add(userLabel);
        loginPanel.add(Box.createVerticalStrut(5));

        List<User> users = UserController.getAllUsers();
        userDropdown = new JComboBox<>(users.toArray(new User[0]));
        styleComboBox(userDropdown);
        if (selectedUsername != null) {
            for (int i = 0; i < userDropdown.getItemCount(); i++) {
                if (userDropdown.getItemAt(i).getUsername().equals(selectedUsername)) {
                    userDropdown.setSelectedIndex(i);
                    break;
                }
            }
        }
        loginPanel.add(userDropdown);
        loginPanel.add(Box.createVerticalStrut(15));

        // Password Field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setForeground(TEXT_COLOR);
        loginPanel.add(passwordLabel);
        loginPanel.add(Box.createVerticalStrut(5));

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(20));

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(CARD_COLOR);

        // Login Button
        loginButton = new JButton("Sign In");
        styleButton(loginButton, true);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.add(loginButton);
        buttonsPanel.add(Box.createVerticalStrut(10));

        // Manage User Button
        JButton manageButton = new JButton("Manage User");
        styleButton(manageButton, false);
        manageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        manageButton.addActionListener(e -> {
            User selectedUser = (User) userDropdown.getSelectedItem();
            if (selectedUser != null) {
                new UserManagementView(selectedUser).setVisible(true);
            }
        });
        buttonsPanel.add(manageButton);
        buttonsPanel.add(Box.createVerticalStrut(10));

        // Create Account Button
        createUserButton = new JButton("Create New Account");
        styleButton(createUserButton, false);
        createUserButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.add(createUserButton);
        buttonsPanel.add(Box.createVerticalStrut(10));

        // Back Button
        JButton backButton = new JButton("Back");
        styleButton(backButton, false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            dispose();
            new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
        });
        buttonsPanel.add(backButton);

        loginPanel.add(buttonsPanel);
        mainPanel.add(loginPanel);

        // Create User Panel (initially hidden)
        createUserPanel = createUserPanel();
        createUserPanel.setVisible(false);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(createUserPanel);

        // Add action listeners
        loginButton.addActionListener(e -> handleLogin());
        createUserButton.addActionListener(e -> {
            dispose();
            new CreateUserView().setVisible(true);
        });
        passwordField.addActionListener(e -> handleLogin());

        // Update manage button state when user selection changes
        userDropdown.addActionListener(e -> {
            User selectedUser = (User) userDropdown.getSelectedItem();
            manageButton.setEnabled(selectedUser != null);
        });

        // Add the mainPanel to the center of the container
        container.add(mainPanel, BorderLayout.CENTER);
        
        // Add the container to the frame
        add(container);
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // Username
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usernameLabel.setForeground(TEXT_COLOR);
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(5));

        newUsernameField = new JTextField();
        styleTextField(newUsernameField);
        panel.add(newUsernameField);
        panel.add(Box.createVerticalStrut(15));

        // Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setForeground(TEXT_COLOR);
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(5));

        newPasswordField = new JPasswordField();
        styleTextField(newPasswordField);
        panel.add(newPasswordField);
        panel.add(Box.createVerticalStrut(20));

        // Create Button
        JButton createButton = new JButton("Create Account");
        styleButton(createButton, true);
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.addActionListener(e -> handleCreateUser());
        panel.add(createButton);

        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(300, 35));
        field.setMaximumSize(new Dimension(300, 35));
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
        comboBox.setPreferredSize(new Dimension(300, 35));
        comboBox.setMaximumSize(new Dimension(300, 35));
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
