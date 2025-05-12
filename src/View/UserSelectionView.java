package View;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import Controller.UserController;
import Model.User;

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
        
        // Set to fullscreen mode
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Remove window decorations for true fullscreen
        setUndecorated(true);
        
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Center panel - limit width for better appearance on large screens
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(CARD_COLOR);
        centerPanel.setMaximumSize(new Dimension(1200, Integer.MAX_VALUE));
        centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 225, 225)),
            new EmptyBorder(STANDARD_PADDING, STANDARD_PADDING, 
                          STANDARD_PADDING, STANDARD_PADDING)
        ));

        // Logo - improved checkmark icon
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
                Color lighterAccent = new Color(
                    Math.min(ACCENT_COLOR.getRed() + 40, 255),
                    Math.min(ACCENT_COLOR.getGreen() + 40, 255),
                    Math.min(ACCENT_COLOR.getBlue() + 40, 255)
                );
                
                g2d.setColor(lighterAccent);
                g2d.fillOval(x, y, size, size);
                
                // Add slight shadow for depth
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillOval(x + 3, y + 3, size, size);
                
                // Main circle
                g2d.setColor(ACCENT_COLOR);
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
                g2d.setClip(new java.awt.geom.Ellipse2D.Float(x, y, size, size));
                g2d.setPaint(new Color(255, 255, 255, 60));
                g2d.fillOval(x + size/4, y - size/6, size, size/3);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 100);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(logoPanel);
        centerPanel.add(Box.createVerticalStrut(STANDARD_PADDING * 2));

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
        centerPanel.add(Box.createVerticalStrut(STANDARD_PADDING * 2));

        // Users Grid panel with fixed width and auto rows
        // Calculate number of columns based on screen size - min 3, max 6
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int columns = Math.max(3, Math.min(6, screenSize.width / 300));
        
        usersGrid = new JPanel(new GridLayout(0, columns, GRID_GAPS, GRID_GAPS));
        usersGrid.setBackground(CARD_COLOR);
        
        // Wrap grid in a panel with fixed width to ensure proper centering
        JPanel gridWrapper = new JPanel();
        gridWrapper.setLayout(new BoxLayout(gridWrapper, BoxLayout.Y_AXIS));
        gridWrapper.setBackground(CARD_COLOR);
        gridWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add the grid to the wrapper with some padding
        JPanel paddedGrid = new JPanel();
        paddedGrid.setLayout(new FlowLayout(FlowLayout.CENTER));
        paddedGrid.setBackground(CARD_COLOR);
        paddedGrid.add(usersGrid);
        
        gridWrapper.add(paddedGrid);
        centerPanel.add(gridWrapper);
        
        // Add exit button at the bottom
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitButton.setForeground(new Color(220, 53, 69)); // Red color
        exitButton.setBackground(Color.WHITE);
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> System.exit(0));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        centerPanel.add(Box.createVerticalStrut(STANDARD_PADDING * 2));
        centerPanel.add(exitButton);

        // Wrap center panel in a container to center it horizontally
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(BACKGROUND_COLOR);
        centerContainer.add(centerPanel);

        // Add the center container to main panel
        mainPanel.add(centerContainer, BorderLayout.CENTER);
        add(mainPanel);
        
        // Remove size constraints - fill the screen
        setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        pack();
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
            userCard.addActionListener(e -> {
                // When a user card is clicked, open the LoginView with the selected username
                LoginView loginView = new LoginView(user.getUsername());
                loginView.setVisible(true);
                dispose(); // Close this window
            });
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
        // Calculate better card size based on screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int dynamicCardWidth = Math.min(CARD_WIDTH * 2, screenSize.width / 8);
        int dynamicCardHeight = Math.min(CARD_HEIGHT * 2, screenSize.height / 5);
        
        JButton card = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Draw avatar circle
                int circleDiameter = Math.min(getWidth(), getHeight()) - 60;
                int x = (getWidth() - circleDiameter) / 2;
                int y = (getHeight() - circleDiameter) / 2 - 20;
                
                g2.setColor(isAddButton ? ACCENT_COLOR : getRandomColor(name));
                g2.fillOval(x, y, circleDiameter, circleDiameter);

                // Draw initial/plus
                g2.setColor(Color.WHITE);
                int fontSize = isAddButton ? 46 : 36;
                g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(initial)) / 2;
                int textY = y + (circleDiameter + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, textX, textY);

                // Draw name
                g2.setColor(TEXT_COLOR);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Larger font for name
                fm = g2.getFontMetrics();
                textX = (getWidth() - fm.stringWidth(name)) / 2;
                textY = getHeight() - 30;
                g2.drawString(name, textX, textY);
            }
            
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(dynamicCardWidth, dynamicCardHeight);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(dynamicCardWidth, dynamicCardHeight);
            }
            
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };

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
