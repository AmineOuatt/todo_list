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

public class CreateUserView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton createButton, backButton;

    // Modern Microsoft-style colors
    private static final Color PRIMARY_COLOR = new Color(42, 120, 255);    // Microsoft Blue
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243); // Light Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = new Color(225, 225, 225);
    private static final Color HOVER_COLOR = new Color(230, 240, 255);

    public CreateUserView() {
        setTitle("Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

        // Main container with BorderLayout for better horizontal layout
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        
        // Use GridBagLayout for perfect centering
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setBackground(BACKGROUND_COLOR);

        // Main panel with avatar, title and form
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0)); // Remove border for better centering

        // Avatar icon
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
                
                // Draw the "+" character for "Create" in the center
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, size / 2));
                FontMetrics fm = g2d.getFontMetrics();
                String symbol = "+";
                int textX = x + (size - fm.stringWidth(symbol)) / 2;
                int textY = y + (size + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(symbol, textX, textY);
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

        // Title and subtitle
        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        JLabel subtitleLabel = new JLabel("Enter your details below", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(117, 117, 117));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));

        // Form Panel - make it square and add inside the main panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2),
            new EmptyBorder(60, 60, 60, 60) // Equal padding on all sides for square shape
        ));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Set fixed square dimensions
        formPanel.setPreferredSize(new Dimension(600, 600));
        formPanel.setMinimumSize(new Dimension(600, 600));
        formPanel.setMaximumSize(new Dimension(600, 600));

        // Username
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        usernameLabel.setForeground(TEXT_COLOR);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(10));

        usernameField = new JTextField();
        styleTextField(usernameField);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(30));

        // Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setForeground(TEXT_COLOR);
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(10));

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(50));

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(CARD_COLOR);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsPanel.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        // Create button
        createButton = new JButton("Create Account");
        styleButton(createButton, true);
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.addActionListener(e -> handleCreateUser());
        buttonsPanel.add(createButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Back button
        backButton = new JButton("Back");
        styleButton(backButton, false);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            dispose();
            new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
        });
        buttonsPanel.add(backButton);
        buttonsPanel.add(Box.createVerticalStrut(20));
        
        // Exit button
        JButton exitButton = new JButton("Exit");
        styleButton(exitButton, false);
        exitButton.setForeground(new Color(220, 53, 69)); // Red for exit
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(e -> System.exit(0));
        buttonsPanel.add(exitButton);

        formPanel.add(buttonsPanel);
        
        // Add some extra space at the bottom to ensure visibility
        formPanel.add(Box.createVerticalStrut(30));
        
        // Add the form panel directly to the main panel for better positioning
        mainPanel.add(formPanel);
        container.add(mainPanel, BorderLayout.CENTER);
        
        // Add the container to the frame
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
        button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
        button.setForeground(isPrimary ? Color.WHITE : PRIMARY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPrimary ? PRIMARY_COLOR : BORDER_COLOR, 2),
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
                button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
            }
        });
    }

    private void handleCreateUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all fields",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (UserController.createUser(username, password)) {
            dispose();
            new LoginView(username).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to create user. Username may already exist.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
