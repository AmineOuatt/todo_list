package View;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import Controller.TaskController;
import Controller.CategoryController;
import Model.Category;
import Model.Note;
import Model.NoteDAO;
import Model.Task;
import Model.TaskDAO;

public class DashboardView extends JFrame {
    private int userId;
    private TaskFrame parentFrame;
    private boolean isStandaloneWindow;
    private JPanel contentPanel;
    
    // Modern color palette
    private static final Color PRIMARY_COLOR = new Color(63, 81, 181);
    private static final Color ACCENT_COLOR = new Color(83, 109, 254);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color DANGER_COLOR = new Color(244, 67, 54);
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color LIGHT_TEXT_COLOR = new Color(117, 117, 117);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final Color HOVER_COLOR = new Color(245, 245, 245);
    
    // Animation properties
    private Timer animationTimer;
    private int animationDuration = 500; // milliseconds
    private long animationStartTime;
    private final int ANIMATION_FRAMES = 60;
    private int currentFrame = 0;
    private Map<Component, Integer> animationTargets = new HashMap<>();
    
    // Task counts by status
    private int completedTasksCount = 0;
    private int pendingTasksCount = 0;
    private int overdueTasksCount = 0;
    
    // Task counts by category 
    private Map<String, Integer> taskCountsByCategory = new HashMap<>();
    // Track completed tasks by category for progress calculation
    private Map<String, Integer> completedTasksByCategory = new HashMap<>();
    // Total tasks by category
    private Map<String, Integer> totalTasksByCategory = new HashMap<>();
    
    // Chart animations
    private int progressBarAnimation = 0;
    private int circleProgressAnimation = 0;
    private Map<String, Integer> categoryBarAnimations = new HashMap<>();
    
    // Constructeur pour une fenêtre séparée
    public DashboardView(int userId, TaskFrame parentFrame) {
        this(userId, parentFrame, true);
    }
    
