package View;

import Controller.UserController;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

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
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Logo and Title
        JLabel logoLabel = new JLabel("✓", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(logoLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Enter your details below", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(117, 117, 117));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(CARD_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)
        ));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        usernameLabel.setForeground(TEXT_COLOR);
        formPanel.add(usernameLabel);
        formPanel.add(Box.createVerticalStrut(5));

        usernameField = new JTextField();
        styleTextField(usernameField);
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(15));

        // Password
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        passwordLabel.setForeground(TEXT_COLOR);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createVerticalStrut(5));

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(20));

        // Buttons Panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBackground(CARD_COLOR);

        // Back button
        backButton = new JButton("Back");
        styleButton(backButton, false);
        backButton.addActionListener(e -> {
            dispose();
            new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
        });

        // Create button
        createButton = new JButton("Create Account");
        styleButton(createButton, true);
        createButton.addActionListener(e -> handleCreateUser());

        buttonsPanel.add(backButton);
        buttonsPanel.add(createButton);
        formPanel.add(buttonsPanel);

        mainPanel.add(formPanel);
        add(mainPanel);
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(300, 35));
        field.setMaximumSize(new Dimension(2000, 35));
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
        button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
        button.setForeground(isPrimary ? Color.WHITE : PRIMARY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPrimary ? PRIMARY_COLOR : BORDER_COLOR),
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
