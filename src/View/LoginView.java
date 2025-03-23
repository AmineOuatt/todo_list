package View;

import Controller.UserController;
import Model.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class LoginView extends JFrame {
    private JComboBox<User> userDropdown;
    private JPasswordField passwordField;
    private JButton loginButton, createUserButton;
    private JTextField newUsernameField;
    private JPasswordField newPasswordField;
    private JPanel createUserPanel;
    private static final Color BUTTON_COLOR = new Color(70, 70, 70);
    private static final Color BACKGROUND_COLOR = new Color(50, 50, 50);
    private static final Color HOVER_COLOR = new Color(80, 80, 80);
    private static final Color CARD_BACKGROUND = new Color(60, 60, 60);

    public LoginView(String selectedUsername) {
        setTitle("To-Do List App");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BACKGROUND_COLOR);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Top panel with back button and title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND_COLOR);
        
        JButton backButton = new JButton("← Back");
        styleBackButton(backButton);
        backButton.addActionListener(e -> dispose());
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Subtitle
        JLabel subtitleLabel = new JLabel("Please sign in to continue");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createVerticalStrut(40));

        // Login card
        JPanel loginCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(CARD_BACKGROUND);
        loginCard.setBorder(new EmptyBorder(30, 30, 30, 30));

        // User selection panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setBackground(CARD_BACKGROUND);
        JLabel userLabel = new JLabel("Select User:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userPanel.add(userLabel);

        // Fetch existing users
        List<User> users = UserController.getAllUsers();
        userDropdown = new JComboBox<>(users.toArray(new User[0]));
        styleComboBox(userDropdown);
        
        // Set selected user if provided
        if (selectedUsername != null) {
            for (int i = 0; i < userDropdown.getItemCount(); i++) {
                User user = userDropdown.getItemAt(i);
                if (user.getUsername().equals(selectedUsername)) {
                    userDropdown.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        userPanel.add(userDropdown);
        loginCard.add(userPanel);
        loginCard.add(Box.createVerticalStrut(20));

        // Password panel
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setBackground(CARD_BACKGROUND);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordPanel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        passwordPanel.add(passwordField);
        loginCard.add(passwordPanel);
        loginCard.add(Box.createVerticalStrut(30));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(CARD_BACKGROUND);

        loginButton = new JButton("Sign In");
        createUserButton = new JButton("Create New User");
        styleButton(loginButton);
        styleButton(createUserButton);

        buttonPanel.add(loginButton);
        buttonPanel.add(createUserButton);
        loginCard.add(buttonPanel);

        mainPanel.add(loginCard);

        // Create user panel (initially hidden)
        createUserPanel = createUserPanel();
        createUserPanel.setVisible(false);
        mainPanel.add(createUserPanel);

        // Add action listeners
        passwordField.addActionListener(e -> handleLogin());
        loginButton.addActionListener(e -> handleLogin());
        createUserButton.addActionListener(e -> toggleCreateUserPanel());

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Title
        JLabel titleLabel = new JLabel("Create New User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        // Username field
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.setBackground(CARD_BACKGROUND);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernamePanel.add(usernameLabel);

        newUsernameField = new JTextField(20);
        styleTextField(newUsernameField);
        usernamePanel.add(newUsernameField);
        panel.add(usernamePanel);
        panel.add(Box.createVerticalStrut(20));

        // Password field
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setBackground(CARD_BACKGROUND);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordPanel.add(passwordLabel);

        newPasswordField = new JPasswordField(20);
        styleTextField(newPasswordField);
        passwordPanel.add(newPasswordField);
        panel.add(passwordPanel);
        panel.add(Box.createVerticalStrut(30));

        // Create button
        JButton createButton = new JButton("Create Account");
        styleButton(createButton);
        createButton.addActionListener(e -> handleCreateUser());
        panel.add(createButton);

        return panel;
    }

    private void toggleCreateUserPanel() {
        createUserPanel.setVisible(!createUserPanel.isVisible());
        createUserButton.setText(createUserPanel.isVisible() ? "Cancel" : "Create New User");
    }

    private void handleCreateUser() {
        String username = newUsernameField.getText();
        String password = new String(newPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Fields cannot be empty!");
            return;
        }

        if (UserController.createUser(username, password)) {
            showSuccessMessage("User created successfully!");
            newUsernameField.setText("");
            newPasswordField.setText("");
            toggleCreateUserPanel();
            // Refresh user dropdown
            List<User> users = UserController.getAllUsers();
            userDropdown.setModel(new DefaultComboBoxModel<>(users.toArray(new User[0])));
        } else {
            showErrorMessage("Failed to create user!");
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
                showErrorMessage("Invalid password!");
            }
        }
    }

    private void styleComboBox(JComboBox<User> comboBox) {
        comboBox.setBackground(CARD_BACKGROUND);
        comboBox.setForeground(Color.WHITE);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(200, 35));
        comboBox.setBorder(new LineBorder(BUTTON_COLOR, 1));
    }

    private void styleTextField(JTextField textField) {
        textField.setBackground(CARD_BACKGROUND);
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(200, 35));
        textField.setBorder(new LineBorder(BUTTON_COLOR, 1));
    }

    private void styleButton(JButton button) {
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });
    }

    private void styleBackButton(JButton button) {
        button.setBackground(BACKGROUND_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }
        });
    }

    private void showErrorMessage(String message) {
        JDialog dialog = new JDialog(this, "Error", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showSuccessMessage(String message) {
        JDialog dialog = new JDialog(this, "Success", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel();
        panel.setBackground(BACKGROUND_COLOR);
        JLabel label = new JLabel(message);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
}