    // Constructeur qui peut créer soit une fenêtre séparée, soit un panneau intégré
    public DashboardView(int userId, TaskFrame parentFrame, boolean isStandaloneWindow) {
        this.userId = userId;
        this.parentFrame = parentFrame;
        this.isStandaloneWindow = isStandaloneWindow;
        
        if (isStandaloneWindow) {
            // Configuration pour une fenêtre séparée
            setTitle("Dashboard - Task Manager");
            setSize(1200, 800);
            setLocationRelativeTo(null);
            setBackground(BACKGROUND_COLOR);
            
            // Set to fullscreen mode
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            
            // Handle window closing
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    parentFrame.setVisible(true);
                    dispose();
                }
            });
        }
        
        // Load data from database
        loadDataFromDatabase();
        
        // Build UI
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        setupUI();
        
        if (isStandaloneWindow) {
            // Ajouter le contenu à la fenêtre
            add(contentPanel);
        }
        
        // Start animations
        startAnimations();
    }
    
    // Récupère le panneau de contenu pour l'intégration
    public JPanel getContentPanel() {
        return contentPanel;
    }
    
    private void startAnimations() {
        // Initial animation for summary cards
        animationTimer = new Timer();
        animationStartTime = System.currentTimeMillis();
        
        animationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentFrame++;
                
                // Update progress bar animations
                if (progressBarAnimation < 100) {
                    progressBarAnimation = Math.min(100, (currentFrame * 100) / ANIMATION_FRAMES);
                }
                
                // Update circle progress animation
                if (circleProgressAnimation < 100) {
                    circleProgressAnimation = Math.min(100, (currentFrame * 100) / ANIMATION_FRAMES);
                }
                
                // Update category bar animations
                for (String category : taskCountsByCategory.keySet()) {
                    if (!categoryBarAnimations.containsKey(category)) {
                        categoryBarAnimations.put(category, 0);
                    }
                    
                    int currentValue = categoryBarAnimations.get(category);
                    if (currentValue < 100) {
                        categoryBarAnimations.put(category, Math.min(100, (currentFrame * 100) / ANIMATION_FRAMES));
                    }
                }
                
                if (currentFrame >= ANIMATION_FRAMES) {
                    animationTimer.cancel();
                }
                
                // Repaint components
                SwingUtilities.invokeLater(() -> {
                    if (isStandaloneWindow) {
                        repaint();
                    } else {
                        contentPanel.repaint();
                    }
                });
            }
        }, 0, animationDuration / ANIMATION_FRAMES);
    }
    
    private void loadDataFromDatabase() {
        // Get all tasks for the user - utiliser getTasksWithOccurrences au lieu de getTasks
        List<Task> allTasks = TaskController.getTasksWithOccurrences(userId);
        
        // Mettre à zéro les compteurs
        completedTasksCount = 0;
        pendingTasksCount = 0;
        overdueTasksCount = 0;
        taskCountsByCategory.clear();
        completedTasksByCategory.clear();
        totalTasksByCategory.clear();
        
        // Count tasks by status
        for (Task task : allTasks) {
            String status = task.getStatus().toLowerCase();
            if (status.equals("completed")) {
                completedTasksCount++;
            } else if (status.equals("in progress")) {
                // Traiter "in progress" comme statut distinct
                pendingTasksCount++;
            } else if (status.equals("pending")) {
                // Check if task is overdue
                if (task.getDueDate() != null && task.getDueDate().before(new Date())) {
                    overdueTasksCount++;
                } else {
                    // Tasks with status "pending" that are not overdue
                    pendingTasksCount++;
                }
            }
            
            // Count by category
            String categoryName = "Uncategorized";
            if (task.getCategory() != null) {
                categoryName = task.getCategory().getName();
            }
            
            // Increment total tasks for this category
            totalTasksByCategory.put(
                categoryName, 
                totalTasksByCategory.getOrDefault(categoryName, 0) + 1
            );
            
            // Increment completed tasks for this category if task is completed
            if (status.equals("completed")) {
                completedTasksByCategory.put(
                    categoryName, 
                    completedTasksByCategory.getOrDefault(categoryName, 0) + 1
                );
            }
            
            // Keep the original count for backward compatibility
            taskCountsByCategory.put(
                categoryName, 
                taskCountsByCategory.getOrDefault(categoryName, 0) + 1
            );
        }
        
        System.out.println("Dashboard data loaded: " + allTasks.size() + " tasks found");
        System.out.println("Completed: " + completedTasksCount + ", In Progress: " + pendingTasksCount + ", Overdue: " + overdueTasksCount);
    }
    
    private void setupUI() {
        // Create main container with a modern design - réduire les marges
        JPanel mainContainer = new JPanel(new BorderLayout(15, 15));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Add header panel with back button and title - seulement si c'est une fenêtre séparée
        if (isStandaloneWindow) {
            JPanel headerPanel = createHeaderPanel();
            mainContainer.add(headerPanel, BorderLayout.NORTH);
        }
        
        // Create scrollable content panel avec une meilleure structure
        JPanel scrollableContent = new JPanel();
        // Utiliser un GridBagLayout pour un meilleur contrôle de la disposition
        scrollableContent.setLayout(new GridBagLayout());
        scrollableContent.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0); // Réduire l'espace vertical entre les composants
        
        // Welcome message with user info
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel welcomePanel = createWelcomePanel();
        scrollableContent.add(welcomePanel, gbc);
        
        // Add dashboard components - summary cards
        gbc.gridy = 1;
        JPanel summaryPanel = createSummaryPanel();
        scrollableContent.add(summaryPanel, gbc);
        
        // Structure principale avec 3 colonnes pour une meilleure utilisation de l'espace
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints contentGbc = new GridBagConstraints();
        contentGbc.fill = GridBagConstraints.BOTH;
        contentGbc.weightx = 1.0;
        contentGbc.weighty = 1.0;
        contentGbc.insets = new Insets(0, 0, 0, 10);
        
        // Première colonne: tâches récentes
        contentGbc.gridx = 0;
        contentGbc.gridy = 0;
        contentGbc.gridheight = 2;
        JPanel tasksPanel = createRecentTasksPanel();
        mainContentPanel.add(tasksPanel, contentGbc);
        
        // Deuxième colonne: statistiques
        contentGbc.gridx = 1;
        contentGbc.gridy = 0;
        contentGbc.gridheight = 1;
        contentGbc.insets = new Insets(0, 5, 10, 5);
        JPanel statsPanel = createStatisticsPanel();
        mainContentPanel.add(statsPanel, contentGbc);
        
        // Troisième colonne: distribution par catégorie
        contentGbc.gridx = 1;
        contentGbc.gridy = 1;
        JPanel categoryPanel = createCategoryDistributionPanel();
        mainContentPanel.add(categoryPanel, contentGbc);
        
        // Ajouter le panneau principal de contenu
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        scrollableContent.add(mainContentPanel, gbc);
        
        // Notes en bas de page pour équilibrer la mise en page
        gbc.gridy = 3;
        gbc.weighty = 0.5;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel notesPanel = createNotesPanel();
        scrollableContent.add(notesPanel, gbc);
        
        // Add scrollable container
        JScrollPane scrollPane = new JScrollPane(scrollableContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(mainContainer, BorderLayout.CENTER);
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // User greeting
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        
        String greeting;
        if (timeOfDay < 12) {
            greeting = "Good morning";
        } else if (timeOfDay < 18) {
            greeting = "Good afternoon";
        } else {
            greeting = "Good evening";
        }
        
        JLabel welcomeLabel = new JLabel(greeting + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(TEXT_COLOR);
        
        // Date display
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        JLabel dateLabel = new JLabel(dateFormat.format(new Date()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(LIGHT_TEXT_COLOR);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Dashboard");
        styleButton(refreshButton, true);
        refreshButton.addActionListener(e -> {
            refreshDashboard();
        });
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftPanel.add(welcomeLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(dateLabel);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(refreshButton, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 20, 15, 20)
        ));

        // Back button with modern styling
        JButton backButton = new JButton("← Back");
        styleButton(backButton, false);
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        // Title with better typography
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_COLOR);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 12, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Calculate overall progress percentage
        int totalTasks = completedTasksCount + pendingTasksCount + overdueTasksCount;
        int overallProgress = totalTasks > 0 ? (completedTasksCount * 100) / totalTasks : 0;
        
        // Task summary cards - titres plus explicites
        panel.add(createSummaryCard("Completed", completedTasksCount, SUCCESS_COLOR, "✓"));
        panel.add(createSummaryCard("In Progress", pendingTasksCount, WARNING_COLOR, "⏱"));
        panel.add(createSummaryCard("Overdue", overdueTasksCount, DANGER_COLOR, "⚠"));
        panel.add(createProgressSummaryCard("Completion", overallProgress, PRIMARY_COLOR));
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, int count, Color accentColor, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle with white background
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.setColor(CARD_COLOR);
                g2d.fill(roundedRectangle);
                
                // Draw subtle shadow
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(roundedRectangle);
                
                // Draw accent line at left side instead of top
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
                
                g2d.dispose();
            }
        };
        
        // Use a more compact layout
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setOpaque(false);

        // Icon in a circle with accent color background
        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a circular background for the icon
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2d.fillOval(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        iconContainer.setLayout(new BorderLayout());
        iconContainer.setPreferredSize(new Dimension(35, 35));
        iconContainer.setOpaque(false);
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconLabel.setForeground(accentColor);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconContainer.add(iconLabel, BorderLayout.CENTER);

        // Card content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Count with larger font
        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        countLabel.setForeground(TEXT_COLOR);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Card title below the count
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(LIGHT_TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(countLabel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(titleLabel);
        
        card.add(iconContainer, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createProgressSummaryCard(String title, int percentage, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle with white background
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12);
                g2d.setColor(CARD_COLOR);
                g2d.fill(roundedRectangle);
                
                // Draw subtle shadow
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(roundedRectangle);
                
                // Draw accent line at left side
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setOpaque(false);

        // Circle progress icon
        JPanel progressContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw a circular background
                g2d.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2d.fillOval(0, 0, getWidth(), getHeight());
                
                g2d.dispose();
            }
        };
        progressContainer.setLayout(new BorderLayout());
        progressContainer.setPreferredSize(new Dimension(35, 35));
        progressContainer.setOpaque(false);
        
        JLabel iconLabel = new JLabel("📊");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        iconLabel.setForeground(accentColor);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressContainer.add(iconLabel, BorderLayout.CENTER);

        // Card content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Percentage with larger font
        JLabel percentLabel = new JLabel(percentage + "%");
        percentLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        percentLabel.setForeground(TEXT_COLOR);
        percentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Card title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(LIGHT_TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Mini progress bar
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw background
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // Draw progress - animate based on current progress animation value
                int animatedPercentage = (percentage * progressBarAnimation) / 100;
                int progressWidth = (width * animatedPercentage) / 100;
                
                g2d.setColor(accentColor);
                g2d.fillRoundRect(0, 0, progressWidth, height, height, height);
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 4));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progressBar.setOpaque(false);
        
        contentPanel.add(percentLabel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(progressBar);
        
        card.add(progressContainer, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createSectionPanel(String title, boolean showAll) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Header panel with title and "All" link
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        if (showAll) {
            JButton allButton = new JButton("All >");
            allButton.setBorderPainted(false);
            allButton.setContentAreaFilled(false);
            allButton.setForeground(PRIMARY_COLOR);
            allButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            allButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            headerPanel.add(allButton, BorderLayout.EAST);
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createRecentTasksPanel() {
        // Create panel for recent tasks with border
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_COLOR);
        containerPanel.setBorder(createCardBorder());
        
        // Header avec titre "Sub Tasks" au lieu de "Tasks"
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("Sub Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Tasks list panel
        JPanel tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksPanel.setBackground(CARD_COLOR);
        tasksPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Load tasks from database
        List<Task> tasks = TaskController.getTasksWithOccurrences(userId);
        
        // Display all tasks, avec un max de 10 pour ne pas surcharger l'interface
        if (tasks.isEmpty()) {
            JPanel emptyState = createEmptyState("No tasks found", "Add some tasks to get started!");
            tasksPanel.add(emptyState);
        } else {
            // Trier par date due (les plus récents d'abord)
            tasks.sort((t1, t2) -> {
                if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                if (t1.getDueDate() == null) return 1;
                if (t2.getDueDate() == null) return -1;
                return t2.getDueDate().compareTo(t1.getDueDate());
            });
            
            int maxToShow = Math.min(10, tasks.size());
            for (int i = 0; i < maxToShow; i++) {
                tasksPanel.add(createTaskItem(tasks.get(i)));
                if (i < maxToShow - 1) {
                    tasksPanel.add(createSeparator());
                }
            }
        }
        
        // Wrap tasks in scroll pane
        JScrollPane scrollPane = new JScrollPane(tasksPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(scrollPane, BorderLayout.CENTER);
        
        return containerPanel;
    }
    
    private JPanel createEmptyState(String title, String description) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        
        // Empty state icon
        JLabel iconLabel = new JLabel("📊");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Empty state title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Empty state description
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(LIGHT_TEXT_COLOR);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);
        
        return panel;
    }
    
    private JPanel createTaskItem(Task task) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 5));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Checkbox for task completion
        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(CARD_COLOR);
        checkbox.setSelected(task.getStatus().equalsIgnoreCase("completed"));
        
        // Change checkbox style to make it more modern
        checkbox.setIcon(new ImageIcon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (checkbox.isSelected()) {
                    g2d.setColor(SUCCESS_COLOR);
                    g2d.fillRoundRect(x, y, 20, 20, 5, 5);
                    
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawLine(x + 5, y + 10, x + 8, y + 14);
                    g2d.drawLine(x + 8, y + 14, x + 15, y + 6);
                } else {
                    g2d.setColor(BORDER_COLOR);
                    g2d.drawRoundRect(x, y, 20, 20, 5, 5);
                }
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 20;
            }
            
            @Override
            public int getIconHeight() {
                return 20;
            }
        });
        
        checkbox.addActionListener(e -> {
            String newStatus = checkbox.isSelected() ? "Completed" : "Pending";
            TaskDAO.updateTaskStatus(task.getTaskId(), newStatus);
            
            // Update title appearance
            JPanel centerPanel = (JPanel) panel.getComponent(1);
            JLabel titleLabel = (JLabel) centerPanel.getComponent(0);
            
            if (checkbox.isSelected()) {
                titleLabel.setText("<html><span style='text-decoration: line-through; color: #757575;'>" 
                    + task.getTitle() + "</span></html>");
            } else {
                titleLabel.setText(task.getTitle());
            }
        });
        
        // Task title
        JLabel titleLabel = new JLabel();
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (task.getStatus().equalsIgnoreCase("completed")) {
            titleLabel.setText("<html><span style='text-decoration: line-through; color: #757575;'>" 
                + task.getTitle() + "</span></html>");
        } else {
            titleLabel.setText(task.getTitle());
            titleLabel.setForeground(TEXT_COLOR);
        }
        
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBackground(CARD_COLOR);
        centerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Add category badge if task has a category
        if (task.getCategory() != null) {
            JLabel categoryLabel = createCategoryBadge(task.getCategory().getName());
            JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            categoryPanel.setBackground(CARD_COLOR);
            categoryPanel.add(categoryLabel);
            centerPanel.add(categoryPanel, BorderLayout.SOUTH);
        }
        
        // Due date
        JLabel dateLabel = new JLabel(formatDueDate(task.getDueDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Set appropriate color for the date (warning for today, danger for overdue)
        if (task.getDueDate() != null) {
            Calendar today = Calendar.getInstance();
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(task.getDueDate());
            
            if (today.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dueDate.get(Calendar.DAY_OF_YEAR)) {
                dateLabel.setForeground(WARNING_COLOR);
            } else if (task.getDueDate().before(new Date())) {
                dateLabel.setForeground(DANGER_COLOR);
            } else {
                dateLabel.setForeground(LIGHT_TEXT_COLOR);
            }
        } else {
            dateLabel.setForeground(LIGHT_TEXT_COLOR);
        }
        
        panel.add(checkbox, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(dateLabel, BorderLayout.EAST);
        
        // Add hover effect
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(HOVER_COLOR);
                centerPanel.setBackground(HOVER_COLOR);
                checkbox.setBackground(HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(CARD_COLOR);
                centerPanel.setBackground(CARD_COLOR);
                checkbox.setBackground(CARD_COLOR);
            }
        });
        
        return panel;
    }
    
    private JLabel createCategoryBadge(String categoryName) {
        JLabel badge = new JLabel(categoryName);
        badge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        badge.setForeground(PRIMARY_COLOR);
        badge.setBackground(new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 20));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        
        return badge;
    }
    
    private JComponent createSeparator() {
        JPanel separator = new JPanel();
        separator.setBackground(BORDER_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setPreferredSize(new Dimension(10, 1));
        
        return separator;
    }
    
    private String formatDueDate(Date date) {
        if (date == null) {
            return "No due date";
        }
        
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        
        Calendar dueDate = Calendar.getInstance();
        dueDate.setTime(date);

        // Check if due date is today
        if (today.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == dueDate.get(Calendar.DAY_OF_YEAR)) {
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
            return "Today, " + timeFormat.format(date);
        }
        
        // Check if due date is tomorrow
        if (tomorrow.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
            tomorrow.get(Calendar.DAY_OF_YEAR) == dueDate.get(Calendar.DAY_OF_YEAR)) {
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
            return "Tomorrow, " + timeFormat.format(date);
        }
        
        // Use full date format for other dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        return dateFormat.format(date);
    }
    
    private javax.swing.border.Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        );
    }

    private JPanel createNotesPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_COLOR);
        containerPanel.setBorder(createCardBorder());
        containerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        
        // Header with title only (bouton "View All Notes" supprimé)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("Notes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Notes grid panel with flexible layout based on number of notes
        JPanel notesGrid = new JPanel();
        notesGrid.setBackground(CARD_COLOR);
        notesGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Get notes from database
        List<Note> notes = NoteDAO.getNotesByUserId(userId);
        
        // Display notes
        if (notes.isEmpty()) {
            notesGrid.setLayout(new BorderLayout());
            JPanel emptyState = createEmptyState("No notes found", "Create notes to see them here");
            notesGrid.add(emptyState, BorderLayout.CENTER);
        } else {
            // Trier par date de création (les plus récents d'abord)
            notes.sort((n1, n2) -> n2.getCreatedDate().compareTo(n1.getCreatedDate()));
            
            // Calcul du nombre de colonnes (3 max) et adaptation du layout
            int maxCols = 3;
            int displayCount = Math.min(6, notes.size()); // Limiter à 6 notes
            int cols = Math.min(maxCols, displayCount);
            int rows = (displayCount + cols - 1) / cols; // Arrondir au supérieur
            
            notesGrid.setLayout(new GridLayout(rows, cols, 15, 15));
            
            for (int i = 0; i < displayCount; i++) {
                notesGrid.add(createNoteCard(notes.get(i)));
            }
        }
        
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(notesGrid, BorderLayout.CENTER);
        
        return containerPanel;
    }
    
    private JPanel createNoteCard(Note note) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw sticky note appearance
                g2d.setColor(new Color(255, 248, 225)); // Light yellow background
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.fill(roundedRectangle);
                
                // Draw subtle shadow
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(roundedRectangle);
                
                // Draw a line at the top to simulate paper edge
                g2d.setColor(new Color(255, 224, 130));
                g2d.fillRect(0, 0, getWidth(), 5);
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Note title
        JLabel titleLabel = new JLabel(note.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        
        // Note content (truncated)
        String content = note.getContent();
        if (content.length() > 100) {
            content = content.substring(0, 97) + "...";
        }
        
        JLabel contentLabel = new JLabel("<html><div style='width: 180px;'>" + content + "</div></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentLabel.setForeground(TEXT_COLOR);
        contentLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // Note date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        JLabel dateLabel = new JLabel(dateFormat.format(note.getCreatedDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(LIGHT_TEXT_COLOR);
        
        // Add category badge if note has a category
        if (note.getCategory() != null) {
            JLabel categoryLabel = createCategoryBadge(note.getCategory().getName());
            JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            categoryPanel.setOpaque(false);
            categoryPanel.add(categoryLabel);
            
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            bottomPanel.add(categoryPanel, BorderLayout.WEST);
            bottomPanel.add(dateLabel, BorderLayout.EAST);
            
            card.add(bottomPanel, BorderLayout.SOUTH);
        } else {
            card.add(dateLabel, BorderLayout.SOUTH);
        }
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentLabel, BorderLayout.CENTER);
        
        // Add hover effect and click action
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 193, 7), 2),
                    BorderFactory.createEmptyBorder(13, 13, 13, 13)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Open note in notes view
            }
        });
        
        return card;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_COLOR);
        containerPanel.setBorder(createCardBorder());
        
        // Header avec accent color
        JPanel headerPanel = new JPanel(new BorderLayout()){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dessiner une barre d'accent à gauche
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillRect(0, 0, 5, getHeight());
                
                g2d.dispose();
            }
        };
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel titleLabel = new JLabel("Task Status Overview");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Calculate completion percentage
        int totalTasks = completedTasksCount + pendingTasksCount + overdueTasksCount;
        int completionPercentage = totalTasks > 0 ? (completedTasksCount * 100) / totalTasks : 0;
        int pendingPercentage = totalTasks > 0 ? (pendingTasksCount * 100) / totalTasks : 0;
        int overduePercentage = totalTasks > 0 ? (overdueTasksCount * 100) / totalTasks : 0;
        
        // Create chart panel with more compact layout
        JPanel chartPanel = new JPanel(new BorderLayout(20, 0));
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create status distribution table avec design amélioré
        JPanel statusTable = new JPanel();
        statusTable.setLayout(new BoxLayout(statusTable, BoxLayout.Y_AXIS));
        statusTable.setBackground(CARD_COLOR);
        
        // Add status rows with animated progress bars - labels mis à jour
        JPanel completedRow = createStatusRow("Completed", completedTasksCount, completionPercentage, SUCCESS_COLOR);
        JPanel pendingRow = createStatusRow("In Progress", pendingTasksCount, pendingPercentage, WARNING_COLOR);
        JPanel overdueRow = createStatusRow("Overdue", overdueTasksCount, overduePercentage, DANGER_COLOR);
        
        statusTable.add(completedRow);
        statusTable.add(Box.createVerticalStrut(10));
        statusTable.add(pendingRow);
        statusTable.add(Box.createVerticalStrut(10));
        statusTable.add(overdueRow);
        
        // Create doughnut chart avec design plus moderne
        JPanel doughnutChart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int diameter = Math.min(width, height) - 30; // Réduire pour plus de compacité
                int x = (width - diameter) / 2;
                int y = (height - diameter) / 2;
                int innerDiameter = diameter - 30; // Donut plus fin
                int innerX = (width - innerDiameter) / 2;
                int innerY = (height - innerDiameter) / 2;
                
                // Calculate animated percentages
                int animatedCompletionPercentage = (completionPercentage * circleProgressAnimation) / 100;
                int animatedPendingPercentage = (pendingPercentage * circleProgressAnimation) / 100;
                int animatedOverduePercentage = (overduePercentage * circleProgressAnimation) / 100;
                
                // Draw background circle (only if we have no tasks)
                if (totalTasks == 0) {
                    g2d.setColor(BORDER_COLOR);
                    g2d.fillArc(x, y, diameter, diameter, 0, 360);
                    g2d.setColor(CARD_COLOR);
                    g2d.fillArc(innerX, innerY, innerDiameter, innerDiameter, 0, 360);
                } else {
                    // Pour plus de netteté, ajouter une ombre légère
                    g2d.setColor(new Color(0, 0, 0, 5));
                    g2d.fillOval(x+2, y+2, diameter, diameter);
                    
                    // Draw segments for each status (in reverse order to get correct layering)
                    // Completed segment
                    if (animatedCompletionPercentage > 0) {
                        g2d.setColor(SUCCESS_COLOR);
                        g2d.fillArc(x, y, diameter, diameter, 90, -animatedCompletionPercentage * 360 / 100);
                    }
                
                    // Pending segment
                    if (animatedPendingPercentage > 0) {
                        g2d.setColor(WARNING_COLOR);
                        g2d.fillArc(x, y, diameter, diameter, 
                                   90 - (animatedCompletionPercentage * 360 / 100), 
                                   -animatedPendingPercentage * 360 / 100);
                    }
                    
                    // Overdue segment
                    if (animatedOverduePercentage > 0) {
                        g2d.setColor(DANGER_COLOR);
                        g2d.fillArc(x, y, diameter, diameter, 
                                  90 - ((animatedCompletionPercentage + animatedPendingPercentage) * 360 / 100),
                                  -animatedOverduePercentage * 360 / 100);
                    }
                    
                    // Draw inner circle to create donut
                    g2d.setColor(CARD_COLOR);
                    g2d.fillArc(innerX, innerY, innerDiameter, innerDiameter, 0, 360);
                }
                
                // Draw center text with modern font
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2d.setColor(TEXT_COLOR);
                
                String percentText;
                if (totalTasks == 0) {
                    percentText = "No tasks";
                } else {
                    percentText = animatedCompletionPercentage + "%";
                }
                
                int textWidth = g2d.getFontMetrics().stringWidth(percentText);
                int textHeight = g2d.getFontMetrics().getHeight();
                g2d.drawString(percentText, width / 2 - textWidth / 2, height / 2 + textHeight / 4);
                
                // Draw completed text
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.setColor(LIGHT_TEXT_COLOR);
                String completedText = "Completed";
                textWidth = g2d.getFontMetrics().stringWidth(completedText);
                g2d.drawString(completedText, width / 2 - textWidth / 2, height / 2 + textHeight + 5);
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(140, 140); // Plus compact
            }
        };
        
        // Affichage plus compact avec FlowLayout
        JPanel chartContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chartContainer.setBackground(CARD_COLOR);
        chartContainer.add(doughnutChart);
        
        chartPanel.add(chartContainer, BorderLayout.WEST);
        chartPanel.add(statusTable, BorderLayout.CENTER);
        
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(chartPanel, BorderLayout.CENTER);
        
        return containerPanel;
    }
    
    private JPanel createStatusRow(String status, int count, int percentage, Color statusColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setOpaque(false);
        
        // Status label with colored dot
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        labelPanel.setOpaque(false);
        
        JPanel colorDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(statusColor);
                g2d.fillOval(0, 0, 12, 12);
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(12, 12);
            }
        };
        
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(TEXT_COLOR);
        
        labelPanel.add(colorDot);
        labelPanel.add(statusLabel);
        
        // Count and percentage labels
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setOpaque(false);
        
        JLabel countLabel = new JLabel(count + " tasks");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(LIGHT_TEXT_COLOR);
        
        statsPanel.add(countLabel);
        
        // Create progress bar panel
        JPanel progressBarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw background
                g2d.setColor(new Color(238, 238, 238));
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // Calculate animated width based on percentage
                int animatedPercentage = (percentage * progressBarAnimation) / 100;
                int progressWidth = (width * animatedPercentage) / 100;
                
                // Draw progress
                g2d.setColor(statusColor);
                g2d.fillRoundRect(0, 0, progressWidth, height, height, height);
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 8);
            }
        };
        
        progressBarPanel.setPreferredSize(new Dimension(0, 8));
        
        // Layout
        panel.add(labelPanel, BorderLayout.NORTH);
        panel.add(progressBarPanel, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createCategoryDistributionPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_COLOR);
        containerPanel.setBorder(createCardBorder());
        
        // Header avec accent color
        JPanel headerPanel = new JPanel(new BorderLayout()){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dessiner une barre d'accent à gauche
                g2d.setColor(ACCENT_COLOR);
                g2d.fillRect(0, 0, 5, getHeight());
                
                g2d.dispose();
            }
        };
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel titleLabel = new JLabel("Tasks by Category");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create chart panel
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // If no categories, show message
        if (taskCountsByCategory.isEmpty()) {
            JPanel emptyState = createEmptyState("No categories found", "Add categories to your tasks");
            chartPanel.add(emptyState);
        } else {
            // Create bar chart visualization
            // Define category colors - we'll rotate through these
            Color[] categoryColors = {
                new Color(63, 81, 181),   // Primary blue
                new Color(0, 150, 136),   // Teal
                new Color(233, 30, 99),   // Pink
                new Color(156, 39, 176),  // Purple
                new Color(255, 87, 34),   // Deep Orange
                new Color(3, 169, 244)    // Light Blue
            };
            
            // Sort categories by task count in descending order
            List<Map.Entry<String, Integer>> sortedCategories = new ArrayList<>(taskCountsByCategory.entrySet());
            sortedCategories.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            int colorIndex = 0;
            int totalTasks = 0;
            for (int count : taskCountsByCategory.values()) {
                totalTasks += count;
            }
            
            // Add each category as a horizontal bar
            for (Map.Entry<String, Integer> entry : sortedCategories) {
                String category = entry.getKey();
                int count = entry.getValue();
                Color categoryColor = categoryColors[colorIndex % categoryColors.length];
                colorIndex++;
                
                // Calculate percentage
                int percentage = totalTasks > 0 ? (count * 100) / totalTasks : 0;
                
                // Calculate progress percentage for this category
                int totalInCategory = totalTasksByCategory.getOrDefault(category, 0);
                int completedInCategory = completedTasksByCategory.getOrDefault(category, 0);
                int categoryProgress = totalInCategory > 0 ? (completedInCategory * 100) / totalInCategory : 0;
                
                // Create category bar with animation - design plus moderne
                chartPanel.add(createCategoryBar(category, count, percentage, categoryProgress, categoryColor));
                
                // Add space between bars
                if (colorIndex < sortedCategories.size()) {
                    chartPanel.add(Box.createVerticalStrut(12));
                }
            }
        }
        
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(chartPanel, BorderLayout.CENTER);
        
        return containerPanel;
    }
    
    private JPanel createCategoryBar(String category, int count, int percentage, int progressPercentage, Color categoryColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        // Top section with category name, completion and count
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        // Ajouter un cercle de couleur pour la catégorie
        JPanel colorDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(categoryColor);
                g2d.fillOval(0, 0, 10, 10);
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(10, 10);
            }
        };
        
        JPanel categoryLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        categoryLabelPanel.setOpaque(false);
        categoryLabelPanel.add(colorDot);
        
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        categoryLabel.setForeground(TEXT_COLOR);
        categoryLabelPanel.add(categoryLabel);
        
        // Information de progression
        JLabel progressLabel = new JLabel(progressPercentage + "% complete");
        progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        progressLabel.setForeground(LIGHT_TEXT_COLOR);
        
        JLabel countLabel = new JLabel(count + " tasks");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(LIGHT_TEXT_COLOR);
        
        // Panel d'informations à droite
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        infoPanel.setOpaque(false);
        infoPanel.add(progressLabel);
        infoPanel.add(countLabel);
        
        topPanel.add(categoryLabelPanel, BorderLayout.WEST);
        topPanel.add(infoPanel, BorderLayout.EAST);
        
        // Middle section with animated progress bar - design amélioré
        JPanel barContainer = new JPanel(new BorderLayout(0, 4));
        barContainer.setOpaque(false);
        
        // Create the distribution bar
        JPanel distributionBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw background with rounded corners
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // Get the animation progress for this category
                int animationProgress = categoryBarAnimations.getOrDefault(category, 0);
                
                // Calculate width based on percentage and animation progress
                int barWidth = (width * percentage * animationProgress) / 10000; // divide by 100*100
                
                // Draw bar with gradient and rounded corners
                if (barWidth > 0) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, categoryColor,
                        barWidth, 0, new Color(categoryColor.getRed(), categoryColor.getGreen(), categoryColor.getBlue(), 200)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(0, 0, barWidth, height, height, height);
                }
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 12);
            }
        };
        
        // Create the completion progress bar
        JPanel progressBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Draw background with rounded corners
                g2d.setColor(new Color(240, 240, 240));
                g2d.fillRoundRect(0, 0, width, height, height, height);
                
                // Get animation progress
                int animationProgress = categoryBarAnimations.getOrDefault(category, 0);
                
                // Calculate width based on completion percentage and animation
                int progressWidth = (width * progressPercentage * animationProgress) / 10000;
                
                // Draw progress bar with rounded corners
                g2d.setColor(SUCCESS_COLOR);
                g2d.fillRoundRect(0, 0, progressWidth, height, height, height);
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 4);
            }
        };
        
        barContainer.add(distributionBar, BorderLayout.NORTH);
        barContainer.add(progressBar, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(barContainer, BorderLayout.CENTER);
        
        return panel;
    }

    private void styleButton(JButton button, boolean isPrimary) {
        if (isPrimary) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setForeground(PRIMARY_COLOR);
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(ACCENT_COLOR);
                } else {
                    button.setForeground(ACCENT_COLOR);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(PRIMARY_COLOR);
                } else {
                    button.setForeground(PRIMARY_COLOR);
                }
            }
        });
    }
    
    private JPanel createFoldersGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 3, 15, 15));
        grid.setBackground(CARD_COLOR);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Get all categories
        List<Category> categories = CategoryController.getAllCategories();
        
        // Create a card for each category
        Color[] categoryColors = {
            new Color(63, 81, 181),   // Indigo
            new Color(0, 150, 136),   // Teal
            new Color(233, 30, 99),   // Pink
            new Color(156, 39, 176),  // Purple
            new Color(255, 87, 34),   // Deep Orange
            new Color(3, 169, 244)    // Light Blue
        };
        
        int colorIndex = 0;
        for (Category category : categories) {
            createFolderItem(grid, category.getName(), categoryColors[colorIndex % categoryColors.length]);
            colorIndex++;
        }
        
        return grid;
    }
    
    private void createFolderItem(JPanel parent, String name, Color bgColor) {
        JPanel folderCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw folder shape with rounded corners
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.setColor(bgColor);
                g2d.fill(roundedRectangle);
                
                // Draw folder tab
                int tabWidth = 40;
                int tabHeight = 15;
                RoundRectangle2D tab = new RoundRectangle2D.Float(
                    15, 0, tabWidth, tabHeight, 5, 5
                );
                g2d.fill(tab);
                
                g2d.dispose();
            }
        };
        
        folderCard.setLayout(new BorderLayout());
        folderCard.setBorder(BorderFactory.createEmptyBorder(20, 15, 15, 15));
        folderCard.setPreferredSize(new Dimension(150, 100));
        folderCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        folderCard.setOpaque(false);
        
        // Folder icon
        JLabel iconLabel = new JLabel("📁");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setForeground(Color.WHITE);
        
        // Folder name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        
        // Count of tasks
        int count = taskCountsByCategory.getOrDefault(name, 0);
        JLabel countLabel = new JLabel(count + " tasks");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(new Color(255, 255, 255, 200));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(countLabel);
        
        folderCard.add(iconLabel, BorderLayout.WEST);
        folderCard.add(infoPanel, BorderLayout.CENTER);
        
        folderCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Implement category filter
            }
        });
        
        parent.add(folderCard);
    }

    // Méthode publique pour rafraîchir le dashboard de l'extérieur
    public void refreshDashboard() {
        // Réinitialiser les données
        completedTasksCount = 0;
        pendingTasksCount = 0;
        overdueTasksCount = 0;
        taskCountsByCategory.clear();
        completedTasksByCategory.clear();
        totalTasksByCategory.clear();
        
        // Recharger les données
        loadDataFromDatabase();
        
        // Reconstruire l'interface
        contentPanel.removeAll();
        setupUI();
        
        // Redémarrer les animations
        startAnimations();
        
        // Rafraîchir l'interface
        if (isStandaloneWindow) {
            revalidate();
            repaint();
        } else {
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }
}