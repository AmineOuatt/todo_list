package View;

import Controller.UserController;
import Model.User;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class UserSelectionView extends JFrame {
    private static final Color BUTTON_COLOR = new Color(70, 70, 70);
    private static final Color BACKGROUND_COLOR = new Color(50, 50, 50);
    private static final Color HOVER_COLOR = new Color(80, 80, 80);
    private static final Color CARD_BACKGROUND = new Color(60, 60, 60);
    private JPanel gridPanel;
    private UserSelectionViewListener listener;

    public UserSelectionView(UserSelectionViewListener listener) {
        this.listener = listener;
        setTitle("To-Do List App");
        setSize(800, 600);
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
        backButton.addActionListener(e -> System.exit(0));
        topPanel.add(backButton, BorderLayout.WEST);

        JLabel titleLabel = new JLabel("Who's watching?");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        mainPanel.add(topPanel);
        mainPanel.add(Box.createVerticalStrut(40));

        // Grid panel for profiles
        gridPanel = new JPanel(new GridLayout(2, 4, 20, 20));
        gridPanel.setBackground(BACKGROUND_COLOR);

        // Add profile buttons
        List<User> users = UserController.getAllUsers();
        for (User user : users) {
            JPanel profilePanel = createProfileCard(user);
            gridPanel.add(profilePanel);
        }

        // Add "Add Profile" button
        JPanel addProfilePanel = createAddProfileCard();
        gridPanel.add(addProfilePanel);

        mainPanel.add(gridPanel);
        mainPanel.add(Box.createVerticalStrut(40));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton manageButton = new JButton("Manage Profiles");
        styleButton(manageButton);

        buttonPanel.add(manageButton);
        mainPanel.add(buttonPanel);

        // Add action listeners
        manageButton.addActionListener(e -> showManageProfilesDialog());

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private JPanel createProfileCard(User user) {
        JPanel card = new JPanel() {
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton profileButton = createProfileButton(user.getUsername());
        JLabel nameLabel = new JLabel(user.getUsername());
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add hover effect to the entire card
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(50, 50, 50));
                card.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(CARD_BACKGROUND);
                card.repaint();
            }
        });

        card.add(profileButton);
        card.add(Box.createVerticalStrut(10));
        card.add(nameLabel);

        return card;
    }

    private JPanel createAddProfileCard() {
        JPanel card = new JPanel() {
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton addProfileButton = createAddProfileButton();
        JLabel addLabel = new JLabel("Add Profile");
        addLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        addLabel.setForeground(Color.WHITE);
        addLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(addProfileButton);
        card.add(Box.createVerticalStrut(10));
        card.add(addLabel);

        return card;
    }

    private JButton createProfileButton(String username) {
        JButton button = new JButton(String.valueOf(username.charAt(0)).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(100, 100));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 32));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        // Click action
        button.addActionListener(e -> {
            listener.onUserSelected(username);
        });

        return button;
    }

    private JButton createAddProfileButton() {
        JButton button = new JButton("+") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setPreferredSize(new Dimension(100, 100));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 48));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_COLOR);
            }
        });

        // Click action
        button.addActionListener(e -> {
            showCreateUserDialog();
        });

        return button;
    }

    private void showCreateUserDialog() {
        JDialog dialog = new JDialog(this, "Create New User", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Create New User");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Username field
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.setBackground(BACKGROUND_COLOR);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernamePanel.add(usernameLabel);

        JTextField usernameField = new JTextField(20);
        styleTextField(usernameField);
        usernamePanel.add(usernameField);
        mainPanel.add(usernamePanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Password field
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passwordPanel.setBackground(BACKGROUND_COLOR);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordPanel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        passwordPanel.add(passwordField);
        mainPanel.add(passwordPanel);
        mainPanel.add(Box.createVerticalStrut(30));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        styleButton(createButton);
        styleButton(cancelButton);

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);

        // Add action listeners
        createButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showErrorMessage("Fields cannot be empty!");
                return;
            }

            if (UserController.createUser(username, password)) {
                showSuccessMessage("User created successfully!");
                refreshUserList();
                dialog.dispose();
            } else {
                showErrorMessage("Failed to create user!");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showManageProfilesDialog() {
        JDialog dialog = new JDialog(this, "Manage Profiles", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Manage Profiles");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Profile list
        JList<User> profileList = new JList<>(UserController.getAllUsers().toArray(new User[0]));
        profileList.setBackground(Color.WHITE);
        profileList.setForeground(Color.WHITE);
        profileList.setFont(new Font("Arial", Font.PLAIN, 14));
        profileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        profileList.setBorder(new LineBorder(BUTTON_COLOR, 1));
        JScrollPane scrollPane = new JScrollPane(profileList);
        scrollPane.setBackground(BACKGROUND_COLOR);
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        styleButton(editButton);
        styleButton(deleteButton);

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        mainPanel.add(buttonPanel);

        // Add action listeners
        editButton.addActionListener(e -> {
            User selectedUser = profileList.getSelectedValue();
            if (selectedUser != null) {
                showEditProfileDialog(selectedUser);
            } else {
                showErrorMessage("Please select a profile to edit");
            }
        });

        deleteButton.addActionListener(e -> {
            User selectedUser = profileList.getSelectedValue();
            if (selectedUser != null) {
                showDeleteProfileDialog(selectedUser);
            } else {
                showErrorMessage("Please select a profile to delete");
            }
        });

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showEditProfileDialog(User user) {
        JDialog dialog = new JDialog(this, "Edit Profile", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Username field
        JTextField usernameField = new JTextField(user.getUsername());
        styleTextField(usernameField);
        mainPanel.add(new JLabel("Username:"));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createVerticalStrut(10));

        // Password field
        JPasswordField passwordField = new JPasswordField();
        styleTextField(passwordField);
        mainPanel.add(new JLabel("New Password (leave blank to keep current):"));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        styleButton(saveButton);
        styleButton(cancelButton);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel);

        // Add action listeners
        saveButton.addActionListener(e -> {
            String newUsername = usernameField.getText();
            String newPassword = new String(passwordField.getPassword());
            
            if (newUsername.isEmpty()) {
                showErrorMessage("Username cannot be empty");
                return;
            }

            if (UserController.updateUser(user.getUserId(), newUsername, newPassword)) {
                showSuccessMessage("Profile updated successfully!");
                refreshUserList();
            } else {
                showErrorMessage("Failed to update profile");
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showDeleteProfileDialog(User user) {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the profile '" + user.getUsername() + "'?",
            "Delete Profile",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (UserController.deleteUser(user.getUserId())) {
                showSuccessMessage("Profile deleted successfully!");
                refreshUserList();
            } else {
                showErrorMessage("Failed to delete profile");
            }
        }
    }

    public void refreshUserList() {
        gridPanel.removeAll();
        List<User> users = UserController.getAllUsers();
        for (User user : users) {
            JPanel profilePanel = createProfileCard(user);
            gridPanel.add(profilePanel);
        }
        JPanel addProfilePanel = createAddProfileCard();
        gridPanel.add(addProfilePanel);
        gridPanel.revalidate();
        gridPanel.repaint();
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

    private void styleButton(JButton button) {
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 40));
        
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

    private void styleTextField(JTextField textField) {
        textField.setBackground(Color.WHITE);
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(200, 35));
        textField.setBorder(new LineBorder(BUTTON_COLOR, 1));
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
}
