package View;

import Controller.UserController;
import Model.User;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserSelectionView extends JFrame {
    // Modern Microsoft style colors
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color ACCENT_COLOR = new Color(42, 120, 255);
    private static final Color HOVER_COLOR = new Color(230, 240, 255);
    private static final Color SECONDARY_TEXT = new Color(117, 117, 117);

    // Fonts
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    // Dimensions
    private static final int STANDARD_PADDING = 20;
    private static final int CARD_WIDTH = 180;
    private static final int CARD_HEIGHT = 180;
    private static final int GRID_GAPS = 20;

    private JPanel usersGrid;
    private UserSelectionViewListener listener;

    public UserSelectionView(UserSelectionViewListener listener) {
        this.listener = listener;
        initializeUI();
        loadUsers();
    }

    private void initializeUI() {
        setTitle("Choose Your Profile");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BACKGROUND_COLOR);
        
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(CARD_COLOR);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 225, 225)),
            new EmptyBorder(STANDARD_PADDING, STANDARD_PADDING, 
                          STANDARD_PADDING, STANDARD_PADDING)
        ));

        // Logo
        JLabel logoLabel = new JLabel("✓", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        logoLabel.setForeground(ACCENT_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createVerticalStrut(STANDARD_PADDING));

        // Title
        JLabel titleLabel = new JLabel("Choose Your Profile", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(titleLabel);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Select a profile to continue", SwingConstants.CENTER);
        subtitleLabel.setFont(BODY_FONT);
        subtitleLabel.setForeground(SECONDARY_TEXT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(STANDARD_PADDING));

        // Users Grid - fixed 3 columns
        usersGrid = new JPanel(new GridLayout(0, 3, GRID_GAPS, GRID_GAPS));
        usersGrid.setBackground(CARD_COLOR);
        centerPanel.add(usersGrid);

        // Add the center panel to main panel
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Set window size
        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
    }

    private void loadUsers() {
        List<User> users = UserController.getAllUsers();
        
        // Add user cards
        for (User user : users) {
            JButton userCard = createProfileCard(
                user.getUsername().substring(0, 1).toUpperCase(),
                user.getUsername(),
                false
            );
            userCard.addActionListener(e -> handleUserSelection(user));
            usersGrid.add(userCard);
        }

        // Add New Profile Button
        JButton addProfileButton = createProfileCard("+", "Add Profile", true);
        addProfileButton.addActionListener(e -> {
            CreateUserView createUserView = new CreateUserView();
            createUserView.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    refreshUsers();
                }
            });
            createUserView.setVisible(true);
        });
        usersGrid.add(addProfileButton);
    }

    private void refreshUsers() {
        usersGrid.removeAll();
        loadUsers();
        usersGrid.revalidate();
        usersGrid.repaint();
    }

    private JButton createProfileCard(String initial, String name, boolean isAddButton) {
        JButton card = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw avatar circle
                int circleDiameter = Math.min(getWidth(), getHeight()) - 40;
                int x = (getWidth() - circleDiameter) / 2;
                int y = (getHeight() - circleDiameter) / 2 - 15;
                
                g2.setColor(isAddButton ? ACCENT_COLOR : getRandomColor(name));
                g2.fillOval(x, y, circleDiameter, circleDiameter);

                // Draw initial/plus
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, isAddButton ? 36 : 28));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(initial)) / 2;
                int textY = y + (circleDiameter + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, textX, textY);

                // Draw name
                g2.setColor(TEXT_COLOR);
                g2.setFont(BODY_FONT);
                fm = g2.getFontMetrics();
                textX = (getWidth() - fm.stringWidth(name)) / 2;
                textY = getHeight() - 20;
                g2.drawString(name, textX, textY);
            }
        };

        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setBackground(CARD_COLOR);
        card.setBorderPainted(false);
        card.setFocusPainted(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(HOVER_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_COLOR);
            }
        });

        return card;
    }

    private Color getRandomColor(String seed) {
        int hash = seed.hashCode();
        float hue = (hash & 0xFF) / 255.0f;
        return Color.getHSBColor(hue, 0.7f, 0.9f);
    }

    private void handleUserSelection(User user) {
        if (listener != null) {
            listener.onUserSelected(user.getUsername());
        }
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new UserSelectionView(null).setVisible(true);
        });
    }
}
