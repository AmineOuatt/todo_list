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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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


public class UserManagementView extends JFrame {
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color ACCENT_COLOR = new Color(42, 120, 255);
    private static final Color HOVER_COLOR = new Color(230, 240, 255);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color BORDER_COLOR = new Color(225, 225, 225);
    
    private final User user;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton saveButton;
    private final JButton deleteButton;
    
    public UserManagementView(User user) {
        super("Edit Profile"); // Call JFrame constructor with title
        this.user = user;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Make the window fullscreen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Remove window decorations for true fullscreen
        setUndecorated(true);
        // Set size to match screen dimensions
        setSize(screenSize);
        setPreferredSize(screenSize);
        
        // Main container with BorderLayout
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
        
        // Avatar-style icon with user initial
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
                // Create a random color based on username
                Color userColor = getColorFromUsername(user.getUsername());
                g2d.setColor(userColor);
                g2d.fillOval(x, y, size, size);
                
                // Create highlight for 3D effect
                g2d.setClip(new Ellipse2D.Float(x, y, size, size));
                g2d.setPaint(new Color(255, 255, 255, 60));
                g2d.fillOval(x + size/4, y - size/8, size/2, size/3);
                g2d.setClip(null);
                
                // Draw the user's first initial in the center
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2d.getFontMetrics();
                String initial = user.getUsername().substring(0, 1).toUpperCase();
                int textX = x + (size - fm.stringWidth(initial)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(initial, textX, textY);
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
        
        // Title
        JLabel titleLabel = new JLabel("Edit Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        JLabel subtitleLabel = new JLabel("Manage your account", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(117, 117, 117));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));
        
        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            new EmptyBorder(60, 60, 60, 60) // Equal padding on all sides for square shape
        ));
        // Set fixed square dimensions
        formPanel.setPreferredSize(new Dimension(600, 600));
        formPanel.setMinimumSize(new Dimension(600, 600));
        formPanel.setMaximumSize(new Dimension(600, 600));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(10));
        
        usernameField = new JTextField(user.getUsername());
        styleTextField(usernameField);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setPreferredSize(new Dimension(400, 40));
        usernameField.setMaximumSize(new Dimension(400, 40));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(30));
        
        // Password field
        JLabel passwordLabel = new JLabel("New Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(10));
        
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setPreferredSize(new Dimension(400, 40));
        passwordField.setMaximumSize(new Dimension(400, 40));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(40));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(CARD_COLOR);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
        
        // Save button - primary action
        saveButton = new JButton("Save Changes");
        styleButton(saveButton, true);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setPreferredSize(new Dimension(300, 40));
        saveButton.setMaximumSize(new Dimension(300, 40));
        saveButton.addActionListener(e -> handleSave());
        buttonsPanel.add(saveButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Back button - secondary action
        JButton backButton = new JButton("Back");
        styleButton(backButton, false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setPreferredSize(new Dimension(300, 40));
        backButton.setMaximumSize(new Dimension(300, 40));
        backButton.addActionListener(e -> dispose());
        buttonsPanel.add(backButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Delete button - danger action with red text
        deleteButton = new JButton("Delete User");
        styleButton(deleteButton, false);
        deleteButton.setForeground(DANGER_COLOR);
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.setPreferredSize(new Dimension(300, 40));
        deleteButton.setMaximumSize(new Dimension(300, 40));
        deleteButton.addActionListener(e -> handleDelete());
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Exit button - secondary action with red text
        JButton exitButton = new JButton("Exit");
        styleButton(exitButton, false);
        exitButton.setForeground(DANGER_COLOR); // Red for exit
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setPreferredSize(new Dimension(300, 40));
        exitButton.setMaximumSize(new Dimension(300, 40));
        exitButton.addActionListener(e -> System.exit(0));
        buttonsPanel.add(exitButton);
        
        formPanel.add(buttonsPanel);
        
        // Add some extra space at the bottom to ensure visibility
        formPanel.add(Box.createVerticalStrut(20));
        
        mainPanel.add(formPanel);
        
        // Add the main panel to the center of the frame
        centeringPanel.add(mainPanel);
        container.add(centeringPanel, BorderLayout.CENTER);
        add(container);
        
        pack();
        setLocationRelativeTo(null);
    }
    
    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(500, 45));
        field.setMaximumSize(new Dimension(500, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
    }
    
    private void styleButton(JButton button, boolean isPrimary) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setPreferredSize(new Dimension(400, 50));
        button.setMaximumSize(new Dimension(400, 50));
        button.setBackground(isPrimary ? ACCENT_COLOR : Color.WHITE);
        button.setForeground(isPrimary ? Color.WHITE : ACCENT_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPrimary ? ACCENT_COLOR : BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(new Color(0, 99, 177));
                } else {
                    button.setBackground(HOVER_COLOR);
                }
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(isPrimary ? ACCENT_COLOR : Color.WHITE);
            }
        });
    }
    
    private Color getColorFromUsername(String username) {
        int hash = username.hashCode();
        float hue = Math.abs(hash % 360) / 360.0f;
        return Color.getHSBColor(hue, 0.7f, 0.9f);
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
                    // Navigate back to user selection
                    new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
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