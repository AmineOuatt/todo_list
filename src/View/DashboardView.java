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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(41, 98, 255);
    private static final Color ACCENT_COLOR = new Color(83, 145, 255);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color WARNING_COLOR = new Color(255, 152, 0);
    private static final Color DANGER_COLOR = new Color(211, 47, 47);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 37, 41);
    private static final Color LIGHT_TEXT_COLOR = new Color(108, 117, 125);
    private static final Color BORDER_COLOR = new Color(233, 236, 239);
    
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
    
    public DashboardView(int userId, TaskFrame parentFrame) {
        this.userId = userId;
        this.parentFrame = parentFrame;
        
        setTitle("Dashboard");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        
        // Set to fullscreen mode
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Load data from database
        loadDataFromDatabase();
        
        // Build UI
        setupUI();
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.setVisible(true);
                dispose();
            }
        });
    }
    
    private void loadDataFromDatabase() {
        // Get all tasks for the user
        List<Task> allTasks = TaskDAO.getTasksByUserId(userId);
        
        // Count tasks by status
        for (Task task : allTasks) {
            String status = task.getStatus().toLowerCase();
            if (status.equals("completed")) {
                completedTasksCount++;
            } else if (status.equals("pending")) {
                // Check if task is overdue
                if (task.getDueDate() != null && task.getDueDate().before(new Date())) {
                    overdueTasksCount++;
                } else {
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
    }
    
    private void setupUI() {
        // Create main container with a modern design
        JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add header panel with back button and title
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        
        // Create scrollable content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // Add dashboard components
        contentPanel.add(createSummaryPanel());
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Create main two-column layout for tasks and stats
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setBorder(null);
        mainSplitPane.setDividerSize(20);
        mainSplitPane.setResizeWeight(0.5);
        
        JPanel leftPanel = new JPanel(new BorderLayout(0, 20));
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.add(createRecentTasksPanel(), BorderLayout.CENTER);
        
        JPanel rightPanel = new JPanel(new BorderLayout(0, 20));
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.add(createStatisticsPanel(), BorderLayout.NORTH);
        rightPanel.add(createCategoryDistributionPanel(), BorderLayout.CENTER);
        
        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightPanel);
        
        contentPanel.add(mainSplitPane);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Add notes section
        contentPanel.add(createNotesPanel());
        
        // Add scrollable container
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContainer.add(scrollPane, BorderLayout.CENTER);

        add(mainContainer);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            new EmptyBorder(15, 20, 15, 20)
        ));

        // Back button with modern styling
        JButton backButton = new JButton("← Back to Main");
        styleButton(backButton, false);
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        // Title with better typography
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Calculate overall progress percentage
        int totalTasks = completedTasksCount + pendingTasksCount + overdueTasksCount;
        int overallProgress = totalTasks > 0 ? (completedTasksCount * 100) / totalTasks : 0;
        
        // Task summary cards
        panel.add(createSummaryCard("Completed Tasks", completedTasksCount, SUCCESS_COLOR));
        panel.add(createSummaryCard("Pending Tasks", pendingTasksCount, WARNING_COLOR));
        panel.add(createSummaryCard("Overdue Tasks", overdueTasksCount, DANGER_COLOR));
        panel.add(createProgressSummaryCard("Overall Progress", overallProgress, PRIMARY_COLOR));
        
        return panel;
    }
    
    private JPanel createSummaryCard(String title, int count, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle with white background
                g2d.setColor(CARD_COLOR);
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.fill(roundedRectangle);
                
                // Draw left accent border
                g2d.setColor(accentColor);
                g2d.fillRect(0, 0, 5, getHeight());
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setOpaque(false);

        // Card title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(LIGHT_TEXT_COLOR);
        
        // Card count
        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        countLabel.setForeground(TEXT_COLOR);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createProgressSummaryCard(String title, int percentage, Color accentColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle with white background
                g2d.setColor(CARD_COLOR);
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.fill(roundedRectangle);
                
                // Draw left accent border
                g2d.setColor(accentColor);
                g2d.fillRect(0, 0, 5, getHeight());
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        card.setOpaque(false);

        // Card title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(LIGHT_TEXT_COLOR);
        
        // Card percentage
        JLabel percentLabel = new JLabel(percentage + "%");
        percentLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        percentLabel.setForeground(TEXT_COLOR);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(percentLabel, BorderLayout.CENTER);
        
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
        
        // Header with title and "See All" link
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("Recent Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        
        JButton viewAllButton = new JButton("View All");
        viewAllButton.setBorderPainted(false);
        viewAllButton.setContentAreaFilled(false);
        viewAllButton.setForeground(PRIMARY_COLOR);
        viewAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllButton, BorderLayout.EAST);
        
        // Tasks list panel
        JPanel tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksPanel.setBackground(CARD_COLOR);
        tasksPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Load tasks from database
        List<Task> tasks = TaskDAO.getTasksByUserId(userId);
        
        // Display up to 5 most recent tasks
        int count = 0;
        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tasks found. Add some tasks to get started!");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(LIGHT_TEXT_COLOR);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tasksPanel.add(emptyLabel);
        } else {
            for (Task task : tasks) {
                if (count >= 5) break;
                tasksPanel.add(createTaskItem(task));
                if (count < tasks.size() - 1 && count < 4) {
                    tasksPanel.add(createSeparator());
                }
                count++;
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
    
    private JPanel createTaskItem(Task task) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        // Checkbox for task completion
        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(CARD_COLOR);
        checkbox.setSelected(task.getStatus().equalsIgnoreCase("completed"));
        checkbox.addActionListener(e -> {
            String newStatus = checkbox.isSelected() ? "Completed" : "Pending";
            TaskDAO.updateTaskStatus(task.getTaskId(), newStatus);
            
            // Update title appearance
            JLabel titleLabel = (JLabel) panel.getComponent(1);
            if (checkbox.isSelected()) {
                titleLabel.setText("<html><span style='text-decoration: line-through; color: #6c757d;'>" 
                    + task.getTitle() + "</span></html>");
            } else {
                titleLabel.setText(task.getTitle());
            }
        });
        
        // Task title
        JLabel titleLabel = new JLabel();
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (task.getStatus().equalsIgnoreCase("completed")) {
            titleLabel.setText("<html><span style='text-decoration: line-through; color: #6c757d;'>" 
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
                panel.setBackground(new Color(250, 250, 250));
                panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(CARD_COLOR);
            }
        });
        
        return panel;
    }
    
    private JLabel createCategoryBadge(String categoryName) {
        JLabel label = new JLabel(categoryName);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(PRIMARY_COLOR);
        label.setBackground(new Color(232, 240, 254));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return label;
    }
    
    private JComponent createSeparator() {
        JPanel separator = new JPanel();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setPreferredSize(new Dimension(100, 1));
        separator.setBackground(BORDER_COLOR);
        return separator;
    }
    
    private String formatDueDate(Date date) {
        if (date == null) return "No due date";
        
        Calendar today = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        dueDate.setTime(date);

        if (today.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == dueDate.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        } else if (today.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
                   today.get(Calendar.DAY_OF_YEAR) + 1 == dueDate.get(Calendar.DAY_OF_YEAR)) {
            return "Tomorrow";
        } else if (date.before(new Date())) {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
            return "Overdue: " + sdf.format(date);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
            return "Due: " + sdf.format(date);
        }
    }
    
    private javax.swing.border.Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        );
    }

    private JPanel createFoldersGrid() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 10));
        panel.setBackground(Color.WHITE);

        // Sample folders - you can modify these or load from your data
        createFolderItem(panel, "Work", new Color(200, 230, 255));
        createFolderItem(panel, "Training", new Color(200, 255, 220));
        createFolderItem(panel, "Personal", new Color(255, 220, 255));

        return panel;
    }

    private void createFolderItem(JPanel parent, String name, Color bgColor) {
        JPanel folderPanel = new JPanel();
        folderPanel.setLayout(new BoxLayout(folderPanel, BoxLayout.Y_AXIS));
        folderPanel.setBackground(bgColor);
        folderPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        folderPanel.setPreferredSize(new Dimension(100, 100));

        JLabel iconLabel = new JLabel("📁");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        folderPanel.add(iconLabel);
        folderPanel.add(Box.createVerticalStrut(5));
        folderPanel.add(nameLabel);

        parent.add(folderPanel);
    }

    private JPanel createNotesPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_COLOR);
        containerPanel.setBorder(createCardBorder());
        
        // Header with title and "See All" link
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("Recent Notes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        
        JButton viewAllButton = new JButton("View All");
        viewAllButton.setBorderPainted(false);
        viewAllButton.setContentAreaFilled(false);
        viewAllButton.setForeground(PRIMARY_COLOR);
        viewAllButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllButton.addActionListener(e -> {
            // Navigate to notes view
            JFrame notesFrame = new JFrame("Notes");
            notesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            notesFrame.setSize(1200, 800);
            notesFrame.setLocationRelativeTo(null);
            notesFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            
            NotesView notesView = new NotesView(userId);
            notesFrame.add(notesView);
            notesFrame.setVisible(true);
            
            dispose();
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(viewAllButton, BorderLayout.EAST);
        
        // Notes grid panel
        JPanel notesGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        notesGrid.setBackground(CARD_COLOR);
        notesGrid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Get up to 3 most recent notes
        List<Note> notes = NoteDAO.getNotesByUserId(userId);
        
        if (notes.isEmpty()) {
            // Create empty state message
            JLabel emptyLabel = new JLabel("No notes found. Create some notes to see them here!");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(LIGHT_TEXT_COLOR);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            notesGrid.setLayout(new BorderLayout());
            notesGrid.add(emptyLabel, BorderLayout.CENTER);
        } else {
            // Show up to 3 notes
            int count = 0;
            for (Note note : notes) {
                if (count >= 3) break;
                notesGrid.add(createNoteCard(note));
                count++;
            }
            
            // Fill remaining space if needed
            for (int i = count; i < 3; i++) {
                notesGrid.add(createEmptyNoteCard());
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
                
                // Draw rounded rectangle with light yellow background
                g2d.setColor(new Color(255, 248, 225));
                RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10);
                g2d.fill(roundedRectangle);
                
                // Draw subtle border
                g2d.setColor(new Color(255, 236, 179));
                g2d.setStroke(new BasicStroke(1));
                g2d.draw(roundedRectangle);
                
                g2d.dispose();
            }
        };
        
        card.setLayout(new BorderLayout(5, 10));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        card.setOpaque(false);
        
        // Note title
        JLabel titleLabel = new JLabel(note.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        
        // Note content preview (limit to first 100 chars)
        String contentPreview = note.getContent();
        if (contentPreview.length() > 100) {
            contentPreview = contentPreview.substring(0, 100) + "...";
        }
        
        JLabel contentLabel = new JLabel("<html><div style='width: 200px'>" + contentPreview + "</div></html>");
        contentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        contentLabel.setForeground(new Color(97, 97, 97));
        
        // Date and category info at bottom
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        bottomPanel.setOpaque(false);
        
        // Format created date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        JLabel dateLabel = new JLabel(sdf.format(note.getCreatedDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        dateLabel.setForeground(LIGHT_TEXT_COLOR);
        
        bottomPanel.add(dateLabel, BorderLayout.WEST);
        
        // Add category badge if note has a category
        if (note.getCategory() != null) {
            JLabel categoryLabel = createCategoryBadge(note.getCategory().getName());
            bottomPanel.add(categoryLabel, BorderLayout.EAST);
        }
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentLabel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);
        
        // Add hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });
        
        return card;
    }
    
    private JPanel createEmptyNoteCard() {
        // Create an empty panel to maintain grid layout
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        return emptyPanel;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_COLOR);
        containerPanel.setBorder(createCardBorder());
        
        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("Task Status Distribution");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Calculate completion percentage
        int totalTasks = completedTasksCount + pendingTasksCount + overdueTasksCount;
        int completionPercentage = totalTasks > 0 ? (completedTasksCount * 100) / totalTasks : 0;
        int pendingPercentage = totalTasks > 0 ? (pendingTasksCount * 100) / totalTasks : 0;
        int overduePercentage = totalTasks > 0 ? (overdueTasksCount * 100) / totalTasks : 0;
        
        // Create chart panel
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create status distribution table
        JPanel statusTable = new JPanel();
        statusTable.setLayout(new BoxLayout(statusTable, BoxLayout.Y_AXIS));
        statusTable.setBackground(CARD_COLOR);
        
        // Add status rows
        JPanel completedRow = createStatusRow("Completed", completedTasksCount, completionPercentage, SUCCESS_COLOR);
        JPanel pendingRow = createStatusRow("Pending", pendingTasksCount, pendingPercentage, WARNING_COLOR);
        JPanel overdueRow = createStatusRow("Overdue", overdueTasksCount, overduePercentage, DANGER_COLOR);
        
        statusTable.add(completedRow);
        statusTable.add(Box.createVerticalStrut(10));
        statusTable.add(pendingRow);
        statusTable.add(Box.createVerticalStrut(10));
        statusTable.add(overdueRow);
        
        // Create progress chart
        JPanel progressChart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                int diameter = Math.min(width, height) - 40;
                int x = (width - diameter) / 2;
                int y = (height - diameter) / 2;
                int thickness = 20;
                
                // Draw background circle
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(thickness));
                g2d.drawArc(x, y, diameter, diameter, 0, 360);
                
                // Draw completion arc
                g2d.setColor(SUCCESS_COLOR);
                g2d.drawArc(x, y, diameter, diameter, 90, -(completionPercentage * 360) / 100);
                
                // Draw pending arc
                g2d.setColor(WARNING_COLOR);
                g2d.drawArc(x, y, diameter, diameter, 90 - (completionPercentage * 360) / 100, 
                           -(pendingPercentage * 360) / 100);
                
                // Draw overdue arc
                g2d.setColor(DANGER_COLOR);
                g2d.drawArc(x, y, diameter, diameter, 
                           90 - ((completionPercentage + pendingPercentage) * 360) / 100, 
                           -(overduePercentage * 360) / 100);
                
                // Draw percentage text
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
                g2d.setColor(TEXT_COLOR);
                String percentText = completionPercentage + "%";
                int textWidth = g2d.getFontMetrics().stringWidth(percentText);
                int textHeight = g2d.getFontMetrics().getHeight();
                g2d.drawString(percentText, width / 2 - textWidth / 2, height / 2 + textHeight / 4);
                
                // Draw completed text
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2d.setColor(LIGHT_TEXT_COLOR);
                String completedText = "Completed";
                textWidth = g2d.getFontMetrics().stringWidth(completedText);
                g2d.drawString(completedText, width / 2 - textWidth / 2, height / 2 + textHeight + 10);
                
                g2d.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(200, 200);
            }
        };
        
        chartPanel.add(progressChart, BorderLayout.WEST);
        chartPanel.add(statusTable, BorderLayout.CENTER);
        
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(chartPanel, BorderLayout.CENTER);
        
        return containerPanel;
    }
    
    private JPanel createStatusRow(String status, int count, int percentage, Color statusColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        
        // Status label with colored dot
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        labelPanel.setBackground(CARD_COLOR);
        
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
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(TEXT_COLOR);
        
        labelPanel.add(colorDot);
        labelPanel.add(statusLabel);
        
        // Count and percentage
        JLabel countLabel = new JLabel(count + " (" + percentage + "%)");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(LIGHT_TEXT_COLOR);
        
        panel.add(labelPanel, BorderLayout.WEST);
        panel.add(countLabel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createCategoryDistributionPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(CARD_COLOR);
        containerPanel.setBorder(createCardBorder());
        
        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("Tasks by Category");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Create chart panel
        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        chartPanel.setBackground(CARD_COLOR);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // If no categories, show message
        if (taskCountsByCategory.isEmpty()) {
            JLabel emptyLabel = new JLabel("No categorized tasks found!");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(LIGHT_TEXT_COLOR);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            chartPanel.add(emptyLabel);
        } else {
            // Create bar chart visualization
            // Define category colors - we'll rotate through these
            Color[] categoryColors = {
                new Color(41, 98, 255),   // Primary blue
                new Color(0, 200, 83),    // Green
                new Color(255, 145, 0),   // Orange
                new Color(103, 58, 183),  // Purple
                new Color(211, 47, 47),   // Red
                new Color(0, 188, 212)    // Cyan
            };
            
            int colorIndex = 0;
            int totalTasks = 0;
            for (int count : taskCountsByCategory.values()) {
                totalTasks += count;
            }
            
            // Add each category as a horizontal bar
            for (Map.Entry<String, Integer> entry : taskCountsByCategory.entrySet()) {
                String category = entry.getKey();
                int count = entry.getValue();
                
                // Calculate percentage
                int percentage = totalTasks > 0 ? (count * 100) / totalTasks : 0;
                
                // Calculate progress percentage for this category
                int totalInCategory = totalTasksByCategory.getOrDefault(category, 0);
                int completedInCategory = completedTasksByCategory.getOrDefault(category, 0);
                int categoryProgress = totalInCategory > 0 ? (completedInCategory * 100) / totalInCategory : 0;
                
                // Create bar panel
                JPanel barPanel = new JPanel(new BorderLayout(10, 0));
                barPanel.setBackground(CARD_COLOR);
                barPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                
                // Category label
                JLabel categoryLabel = new JLabel(category);
                categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                categoryLabel.setForeground(TEXT_COLOR);
                categoryLabel.setPreferredSize(new Dimension(120, 20));
                
                // Stats panel for count and progress
                JPanel statsPanel = new JPanel(new GridLayout(2, 1));
                statsPanel.setBackground(CARD_COLOR);
                
                // Count label
                JLabel countLabel = new JLabel(count + " (" + percentage + "% of all tasks)");
                countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                countLabel.setForeground(LIGHT_TEXT_COLOR);
                
                // Progress label
                JLabel progressLabel = new JLabel("Progress: " + categoryProgress + "%");
                progressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                progressLabel.setForeground(completedInCategory > 0 ? SUCCESS_COLOR : LIGHT_TEXT_COLOR);
                
                statsPanel.add(countLabel);
                statsPanel.add(progressLabel);
                
                // Bar visualization
                Color barColor = categoryColors[colorIndex % categoryColors.length];
                JPanel barGraph = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        
                        int width = getWidth();
                        int height = getHeight();
                        
                        // Calculate bar width based on percentage
                        int barWidth = (width * percentage) / 100;
                        
                        // Draw background
                        g2d.setColor(new Color(244, 245, 247));
                        g2d.fillRoundRect(0, 0, width, height, 10, 10);
                        
                        // Draw bar
                        if (barWidth > 0) {
                            g2d.setColor(barColor);
                            g2d.fillRoundRect(0, 0, barWidth, height, 10, 10);
                        }
                        
                        // Draw progress overlay
                        if (categoryProgress > 0) {
                            int progressWidth = Math.min((width * categoryProgress) / 100, barWidth);
                            g2d.setColor(new Color(0, 200, 83, 100)); // Translucent green
                            g2d.fillRoundRect(0, 0, progressWidth, height, 10, 10);
                        }
                        
                        g2d.dispose();
                    }
                    
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(200, 30);
                    }
                };
                
                barPanel.add(categoryLabel, BorderLayout.WEST);
                barPanel.add(barGraph, BorderLayout.CENTER);
                barPanel.add(statsPanel, BorderLayout.EAST);
                
                chartPanel.add(barPanel);
                chartPanel.add(Box.createVerticalStrut(15));
                
                colorIndex++;
            }
        }
        
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(chartPanel, BorderLayout.CENTER);
        
        return containerPanel;
    }

    private void styleButton(JButton button, boolean isPrimary) {
        if (isPrimary) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(TEXT_COLOR);
            button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }
        
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(ACCENT_COLOR);
                } else {
                    button.setBackground(new Color(245, 245, 245));
                }
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(PRIMARY_COLOR);
                } else {
                    button.setBackground(Color.WHITE);
                }
            }
        });
    }
}