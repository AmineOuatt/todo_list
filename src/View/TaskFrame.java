package View;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import Controller.CategoryController;
import Controller.TaskController;
import Controller.SubTaskController;
import Model.Category;
import Model.Task;
import Model.Note;
import Model.SubTask;
import Controller.NoteController;

public class TaskFrame extends JFrame {
    private int userId;
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private JButton addButton, deleteButton;
    private JTextField titleField;
    private JTextArea descriptionField;
    private JComboBox<String> statusComboBox;
    private JComboBox<String> priorityComboBox;  // Ajout du champ priorité
    private JSpinner dueDateSpinner;
    private JComboBox<Category> categoryComboBox;
    private JTextField newCategoryField;
    private JButton addCategoryButton;
    
    // Pomodoro components
    private Timer pomodoroTimer;
    private JLabel timerLabel;
    private JButton startPomodoroButton;
    private JButton resetPomodoroButton;
    private int remainingSeconds;
    private boolean isBreak = false;
    private int workDuration = 25 * 60;  // 25 minutes in seconds
    private int shortBreakDuration = 5 * 60;  // 5 minutes in seconds
    private int longBreakDuration = 15 * 60;  // 15 minutes in seconds
    private boolean autoStartBreaks = false;
    private boolean autoStartPomodoros = false;
    
    // Modern Microsoft-style colors
    private static final Color PRIMARY_COLOR = new Color(77, 100, 255);    // TickTick Blue
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255); // White
    private static final Color SIDEBAR_COLOR = new Color(247, 248, 250);   // Light Gray
    private static final Color TEXT_COLOR = new Color(37, 38, 43);         // Dark Gray
    private static final Color BORDER_COLOR = new Color(233, 234, 236);    // Light Border
    private static final Color HOVER_COLOR = new Color(242, 243, 245);     // Light Hover
    private static final Color ACCENT_COLOR = new Color(77, 100, 255);     // TickTick Accent
    private static final Color COMPLETED_COLOR = new Color(52, 199, 89);   // Green
    private static final Color PENDING_COLOR = new Color(255, 149, 0);     // Orange
    private static final Color CARD_COLOR = Color.WHITE;
    
    // Define consistent task status colors
    private static final Color IN_PROGRESS_COLOR = PENDING_COLOR;         // Orange for "In Progress"
    private static final Color PENDING_STATUS_COLOR = new Color(149, 149, 149); // Gray for "Pending"
    
    // Couleurs pour les niveaux de priorité
    private static final Color URGENT_PRIORITY_COLOR = new Color(255, 59, 48);  // Rouge vif
    private static final Color HIGH_PRIORITY_COLOR = new Color(255, 149, 0);    // Orange
    private static final Color NORMAL_PRIORITY_COLOR = new Color(0, 122, 255);  // Bleu
    private static final Color LOW_PRIORITY_COLOR = new Color(142, 142, 147);   // Gris

    // Custom calendar component
    private JPanel calendarPanel;
    private Calendar currentCalendar;
    private JLabel monthLabel;
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy");

    private CardLayout sidebarContentLayout;
    private JPanel sidebarContent;
    private JButton pomodoroButton;
    private JButton calendarButton;
    private JButton dashboardButton;
    private JPanel pomodoroPanel;
    private JPanel dashboardPanel;

    private JPanel mainContentPanel;
    private boolean isTimerRunning = false;
    private CircularProgressPanel progressPanel;

    private JPanel navigationPanel;

    private Color currentPomodoroColor = PRIMARY_COLOR;
    private int pomodoroCount = 0;
    private PomodoroMode currentMode = PomodoroMode.POMODORO;

    private enum PomodoroMode {
        POMODORO("Pomodoro", PRIMARY_COLOR),
        SHORT_BREAK("Short Break", ACCENT_COLOR),
        LONG_BREAK("Long Break", PRIMARY_COLOR);

        final String label;
        final Color color;

        PomodoroMode(String label, Color color) {
            this.label = label;
            this.color = color;
        }
    }

    // Inner class for circular progress
    private class CircularProgressPanel extends JPanel {
        private double progress = 0.0;

        public void setProgress(double progress) {
            this.progress = progress;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 40;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            // Draw background circle
            g2.setColor(new Color(240, 240, 240));
            g2.setStroke(new BasicStroke(10));
            g2.drawArc(x, y, size, size, 0, 360);

            // Draw progress
            g2.setColor(currentPomodoroColor);
            g2.drawArc(x, y, size, size, 90, -(int) (progress * 360));
        }
    }

    // Custom cell renderer for tasks
    private class TaskListCellRenderer extends DefaultListCellRenderer {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            if (value instanceof Task) {
                Task task = (Task) value;
                
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                
                // Déterminer une bordure basée sur la priorité
                Color borderColor = BORDER_COLOR;
                int borderThickness = 1;
                
                if (task.getPriority() != null) {
                    if ("URGENT".equals(task.getPriority())) {
                        borderColor = URGENT_PRIORITY_COLOR;
                        borderThickness = 2;
                    } else if ("HIGH".equals(task.getPriority())) {
                        borderColor = HIGH_PRIORITY_COLOR;
                        borderThickness = 2;
                    }
                }
                
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, borderThickness, 1, 0, borderColor),
                    BorderFactory.createEmptyBorder(15, 10, 15, 10)
                ));
                
                if (isSelected) {
                    panel.setBackground(HOVER_COLOR);
                } else {
                    panel.setBackground(SIDEBAR_COLOR);
                }
                
                // Title panel without checkbox
                JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
                titlePanel.setBackground(panel.getBackground());
                titlePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Status indicator icon
                JLabel statusIcon = new JLabel(createStatusIcon(task.getStatus()));
                titlePanel.add(statusIcon, BorderLayout.WEST);
                
                // Title with priority indicator if needed
                JPanel titleWithPriorityPanel = new JPanel(new BorderLayout(5, 0));
                titleWithPriorityPanel.setBackground(titlePanel.getBackground());
                
                // Title
                JLabel titleLabel = new JLabel(task.getTitle());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if (isSelected) {
                    titleLabel.setForeground(PRIMARY_COLOR);
                } else if ("Completed".equals(task.getStatus())) {
                    titleLabel.setForeground(Color.GRAY);
                    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
                    // Add strikethrough for completed tasks
                    titleLabel.setText("<html><strike>" + task.getTitle() + "</strike></html>");
                } else {
                    // Colorer le titre selon la priorité
                    if (task.getPriority() != null) {
                        if ("URGENT".equals(task.getPriority())) {
                            titleLabel.setForeground(URGENT_PRIORITY_COLOR);
                        } else if ("HIGH".equals(task.getPriority())) {
                            titleLabel.setForeground(HIGH_PRIORITY_COLOR);
                        } else {
                            titleLabel.setForeground(TEXT_COLOR);
                        }
                    } else {
                        titleLabel.setForeground(TEXT_COLOR);
                    }
                }
                titleWithPriorityPanel.add(titleLabel, BorderLayout.CENTER);
                
                // Ajouter l'indicateur de priorité
                JLabel priorityIcon = new JLabel(createPriorityIcon(task.getPriority()));
                titleWithPriorityPanel.add(priorityIcon, BorderLayout.EAST);
                
                titlePanel.add(titleWithPriorityPanel, BorderLayout.CENTER);
                panel.add(titlePanel);
                
                // Add some space
                panel.add(Box.createVerticalStrut(5));
                
                // Preview of description (first 50 chars)
                String preview = task.getDescription();
                if (preview != null) {
                    if (preview.length() > 50) {
                        preview = preview.substring(0, 47) + "...";
                    }
                    JLabel previewLabel = new JLabel(preview);
                    previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    previewLabel.setForeground(new Color(100, 100, 100));
                    previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panel.add(previewLabel);
                }
                
                // Add some space
                panel.add(Box.createVerticalStrut(5));
                
                // Category, status and date
                JPanel metaPanel = new JPanel(new BorderLayout());
                metaPanel.setBackground(panel.getBackground());
                
                JPanel leftMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                leftMeta.setBackground(panel.getBackground());
                
                // Status indicator
                JLabel statusLabel = new JLabel(task.getStatus());
                statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                Color statusColor = TEXT_COLOR;
                if ("Completed".equals(task.getStatus())) {
                    statusColor = COMPLETED_COLOR;
                } else if ("In Progress".equals(task.getStatus())) {
                    statusColor = IN_PROGRESS_COLOR;
                } else if ("Pending".equals(task.getStatus())) {
                    statusColor = PENDING_STATUS_COLOR;
                }
                                statusLabel.setForeground(statusColor);                leftMeta.add(statusLabel);                                // Ajout de l'affichage textuel de la priorité                if (task.getPriority() != null) {                    JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));                    priorityPanel.setBackground(leftMeta.getBackground());                                        // Créer une icône de bulle pour la priorité                    JLabel bulletIcon = new JLabel(createBulletIcon());                    priorityPanel.add(bulletIcon);                                        JLabel priorityLabel = new JLabel(task.getPriority());                    priorityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));                                        // Définir la couleur en fonction de la priorité                    Color priorityColor = NORMAL_PRIORITY_COLOR; // Par défaut                    String priority = task.getPriority();                    if ("URGENT".equals(priority)) {                        priorityColor = URGENT_PRIORITY_COLOR;                    } else if ("HIGH".equals(priority)) {                        priorityColor = HIGH_PRIORITY_COLOR;                    } else if ("NORMAL".equals(priority)) {                        priorityColor = NORMAL_PRIORITY_COLOR;                    } else if ("LOW".equals(priority)) {                        priorityColor = LOW_PRIORITY_COLOR;                    }                                        priorityLabel.setForeground(priorityColor);                    priorityPanel.add(priorityLabel);                                        leftMeta.add(priorityPanel);                }
                
                // Category if available
                if (task.getCategory() != null) {
                    JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                    categoryPanel.setBackground(leftMeta.getBackground());
                    
                    // Create custom bullet icon
                    JLabel bulletIcon = new JLabel(createBulletIcon());
                    categoryPanel.add(bulletIcon);
                    
                    JLabel categoryLabel = new JLabel(task.getCategory().getName());
                    categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    categoryLabel.setForeground(PRIMARY_COLOR);
                    categoryPanel.add(categoryLabel);
                    
                    leftMeta.add(categoryPanel);
                }
                
                metaPanel.add(leftMeta, BorderLayout.WEST);
                
                // Date
                if (task.getDueDate() != null) {
                    String dateText = dateFormat.format(task.getDueDate());
                    JLabel dateLabel = new JLabel(dateText);
                    dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    dateLabel.setForeground(new Color(150, 150, 150));
                    metaPanel.add(dateLabel, BorderLayout.EAST);
                }
                
                metaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(metaPanel);
                
                return panel;
            }
            
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private DefaultListModel<Note> notesListModel;
    private JList<Note> notesList;
    private JTextField notesTitleField;
    private JTextArea notesContentArea;
    private JComboBox<Category> notesCategoryComboBox;
    private Note currentNote;

    // Inside the TaskFrame class, add a new field to keep track of subtasks
    private List<SubTask> currentSubTasks = new ArrayList<>();
    private JPanel subTasksPanel;

    // Recurring task components
    private JCheckBox isRecurringCheckBox;
    private JComboBox<String> recurrenceTypeComboBox;
    private JSpinner recurrenceIntervalSpinner;
    private JSpinner recurrenceEndDateSpinner;
    private JSpinner occurrencesSpinner;
    private JPanel recurrencePanel;
    private JComboBox<String> dayOfWeekComboBox;
    private JSpinner dayOfMonthSpinner;
    private JComboBox<String> monthOfYearComboBox;

    public TaskFrame(int userId) {
        this.userId = userId;
        
        setTitle("Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Ensure window opens in fullscreen mode
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Set minimum size for the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(screenSize.width, screenSize.height));
        setPreferredSize(screenSize);
        
        // Set location to center of screen
        setLocationRelativeTo(null);
        
        // Initialize the task list
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new TaskListCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    displayTaskDetails(selectedTask);
                }
            }
        });
        
        // Add mouse listener for double-click to toggle completion status
        taskList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = taskList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Task task = taskListModel.getElementAt(index);
                        // Toggle between Completed and Pending
                        String newStatus = "Completed".equals(task.getStatus()) ? "Pending" : "Completed";
                        
                        // Update in database
                        boolean success = TaskController.updateTask(
                            task.getTaskId(),
                            task.getTitle(),
                            task.getDescription(),
                            newStatus,
                            task.getDueDate(),
                            task.getCategory()
                        );
                        
                        if (success) {
                            // Refresh the list to show changes
                            loadTasks();
                            // Show feedback
                            JOptionPane.showMessageDialog(
                                TaskFrame.this,
                                "Task marked as " + newStatus,
                                "Status Updated",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                }
            }
        });
        
        // Create the main layout
        setLayout(new BorderLayout());
        
        // Create navigation sidebar
        navigationPanel = createNavigationPanel();
        add(navigationPanel, BorderLayout.WEST);
        
        // Create main content panel with card layout
        mainContentPanel = new JPanel(new CardLayout());
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        
        // Create task view with split pane (list on left, details on right)
        JSplitPane tasksSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        tasksSplitPane.setDividerLocation(300);
        tasksSplitPane.setDividerSize(1);
        tasksSplitPane.setBorder(null);
        tasksSplitPane.setBackground(BACKGROUND_COLOR);
        
        // Create task list panel (left side)
        JPanel taskListPanel = createTaskListPanel();
        
        // Create task details panel (right side)
        JPanel taskDetailsPanel = createTaskDetailsPanel();
        
        // Create card layout for task view to switch between list-only and details views
        JPanel taskViewPanel = new JPanel(new CardLayout());
        taskViewPanel.add(taskListPanel, "TASK_LIST");
        taskViewPanel.add(taskDetailsPanel, "TASK_DETAILS");
        
        // Create pomodoro panel
        pomodoroPanel = createPomodoroPanel();
        
        // Create dashboard panel
        dashboardPanel = createDashboardPanel();
        
        // Create calendar panel - store the reference first
        calendarPanel = createCalendarPanel();
        
        // Create notes panel with card layout to switch between list-only and details views
        JPanel notesPanel = new JPanel(new CardLayout());
        
        // Create notes list panel (left side)
        JPanel notesListPanel = createNotesListPanel();
        
        // Create note details panel (right side)
        JPanel noteDetailsPanel = createNoteDetailsPanel();
        
        notesPanel.add(notesListPanel, "NOTES_LIST");
        notesPanel.add(noteDetailsPanel, "NOTES_DETAILS");
        
        // Add panels to card layout
        mainContentPanel.add(taskViewPanel, "TASKS");
        mainContentPanel.add(pomodoroPanel, "POMODORO");
        mainContentPanel.add(dashboardPanel, "DASHBOARD");
        mainContentPanel.add(calendarPanel, "CALENDAR");
        mainContentPanel.add(notesPanel, "NOTES");
        
        // Add main content panel to frame
        add(mainContentPanel, BorderLayout.CENTER);
        
        // Load tasks
        loadTasks();
        
        // Load notes
        loadNotes();
        
        // Initialize timer for pomodoro
        initializePomodoroTimer();
        
        // Now that everything is set up, we can update the calendar
        updateCalendar();
        
        // Show tasks panel by default
        showPanel("TASKS");
        
        pack();
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Add logo/brand at the top
        JLabel logoLabel = new JLabel("TaskFrame");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(25));

        // Create navigation options with custom icons instead of Unicode
        String[] navLabels = {"Tasks", "Pomodoro", "Dashboard", "Calendar", "Notes"};
        String[] navActions = {"TASKS", "POMODORO", "DASHBOARD", "CALENDAR", "NOTES"};

        for (int i = 0; i < navLabels.length; i++) {
            JButton button = new JButton(navLabels[i]);
            
            // Set appropriate icon based on button type
            switch (i) {
                case 0: // Tasks
                    button.setIcon(createTaskIcon());
                    break;
                case 1: // Pomodoro
                    button.setIcon(createPomodoroIcon());
                    break;
                case 2: // Dashboard
                    button.setIcon(createDashboardIcon());
                    break;
                case 3: // Calendar
                    button.setIcon(createCalendarIcon());
                    break;
                case 4: // Notes
                    button.setIcon(createNoteIcon());
                    break;
            }
            
            styleNavigationButton(button);
            final String action = navActions[i];
            button.addActionListener(e -> showPanel(action));
            panel.add(button);
            panel.add(Box.createVerticalStrut(5));
            
            // Store references to these buttons for selection highlighting
            if (i == 0) {
                // Task button
            } else if (i == 1) {
                pomodoroButton = button;
            } else if (i == 2) {
                dashboardButton = button;
            } else if (i == 3) {
                calendarButton = button;
            } else if (i == 4) {
                // Notes button
            }
        }

        panel.add(Box.createVerticalGlue());

        // Add separator before logout
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(SIDEBAR_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(15));

        // Add logout button at the bottom with custom icon
        JButton logoutButton = new JButton();
        logoutButton.setLayout(new BorderLayout(10, 0));
        
        // Replace door icon panel implementation with custom-drawn icon
        logoutButton.setIcon(createLogoutIcon());
        
        // Create text label
        JLabel textLabel = new JLabel("Logout");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(new Color(255, 89, 89));
        
        logoutButton.add(textLabel, BorderLayout.CENTER);
        styleNavigationButton(logoutButton);
        
        // Override the default style for logout button
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        logoutButton.addActionListener(e -> logout());
        panel.add(logoutButton);

        return panel;
    }

    private void styleNavigationButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(SIDEBAR_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
            }
        });
    }

    private void updateSelectedButton(JButton selectedButton) {
        Component[] components = navigationPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (button == selectedButton) {
                    button.setBackground(PRIMARY_COLOR);
                    button.setForeground(Color.WHITE);
                } else {
                    button.setBackground(Color.WHITE);
                    button.setForeground(PRIMARY_COLOR);
                }
            }
        }
    }

    private void updateDashboard() {
        // Get tasks statistics
        List<Task> tasks = TaskController.getTasks(userId);
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int inProgressTasks = 0;
        int pendingTasks = 0;

        for (Task task : tasks) {
            switch (task.getStatus().toLowerCase()) {
                case "completed":
                    completedTasks++;
                    break;
                case "in progress":
                    inProgressTasks++;
                    break;
                default:
                    pendingTasks++;
            }
        }

        // Update the chart with real data
        int[] data = {completedTasks, inProgressTasks, pendingTasks};
        updateChartData(data);
    }

    private void updateChartData(int[] data) {
        if (dashboardPanel != null) {
            Component[] components = dashboardPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel && comp.getName() != null && comp.getName().equals("chartPanel")) {
                    ((ChartPanel) comp).updateData(data);
                    comp.repaint();
                    break;
                }
            }
        }
    }

    // Custom chart panel class
    private class ChartPanel extends JPanel {
        private int[] data;
        private String[] labels = {"Completed", "In Progress", "Pending"};
        private Color[] colors = {COMPLETED_COLOR, IN_PROGRESS_COLOR, PENDING_STATUS_COLOR};

        public ChartPanel(int[] initialData) {
            this.data = initialData;
            setName("chartPanel");
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        }

        public void updateData(int[] newData) {
            this.data = newData;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = 0;
            for (int value : data) {
                total += value;
            }

            if (total == 0) return;

            int width = getWidth() - 40;
            int height = getHeight() - 60;
            int x = 20;
            int y = 20;
            int barHeight = height / data.length;

            // Draw bars and labels
            for (int i = 0; i < data.length; i++) {
                int barWidth = (int) ((data[i] / (double) total) * width);
                int barY = y + (i * barHeight);

                // Draw bar
                g2.setColor(colors[i]);
                g2.fillRect(x, barY, barWidth, barHeight - 10);

                // Draw label and value
                g2.setColor(TEXT_COLOR);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2.drawString(labels[i], x, barY - 5);
                g2.drawString(String.valueOf(data[i]), x + barWidth + 5, barY + barHeight/2);
            }
        }
    }

    private JPanel createPomodoroPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(currentPomodoroColor);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Mode selection panel with settings button
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        modePanel.setOpaque(false);

        // Add mode buttons
        for (PomodoroMode mode : PomodoroMode.values()) {
            JButton modeButton = createModeButton(mode.label, mode);
            modePanel.add(modeButton);
        }

        // Add settings button
        JButton settingsButton = new JButton("Settings ⚙");
        settingsButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setBorderPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setOpaque(true);
        settingsButton.setBackground(new Color(255, 255, 255, 30));
        settingsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        settingsButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        // Create settings popup
        JPopupMenu settingsPopup = createSettingsPopup();
        
        settingsButton.addActionListener(e -> {
            settingsPopup.show(settingsButton, 
                             settingsButton.getWidth() - settingsPopup.getPreferredSize().width, 
                             settingsButton.getHeight());
        });
        
        settingsButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                settingsButton.setBackground(new Color(255, 255, 255, 50));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                settingsButton.setBackground(new Color(255, 255, 255, 30));
            }
        });

        modePanel.add(Box.createHorizontalStrut(10));
        modePanel.add(settingsButton);

        // Timer panel
        JPanel timerPanel = new JPanel(new BorderLayout(10, 10));
        timerPanel.setOpaque(false);

        // Mode label
        JLabel modeLabel = new JLabel(currentMode.label, SwingConstants.CENTER);
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        modeLabel.setForeground(Color.WHITE);
        timerPanel.add(modeLabel, BorderLayout.NORTH);

        // Timer display
        JPanel timeDisplayPanel = new JPanel(new BorderLayout());
        timeDisplayPanel.setOpaque(false);
        timeDisplayPanel.setBorder(new EmptyBorder(40, 0, 40, 0));

        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 120));
        timerLabel.setForeground(Color.WHITE);
        timeDisplayPanel.add(timerLabel, BorderLayout.CENTER);

        // Control buttons
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        controlPanel.setOpaque(false);

        startPomodoroButton = new JButton("START");
        resetPomodoroButton = new JButton("RESET");
        
        startPomodoroButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        resetPomodoroButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        stylePomodoroButton(startPomodoroButton, true);
        stylePomodoroButton(resetPomodoroButton, false);

        startPomodoroButton.addActionListener(e -> togglePomodoro());
        resetPomodoroButton.addActionListener(e -> resetPomodoro());

        controlPanel.add(startPomodoroButton);
        controlPanel.add(resetPomodoroButton);

        timerPanel.add(timeDisplayPanel, BorderLayout.CENTER);
        timerPanel.add(controlPanel, BorderLayout.SOUTH);

        // Add components to main panel
        panel.add(modePanel, BorderLayout.NORTH);
        panel.add(timerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPopupMenu createSettingsPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        popup.setBackground(currentPomodoroColor);

        // Create a panel for settings
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBackground(currentPomodoroColor);
        settingsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Work duration settings
        JPanel workDurationPanel = createSettingsRow("Work Duration (minutes):", workDuration);
        JSpinner workSpinner = (JSpinner) workDurationPanel.getComponent(1);
        workSpinner.addChangeListener(e -> {
            workDuration = (Integer) workSpinner.getValue() * 60;
            if (currentMode == PomodoroMode.POMODORO) {
                remainingSeconds = workDuration;
                updateTimerLabel();
            }
        });

        // Short break settings
        JPanel shortBreakPanel = createSettingsRow("Short Break (minutes):", shortBreakDuration);
        JSpinner shortBreakSpinner = (JSpinner) shortBreakPanel.getComponent(1);
        shortBreakSpinner.addChangeListener(e -> {
            shortBreakDuration = (Integer) shortBreakSpinner.getValue() * 60;
            if (currentMode == PomodoroMode.SHORT_BREAK) {
                remainingSeconds = shortBreakDuration;
                updateTimerLabel();
            }
        });

        // Long break settings
        JPanel longBreakPanel = createSettingsRow("Long Break (minutes):", longBreakDuration);
        JSpinner longBreakSpinner = (JSpinner) longBreakPanel.getComponent(1);
        longBreakSpinner.addChangeListener(e -> {
            longBreakDuration = (Integer) longBreakSpinner.getValue() * 60;
            if (currentMode == PomodoroMode.LONG_BREAK) {
                remainingSeconds = longBreakDuration;
                updateTimerLabel();
            }
        });

        // Auto start breaks
        JPanel autoStartPanel = createCheckboxRow("Auto-start breaks");
        JCheckBox autoStartCheckbox = (JCheckBox) autoStartPanel.getComponent(0);
        autoStartCheckbox.setSelected(autoStartBreaks);
        autoStartCheckbox.addActionListener(e -> {
            autoStartBreaks = autoStartCheckbox.isSelected();
        });

        // Auto start pomodoros
        JPanel autoStartPomodoroPanel = createCheckboxRow("Auto-start pomodoros");
        JCheckBox autoStartPomodoroCheckbox = (JCheckBox) autoStartPomodoroPanel.getComponent(0);
        autoStartPomodoroCheckbox.setSelected(autoStartPomodoros);
        autoStartPomodoroCheckbox.addActionListener(e -> {
            autoStartPomodoros = autoStartPomodoroCheckbox.isSelected();
        });

        // Add components with spacing
        settingsPanel.add(workDurationPanel);
        settingsPanel.add(Box.createVerticalStrut(10));
        settingsPanel.add(shortBreakPanel);
        settingsPanel.add(Box.createVerticalStrut(10));
        settingsPanel.add(longBreakPanel);
        settingsPanel.add(Box.createVerticalStrut(15));
        settingsPanel.add(autoStartPanel);
        settingsPanel.add(Box.createVerticalStrut(5));
        settingsPanel.add(autoStartPomodoroPanel);

        popup.add(settingsPanel);
        return popup;
    }

    private JButton createModeButton(String text, PomodoroMode mode) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(mode == currentMode ? new Color(255, 255, 255, 50) : new Color(0, 0, 0, 0));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        button.addActionListener(e -> switchMode(mode));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (mode != currentMode) {
                    button.setBackground(new Color(255, 255, 255, 30));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (mode != currentMode) {
                    button.setBackground(new Color(0, 0, 0, 0));
                }
            }
        });
        
        return button;
    }

    private void stylePomodoroButton(JButton button, boolean isPrimary) {
        button.setForeground(currentPomodoroColor);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(12, 30, 12, 30)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
    }

    private void switchMode(PomodoroMode newMode) {
        if (currentMode != newMode) {
            currentMode = newMode;
            currentPomodoroColor = newMode.color;
            
            // Set duration based on mode
            switch (newMode) {
                case POMODORO:
                    remainingSeconds = workDuration;
                    break;
                case SHORT_BREAK:
                    remainingSeconds = shortBreakDuration;
                    break;
                case LONG_BREAK:
                    remainingSeconds = longBreakDuration;
                    break;
            }
            
            resetPomodoro();
            updatePomodoroUI();
        }
    }

    private void updatePomodoroUI() {
        // Find and update the Pomodoro panel
        for (Component comp : mainContentPanel.getComponents()) {
            if (comp.isVisible() && comp instanceof JPanel) {
                updatePanelColors((JPanel) comp);
            }
        }
        updateTimerLabel();
    }

    private void updatePanelColors(JPanel panel) {
        if (panel.getLayout() instanceof BorderLayout) {
            panel.setBackground(currentPomodoroColor);
            for (Component comp : panel.getComponents()) {
                if (comp instanceof JPanel) {
                    updatePanelColors((JPanel) comp);
                } else if (comp instanceof JButton) {
                    JButton button = (JButton) comp;
                    if (button == startPomodoroButton || button == resetPomodoroButton) {
                        button.setForeground(currentPomodoroColor);
                    }
                }
            }
        }
    }

    private void initializePomodoroTimer() {
        remainingSeconds = workDuration;
        pomodoroTimer = new Timer(1000, e -> {
            if (remainingSeconds > 0) {
                remainingSeconds--;
                updateTimerLabel();
            } else {
                pomodoroTimer.stop();
                handlePomodoroComplete();
            }
        });
    }

    private void handlePomodoroComplete() {
        Toolkit.getDefaultToolkit().beep();
        
        if (currentMode == PomodoroMode.POMODORO) {
            pomodoroCount++;
            String message = "Time for a break!";
            JOptionPane.showMessageDialog(this, message, "Timer Complete", JOptionPane.INFORMATION_MESSAGE);
            
            if (pomodoroCount % 4 == 0) {
                if (autoStartBreaks) {
                    switchMode(PomodoroMode.LONG_BREAK);
                    togglePomodoro();
                } else {
                    switchMode(PomodoroMode.LONG_BREAK);
                }
            } else {
                if (autoStartBreaks) {
                    switchMode(PomodoroMode.SHORT_BREAK);
                    togglePomodoro();
                } else {
                    switchMode(PomodoroMode.SHORT_BREAK);
                }
            }
        } else {
            String message = "Break is over, back to work!";
            JOptionPane.showMessageDialog(this, message, "Timer Complete", JOptionPane.INFORMATION_MESSAGE);
            
            if (autoStartPomodoros) {
                switchMode(PomodoroMode.POMODORO);
                togglePomodoro();
            } else {
                switchMode(PomodoroMode.POMODORO);
            }
        }
    }

    private void togglePomodoro() {
        if (pomodoroTimer.isRunning()) {
            pomodoroTimer.stop();
            startPomodoroButton.setText("START");
        } else {
            pomodoroTimer.start();
            startPomodoroButton.setText("PAUSE");
        }
    }

    private void resetPomodoro() {
        pomodoroTimer.stop();
        remainingSeconds = workDuration;
        startPomodoroButton.setText("START");
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Left section with title
        JLabel titleLabel = new JLabel("Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);

        // Right section with user profile and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(BACKGROUND_COLOR);

        // User profile button
        JButton profileButton = new JButton();
        profileButton.setLayout(new BorderLayout());
        
        // Create circular avatar
        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillOval(0, 0, getWidth(), getHeight());
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initial = "U"; // You can set this based on the user's name
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initial)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(initial, x, y);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(32, 32);
            }
        };
        avatarPanel.setOpaque(false);
        
        profileButton.add(avatarPanel, BorderLayout.CENTER);
        profileButton.setPreferredSize(new Dimension(32, 32));
        profileButton.setBorderPainted(false);
        profileButton.setContentAreaFilled(false);
        profileButton.setFocusPainted(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Create popup menu for profile button
        JPopupMenu profileMenu = new JPopupMenu();
        profileMenu.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        // Add menu items
        JMenuItem settingsItem = new JMenuItem("Settings");
        JMenuItem logoutItem = new JMenuItem("Logout");
        
        // Style menu items
        styleMenuItem(settingsItem);
        styleMenuItem(logoutItem);
        
        // Add action listener for logout
        logoutItem.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
            }
        });
        
        profileMenu.add(settingsItem);
        profileMenu.addSeparator();
        profileMenu.add(logoutItem);
        
        // Show popup menu on profile button click
        profileButton.addActionListener(e -> {
            profileMenu.show(profileButton, 0, profileButton.getHeight());
        });

        rightPanel.add(profileButton);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private void styleMenuItem(JMenuItem item) {
        item.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        item.setBackground(BACKGROUND_COLOR);
        item.setForeground(TEXT_COLOR);
        item.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        item.addChangeListener(e -> {
            if (item.isArmed()) {
                item.setBackground(HOVER_COLOR);
            } else {
                item.setBackground(BACKGROUND_COLOR);
            }
        });
    }

    private JPanel createTaskDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Task detail header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);
        
        JButton backButton = new JButton("← Back to Tasks");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setForeground(PRIMARY_COLOR);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            showTaskListView();
        });
        
        JLabel headerLabel = new JLabel("Task Details");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(TEXT_COLOR);
        
        titlePanel.add(backButton, BorderLayout.WEST);
        titlePanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Add panel to contain buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        
        JButton saveButton = new JButton("Save Task");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.setBackground(PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        saveButton.addActionListener(e -> {
            Task selectedTask = taskList.getSelectedValue();
            if (selectedTask == null) {
                handleAddTask();
            } else {
                handleUpdateTask();
            }
        });
        
        deleteButton = new JButton("Delete Task");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteButton.setFocusPainted(false);
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        deleteButton.addActionListener(e -> handleDeleteTask());
        deleteButton.setEnabled(false); // Only enable when a task is selected
        
        JButton clearButton = new JButton("Clear Form");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearButton.setFocusPainted(false);
        clearButton.setBackground(HOVER_COLOR);
        clearButton.setForeground(TEXT_COLOR);
        clearButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        clearButton.addActionListener(e -> clearFields());
        
        buttonsPanel.add(clearButton);
        buttonsPanel.add(saveButton);
        buttonsPanel.add(deleteButton);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);
        
        // Create a split panel for form and description/subtasks
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitPane.setBorder(null);
        horizontalSplitPane.setDividerSize(5);
        horizontalSplitPane.setResizeWeight(0.4); // Let the right panel get more space when resizing
        horizontalSplitPane.setBackground(BACKGROUND_COLOR);
        
        // Set initial divider location to 40% for form fields, 60% for description and subtasks
        horizontalSplitPane.setDividerLocation(0.4);
        
        // Left panel for form fields - MODIFIÉ: Ajouter un JScrollPane pour permettre le défilement
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Création d'un JScrollPane pour le panneau de formulaire
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.setBackground(BACKGROUND_COLOR);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 10, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Title field
        JLabel titleLabel = new JLabel("Title");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(titleLabel, gbc);
        
        gbc.gridy++;
        titleField = new JTextField();
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(titleField, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 10, 0);
        
        // Status selection
        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(statusLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        String[] statuses = {"Pending", "In Progress", "Completed"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Add a custom renderer for the status dropdown
        statusComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout(10, 0));
                
                if (isSelected) {
                    panel.setBackground(HOVER_COLOR);
                } else {
                    panel.setBackground(list.getBackground());
                }
                
                String status = (String) value;
                JLabel statusIcon = new JLabel(createStatusIcon(status));
                panel.add(statusIcon, BorderLayout.WEST);
                
                JLabel textLabel = new JLabel(status);
                textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                // Set color based on status
                if ("Completed".equals(status)) {
                    textLabel.setForeground(COMPLETED_COLOR);
                } else if ("In Progress".equals(status)) {
                    textLabel.setForeground(IN_PROGRESS_COLOR);
                } else if ("Pending".equals(status)) {
                    textLabel.setForeground(PENDING_STATUS_COLOR);
                }
                
                panel.add(textLabel, BorderLayout.CENTER);
                
                return panel;
            }
        });
        
        formPanel.add(statusComboBox, gbc);
        
        // Ajout du champ de priorité
        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 10, 0);
        JLabel priorityLabel = new JLabel("Priority");
        priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(priorityLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        String[] priorities = {"NORMAL", "URGENT", "HIGH", "LOW"};
        priorityComboBox = new JComboBox<>(priorities);
        priorityComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        priorityComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Add a custom renderer for the priority dropdown
        priorityComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                    boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout(10, 0));
                
                if (isSelected) {
                    panel.setBackground(HOVER_COLOR);
                } else {
                    panel.setBackground(list.getBackground());
                }
                
                String priority = (String) value;
                JLabel priorityIcon = new JLabel(createPriorityIcon(priority));
                panel.add(priorityIcon, BorderLayout.WEST);
                
                JLabel textLabel = new JLabel(priority);
                textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                
                // Set color based on priority
                Color textColor = NORMAL_PRIORITY_COLOR;
                if ("URGENT".equals(priority)) {
                    textColor = URGENT_PRIORITY_COLOR;
                } else if ("HIGH".equals(priority)) {
                    textColor = HIGH_PRIORITY_COLOR;
                } else if ("LOW".equals(priority)) {
                    textColor = LOW_PRIORITY_COLOR;
                }
                textLabel.setForeground(textColor);
                
                panel.add(textLabel, BorderLayout.CENTER);
                
                return panel;
            }
        });
        
        formPanel.add(priorityComboBox, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 10, 0);
        
        // Due Date field with time
        JLabel dueDateLabel = new JLabel("Due Date & Time");
        dueDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(dueDateLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dueDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "dd/MM/yyyy HH:mm");
        dueDateSpinner.setEditor(dateEditor);
        dueDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dueDateSpinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(dueDateSpinner, gbc);
        
        // Recurring task section
        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 10, 0);
        
        JLabel recurringLabel = new JLabel("Recurring Task");
        recurringLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(recurringLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        
        isRecurringCheckBox = new JCheckBox("Make this task recurring");
        isRecurringCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        isRecurringCheckBox.setBackground(BACKGROUND_COLOR);
        isRecurringCheckBox.addActionListener(e -> toggleRecurrenceControls());
        formPanel.add(isRecurringCheckBox, gbc);
        
        // Recurrence panel that will be toggled
        recurrencePanel = new JPanel();
        recurrencePanel.setLayout(new GridBagLayout());
        recurrencePanel.setBackground(BACKGROUND_COLOR);
        recurrencePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 0, 10, 0)
        ));
        
        GridBagConstraints gbcRecur = new GridBagConstraints();
        gbcRecur.fill = GridBagConstraints.HORIZONTAL;
        gbcRecur.insets = new Insets(5, 0, 10, 0);
        gbcRecur.weightx = 1.0;
        gbcRecur.gridx = 0;
        gbcRecur.gridy = 0;
        
        // Recurrence type
        JLabel typeLabel = new JLabel("Recurrence Pattern");
        typeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrencePanel.add(typeLabel, gbcRecur);
        
        gbcRecur.gridy++;
        String[] recurrenceTypes = {"Daily", "Weekly", "Monthly", "Yearly"};
        recurrenceTypeComboBox = new JComboBox<>(recurrenceTypes);
        recurrenceTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrenceTypeComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        recurrencePanel.add(recurrenceTypeComboBox, gbcRecur);
        
        // Ajouter les sélecteurs spécifiques pour chaque type de récurrence
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(15, 0, 5, 0);
        JLabel specificLabel = new JLabel("Specific Settings");
        specificLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrencePanel.add(specificLabel, gbcRecur);
        
        // Jour de la semaine (pour Weekly)
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(5, 0, 5, 0);
        JPanel weeklyPanel = new JPanel(new BorderLayout(5, 0));
        weeklyPanel.setBackground(BACKGROUND_COLOR);
        JLabel dayOfWeekLabel = new JLabel("Day of Week:");
        dayOfWeekLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        weeklyPanel.add(dayOfWeekLabel, BorderLayout.WEST);
        
        String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        dayOfWeekComboBox = new JComboBox<>(daysOfWeek);
        dayOfWeekComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        weeklyPanel.add(dayOfWeekComboBox, BorderLayout.CENTER);
        recurrencePanel.add(weeklyPanel, gbcRecur);
        
        // Jour du mois (pour Monthly)
        gbcRecur.gridy++;
        JPanel monthlyPanel = new JPanel(new BorderLayout(5, 0));
        monthlyPanel.setBackground(BACKGROUND_COLOR);
        JLabel dayOfMonthLabel = new JLabel("Day of Month:");
        dayOfMonthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthlyPanel.add(dayOfMonthLabel, BorderLayout.WEST);
        
        SpinnerNumberModel dayOfMonthModel = new SpinnerNumberModel(1, 1, 31, 1);
        dayOfMonthSpinner = new JSpinner(dayOfMonthModel);
        dayOfMonthSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthlyPanel.add(dayOfMonthSpinner, BorderLayout.CENTER);
        recurrencePanel.add(monthlyPanel, gbcRecur);
        
        // Mois de l'année (pour Yearly)
        gbcRecur.gridy++;
        JPanel yearlyPanel = new JPanel(new BorderLayout(5, 0));
        yearlyPanel.setBackground(BACKGROUND_COLOR);
        JLabel monthOfYearLabel = new JLabel("Month of Year:");
        monthOfYearLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearlyPanel.add(monthOfYearLabel, BorderLayout.WEST);
        
        String[] monthsOfYear = {"January", "February", "March", "April", "May", "June", 
                                 "July", "August", "September", "October", "November", "December"};
        monthOfYearComboBox = new JComboBox<>(monthsOfYear);
        monthOfYearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearlyPanel.add(monthOfYearComboBox, BorderLayout.CENTER);
        recurrencePanel.add(yearlyPanel, gbcRecur);
        
        // Montrer/cacher les options en fonction du type de récurrence
        recurrenceTypeComboBox.addActionListener(e -> {
            String selectedType = (String) recurrenceTypeComboBox.getSelectedItem();
            weeklyPanel.setVisible("Weekly".equals(selectedType));
            monthlyPanel.setVisible("Monthly".equals(selectedType));
            yearlyPanel.setVisible("Yearly".equals(selectedType));
        });
        
        // Recurrence interval
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(15, 0, 5, 0);
        JLabel intervalLabel = new JLabel("Repeat Every");
        intervalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrencePanel.add(intervalLabel, gbcRecur);
        
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(0, 0, 15, 0);
        
        JPanel intervalPanel = new JPanel(new BorderLayout(10, 0));
        intervalPanel.setBackground(BACKGROUND_COLOR);
        
        SpinnerNumberModel intervalModel = new SpinnerNumberModel(1, 1, 100, 1);
        recurrenceIntervalSpinner = new JSpinner(intervalModel);
        recurrenceIntervalSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrenceIntervalSpinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JLabel unitLabel = new JLabel("day(s)");
        unitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Update unit label when recurrence type changes
        recurrenceTypeComboBox.addActionListener(e -> {
            String selectedType = (String) recurrenceTypeComboBox.getSelectedItem();
            if (selectedType.equals("Daily")) {
                unitLabel.setText("day(s)");
            } else if (selectedType.equals("Weekly")) {
                unitLabel.setText("week(s)");
            } else if (selectedType.equals("Monthly")) {
                unitLabel.setText("month(s)");
            } else if (selectedType.equals("Yearly")) {
                unitLabel.setText("year(s)");
            }
        });
        
        intervalPanel.add(recurrenceIntervalSpinner, BorderLayout.CENTER);
        intervalPanel.add(unitLabel, BorderLayout.EAST);
        
        recurrencePanel.add(intervalPanel, gbcRecur);
        
        // Ajouter un nouveau champ pour le nombre d'occurrences
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(15, 0, 5, 0);
        JLabel occurrencesLabel = new JLabel("Number of Occurrences");
        occurrencesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrencePanel.add(occurrencesLabel, gbcRecur);
        
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(0, 0, 15, 0);
        SpinnerNumberModel occurrencesModel = new SpinnerNumberModel(5, 1, 100, 1);
        occurrencesSpinner = new JSpinner(occurrencesModel);
        occurrencesSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        occurrencesSpinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        recurrencePanel.add(occurrencesSpinner, gbcRecur);
        
        // End date
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(5, 0, 5, 0);
        JLabel endDateLabel = new JLabel("End Date (Optional)");
        endDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrencePanel.add(endDateLabel, gbcRecur);
        
        gbcRecur.gridy++;
        gbcRecur.insets = new Insets(0, 0, 5, 0);
        
        SpinnerDateModel endDateModel = new SpinnerDateModel();
        recurrenceEndDateSpinner = new JSpinner(endDateModel);
        JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(recurrenceEndDateSpinner, "dd/MM/yyyy");
        recurrenceEndDateSpinner.setEditor(endDateEditor);
        recurrenceEndDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recurrenceEndDateSpinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Set default end date to 3 months from now
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.MONTH, 3);
        recurrenceEndDateSpinner.setValue(endCal.getTime());
        
        recurrencePanel.add(recurrenceEndDateSpinner, gbcRecur);
        
        // Add the recurrence panel to the form
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        formPanel.add(recurrencePanel, gbc);
        
        // Initially hide recurrence controls
        recurrencePanel.setVisible(false);
        
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 10, 0);
        
        // Category selection
        JLabel categoryLabel = new JLabel("Category");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(categoryLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        categoryComboBox = new JComboBox<>();
        
        // Add "No Category" option
        categoryComboBox.addItem(new Category(0, "No Category"));
        
        // Load categories
        loadCategories();
        
        categoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        categoryComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(categoryComboBox, gbc);
        
        // New Category section
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 10, 0);
        
        JPanel newCategoryPanel = new JPanel(new BorderLayout(10, 0));
        newCategoryPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel newCategoryLabel = new JLabel("New Category");
        newCategoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        gbc.gridy++;
        formPanel.add(newCategoryLabel, gbc);
        
        gbc.gridy++;
        
        newCategoryField = new JTextField();
        newCategoryField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newCategoryField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JPanel newCategoryFieldPanel = new JPanel(new BorderLayout(5, 0));
        newCategoryFieldPanel.setBackground(BACKGROUND_COLOR);
        newCategoryFieldPanel.add(newCategoryField, BorderLayout.CENTER);
        
        addCategoryButton = new JButton("Add");
        addCategoryButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addCategoryButton.setFocusPainted(false);
        addCategoryButton.setBackground(PRIMARY_COLOR);
        addCategoryButton.setForeground(Color.WHITE);
        addCategoryButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        addCategoryButton.addActionListener(e -> handleAddCategory());
        newCategoryFieldPanel.add(addCategoryButton, BorderLayout.EAST);
        
        formPanel.add(newCategoryFieldPanel, gbc);
        
        // Right panel split into description and subtasks
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setBorder(null);
        rightSplitPane.setDividerSize(5);
        rightSplitPane.setResizeWeight(0.5); // Equal space for description and subtasks
        rightSplitPane.setBackground(BACKGROUND_COLOR);
        
        // Description panel (top)
        JPanel descriptionPanel = new JPanel(new BorderLayout(0, 10));
        descriptionPanel.setBackground(BACKGROUND_COLOR);
        descriptionPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel descriptionLabel = new JLabel("Description");
        descriptionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        descriptionPanel.add(descriptionLabel, BorderLayout.NORTH);
        
        descriptionField = new JTextArea();
        descriptionField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        
        JScrollPane descScrollPane = new JScrollPane(descriptionField);
        descScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        descriptionPanel.add(descScrollPane, BorderLayout.CENTER);
        
        // Subtasks panel (bottom)
        JPanel subtasksContainer = new JPanel(new BorderLayout(0, 10));
        subtasksContainer.setBackground(BACKGROUND_COLOR);
        subtasksContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel subtasksLabel = new JLabel("Sub-Tasks");
        subtasksLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        subtasksContainer.add(subtasksLabel, BorderLayout.NORTH);
        
        // Panel for adding new subtasks
        JPanel addSubTaskPanel = new JPanel(new BorderLayout(5, 0));
        addSubTaskPanel.setBackground(BACKGROUND_COLOR);
        
        JTextField newSubTaskField = new JTextField();
        newSubTaskField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newSubTaskField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        newSubTaskField.putClientProperty("JTextField.placeholderText", "Add a sub-task...");
        
        JButton addSubTaskButton = new JButton("Add");
        addSubTaskButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addSubTaskButton.setFocusPainted(false);
        addSubTaskButton.setBackground(PRIMARY_COLOR);
        addSubTaskButton.setForeground(Color.WHITE);
        addSubTaskButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addSubTaskButton.addActionListener(e -> {
            String subtaskText = newSubTaskField.getText().trim();
            if (!subtaskText.isEmpty() && taskList.getSelectedValue() != null) {
                Task selectedTask = taskList.getSelectedValue();
                SubTask newSubTask = SubTaskController.createAndGetSubTask(selectedTask.getTaskId(), subtaskText);
                if (newSubTask != null) {
                    currentSubTasks.add(newSubTask);
                    updateSubTasksPanel();
                    newSubTaskField.setText("");
                }
            } else if (subtaskText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Subtask description cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Save the task before adding subtasks", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Also allow adding subtasks by pressing Enter
        newSubTaskField.addActionListener(e -> addSubTaskButton.doClick());
        
        addSubTaskPanel.add(newSubTaskField, BorderLayout.CENTER);
        addSubTaskPanel.add(addSubTaskButton, BorderLayout.EAST);
        
        // Create the scrollable panel for subtasks
        subTasksPanel = new JPanel();
        subTasksPanel.setLayout(new BoxLayout(subTasksPanel, BoxLayout.Y_AXIS));
        subTasksPanel.setBackground(BACKGROUND_COLOR);
        
        JScrollPane subtasksScrollPane = new JScrollPane(subTasksPanel);
        subtasksScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        subtasksScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Add components to subtasks container
        JPanel subTasksContentPanel = new JPanel(new BorderLayout(0, 10));
        subTasksContentPanel.setBackground(BACKGROUND_COLOR);
        subTasksContentPanel.add(addSubTaskPanel, BorderLayout.NORTH);
        subTasksContentPanel.add(subtasksScrollPane, BorderLayout.CENTER);
        
        subtasksContainer.add(subTasksContentPanel, BorderLayout.CENTER);
        
        // Add panels to right split pane
        rightSplitPane.setTopComponent(descriptionPanel);
        rightSplitPane.setBottomComponent(subtasksContainer);
        
        // Add panels to horizontal split pane - MODIFIÉ: Utiliser formScrollPane au lieu de formPanel
        horizontalSplitPane.setLeftComponent(formScrollPane);
        horizontalSplitPane.setRightComponent(rightSplitPane);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(horizontalSplitPane, BorderLayout.CENTER);
        
        return panel;
    }

    // Add a method to create a subtask row
    private JPanel createSubTaskRow(SubTask subTask) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(BACKGROUND_COLOR);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Checkbox for completion status
        JCheckBox checkBox = new JCheckBox();
        checkBox.setBackground(BACKGROUND_COLOR);
        checkBox.setSelected(subTask.isCompleted());
        checkBox.addActionListener(e -> {
            boolean isCompleted = checkBox.isSelected();
            subTask.setCompleted(isCompleted);
            SubTaskController.updateSubTaskStatus(subTask.getSubTaskId(), isCompleted);
            
            // Update the label when checkbox is toggled
            JLabel descLabel = (JLabel) rowPanel.getComponent(1);
            updateSubTaskLabel(descLabel, subTask.getDescription(), isCompleted);
        });
        
        rowPanel.add(checkBox, BorderLayout.WEST);
        
        // Use JLabel with edit icon instead of JTextField for properly displaying HTML
        JPanel descriptionPanel = new JPanel(new BorderLayout(5, 0));
        descriptionPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel descLabel = new JLabel(subTask.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Apply strikethrough if completed
        updateSubTaskLabel(descLabel, subTask.getDescription(), subTask.isCompleted());
        
        descriptionPanel.add(descLabel, BorderLayout.CENTER);
        
        // Add edit button
        JButton editButton = new JButton("✎");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editButton.setForeground(PRIMARY_COLOR);
        editButton.setBackground(BACKGROUND_COLOR);
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> {
            String currentDesc = subTask.getDescription();
            String newDesc = JOptionPane.showInputDialog(this, 
                "Edit subtask:", currentDesc);
            
            if (newDesc != null && !newDesc.trim().isEmpty()) {
                subTask.setDescription(newDesc.trim());
                SubTaskController.updateSubTaskDescription(subTask.getSubTaskId(), newDesc.trim());
                updateSubTaskLabel(descLabel, newDesc.trim(), subTask.isCompleted());
            }
        });
        
        descriptionPanel.add(editButton, BorderLayout.EAST);
        rowPanel.add(descriptionPanel, BorderLayout.CENTER);
        
        // Delete button
        JButton deleteButton = new JButton("×");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        deleteButton.setForeground(new Color(220, 53, 69));
        deleteButton.setBackground(BACKGROUND_COLOR);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this sub-task?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (SubTaskController.deleteSubTask(subTask.getSubTaskId())) {
                    currentSubTasks.remove(subTask);
                    updateSubTasksPanel();
                }
            }
        });
        
        rowPanel.add(deleteButton, BorderLayout.EAST);
        
        return rowPanel;
    }
    
    // Helper method to update label with strikethrough for completed subtasks
    private void updateSubTaskLabel(JLabel label, String description, boolean isCompleted) {
        if (isCompleted) {
            label.setText("<html><strike>" + description + "</strike></html>");
        } else {
            // Remove any HTML tags if present
            if (description.contains("<html>")) {
                description = description.replaceAll("<[^>]*>", "");
            }
            label.setText(description);
        }
    }

    // Add a method to update the subtasks panel
    private void updateSubTasksPanel() {
        subTasksPanel.removeAll();
        
        for (SubTask subTask : currentSubTasks) {
            subTasksPanel.add(createSubTaskRow(subTask));
        }
        
        subTasksPanel.revalidate();
        subTasksPanel.repaint();
    }

    private void styleSearchField(JTextField field) {
        field.setBackground(HOVER_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
        field.putClientProperty("JTextField.placeholderText", "Search tasks...");
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // Title
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh Dashboard");
        refreshButton.setIcon(createRefreshIcon());
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            updateDashboard();
            JOptionPane.showMessageDialog(panel, "Dashboard refreshed!", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });
        
        headerPanel.add(refreshButton, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Create split pane for left and right sections
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.6);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);

        // Left panel (Tasks and Statistics)
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        leftPanel.setBackground(BACKGROUND_COLOR);

        // Recent Tasks Section
        JPanel tasksSection = createSectionPanel("Recent Tasks");
        JPanel tasksList = new JPanel();
        tasksList.setLayout(new BoxLayout(tasksList, BoxLayout.Y_AXIS));
        tasksList.setBackground(CARD_COLOR);

        List<Task> recentTasks = TaskController.getTasks(userId);
        for (int i = 0; i < Math.min(5, recentTasks.size()); i++) {
            Task task = recentTasks.get(i);
            tasksList.add(createTaskItem(task));
            if (i < Math.min(4, recentTasks.size())) {
                tasksList.add(createSeparator());
            }
        }

        JScrollPane tasksScroll = new JScrollPane(tasksList);
        tasksScroll.setBorder(null);
        tasksScroll.setBackground(CARD_COLOR);
        tasksSection.add(tasksScroll, BorderLayout.CENTER);

        // Statistics Section
        JPanel statsSection = createSectionPanel("Statistics");
        ChartPanel chartPanel = new ChartPanel(new int[]{0, 0, 0});
        chartPanel.setPreferredSize(new Dimension(0, 200));
        statsSection.add(chartPanel, BorderLayout.CENTER);

        leftPanel.add(tasksSection);
        leftPanel.add(statsSection);

        // Right panel (Folders and Notes)
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        rightPanel.setBackground(BACKGROUND_COLOR);

        // Folders Section
        JPanel foldersSection = createSectionPanel("Folders");
        JPanel foldersGrid = new JPanel(new GridLayout(0, 2, 10, 10));
        foldersGrid.setBackground(CARD_COLOR);
        
        String[] folderNames = {"Work", "Personal", "Training"};
        Color[] folderColors = {new Color(255, 107, 107), new Color(79, 193, 233), new Color(162, 155, 254)};
        
        for (int i = 0; i < folderNames.length; i++) {
            foldersGrid.add(createFolderCard(folderNames[i], folderColors[i]));
        }
        
        foldersSection.add(foldersGrid, BorderLayout.CENTER);

        // Notes Section
        JPanel notesSection = createSectionPanel("Notes");
        JPanel notesList = new JPanel();
        notesList.setLayout(new BoxLayout(notesList, BoxLayout.Y_AXIS));
        notesList.setBackground(CARD_COLOR);
        
        // Add sample notes (replace with actual notes later)
        String[] sampleNotes = {
            "Meeting notes from yesterday",
            "Project ideas",
            "Weekly goals"
        };
        
        for (int i = 0; i < sampleNotes.length; i++) {
            notesList.add(createNoteItem(sampleNotes[i]));
            if (i < sampleNotes.length - 1) {
                notesList.add(createSeparator());
            }
        }
        
        notesSection.add(notesList, BorderLayout.CENTER);

        rightPanel.add(foldersSection);
        rightPanel.add(notesSection);

        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Header with title and optional link
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        header.add(titleLabel, BorderLayout.WEST);

        JLabel viewAllLink = new JLabel("View all >");
        viewAllLink.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewAllLink.setForeground(PRIMARY_COLOR);
        viewAllLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(viewAllLink, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createTaskItem(Task task) {
        // Refresh the task data from the database to ensure we have the most up-to-date information
        final Task refreshedTask = TaskController.getTaskById(task.getTaskId());
        if (refreshedTask != null) {
            task = refreshedTask;
        }
        
        final Task finalTask = task; // Create a final reference for use in lambdas
        
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));

        // Checkbox
        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(CARD_COLOR);
        checkbox.setSelected("Completed".equalsIgnoreCase(finalTask.getStatus()));
        
        // Add action listener to checkbox to update task status when clicked
        checkbox.addActionListener(e -> {
            String newStatus = checkbox.isSelected() ? "Completed" : "Pending";
            if (TaskController.updateTaskStatus(finalTask.getTaskId(), newStatus)) {
                // Update UI after status change
                loadTasks();
                updateDashboard();
                updateCalendar();
            }
        });
        
        panel.add(checkbox, BorderLayout.WEST);

        // Task details
        JPanel details = new JPanel(new BorderLayout(5, 0));
        details.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel(finalTask.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Set color and style based on status - using case-insensitive comparison
        String status = finalTask.getStatus().toLowerCase();
        if (status.equals("completed")) {
            titleLabel.setText("<html><strike>" + finalTask.getTitle() + "</strike></html>");
            titleLabel.setForeground(COMPLETED_COLOR);
        } else if (status.equals("in progress")) {
            titleLabel.setForeground(IN_PROGRESS_COLOR);
        } else if (status.equals("pending")) {
            titleLabel.setForeground(PENDING_STATUS_COLOR);
        } else {
            titleLabel.setForeground(TEXT_COLOR);
        }
        details.add(titleLabel, BorderLayout.CENTER);

        if (finalTask.getDueDate() != null) {
            JLabel dateLabel = new JLabel(formatDueDate(finalTask.getDueDate()));
            dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateLabel.setForeground(new Color(158, 158, 158));
            details.add(dateLabel, BorderLayout.EAST);
        }

        // Make the panel clickable to view task details
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get fresh task data from database
                Task freshTask = TaskController.getTaskById(finalTask.getTaskId());
                if (freshTask != null) {
                    // Find the task in the list model
                    for (int i = 0; i < taskListModel.getSize(); i++) {
                        if (taskListModel.getElementAt(i).getTaskId() == freshTask.getTaskId()) {
                            taskList.setSelectedIndex(i);
                            break;
                        }
                    }
                    displayTaskDetails(freshTask);
                }
            }
        });

        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFolderCard(String name, Color color) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Folder icon (colored circle)
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fillOval(0, 0, 12, 12);
            }
        };
        iconPanel.setPreferredSize(new Dimension(12, 12));
        iconPanel.setBackground(CARD_COLOR);
        panel.add(iconPanel, BorderLayout.WEST);

        // Folder name and task count
        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        textPanel.setBackground(CARD_COLOR);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_COLOR);
        textPanel.add(nameLabel, BorderLayout.NORTH);

        // Count tasks in this folder (placeholder)
        JLabel countLabel = new JLabel("3 tasks");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(new Color(158, 158, 158));
        textPanel.add(countLabel, BorderLayout.SOUTH);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createNoteItem(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Note icon
        JLabel iconLabel = new JLabel("📝");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(iconLabel, BorderLayout.WEST);

        // Note title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(CARD_COLOR);
        return separator;
    }

    private JPanel createSettingsRow(String label, int defaultValue) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.WHITE);

        SpinnerModel model = new SpinnerNumberModel(defaultValue / 60, 1, 60, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(new Dimension(60, 30));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(Color.WHITE);
            tf.setForeground(currentPomodoroColor);
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
            ));
        }

        panel.add(titleLabel);
        panel.add(spinner);
        return panel;
    }
    
    private JPanel createCheckboxRow(String label) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        JCheckBox checkbox = new JCheckBox(label);
        checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        checkbox.setForeground(Color.WHITE);
        checkbox.setOpaque(false);
        checkbox.setFocusPainted(false);
        checkbox.setIcon(createCheckboxIcon(false));
        checkbox.setSelectedIcon(createCheckboxIcon(true));

        panel.add(checkbox);
        return panel;
    }

    private Icon createCheckboxIcon(boolean selected) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw checkbox
                g2d.setColor(selected ? Color.WHITE : new Color(255, 255, 255, 100));
                g2d.drawRect(x, y, 16, 16);
                
                if (selected) {
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawLine(x + 3, y + 8, x + 7, y + 12);
                    g2d.drawLine(x + 7, y + 12, x + 13, y + 4);
                }
                
                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    private JPanel createDetailSection(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(BACKGROUND_COLOR);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(115, 115, 115));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        section.add(titleLabel);
        return section;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BACKGROUND_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_COLOR);
    }

    private void styleTextArea(JTextArea area) {
        area.setBackground(BACKGROUND_COLOR);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(TEXT_COLOR);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    private void styleButton(JButton button, boolean isPrimary) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(isPrimary ? Color.WHITE : TEXT_COLOR);
        button.setBackground(isPrimary ? PRIMARY_COLOR : BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isPrimary ? PRIMARY_COLOR : BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(button.getBackground().darker());
                } else {
                    button.setBackground(HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(isPrimary ? PRIMARY_COLOR : BACKGROUND_COLOR);
            }
        });
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(BACKGROUND_COLOR);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        ((JComponent) comboBox.getRenderer()).setBackground(BACKGROUND_COLOR);
    }
    
    private void styleDateSpinner(JSpinner spinner) {
        spinner.setBackground(BACKGROUND_COLOR);
        spinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MMM d, yyyy");
        spinner.setEditor(editor);
        editor.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editor.getTextField().setForeground(TEXT_COLOR);
        editor.getTextField().setBackground(BACKGROUND_COLOR);
    }

    private void loadTasks() {
        taskListModel.clear();
        
        try {
            // Get fresh data from the database - using normal tasks without occurrences for list view
            List<Task> tasks = TaskController.getTasks(userId);
            
            // Tri des tâches selon la priorité et le statut
            Collections.sort(tasks, (t1, t2) -> {
                // D'abord, comparer les statuts (Pending et In Progress avant Completed)
                boolean t1Completed = "Completed".equalsIgnoreCase(t1.getStatus());
                boolean t2Completed = "Completed".equalsIgnoreCase(t2.getStatus());
                
                if (t1Completed && !t2Completed) return 1;
                if (!t1Completed && t2Completed) return -1;
                
                // Ensuite, si les statuts sont équivalents, comparer les priorités
                String p1 = t1.getPriority() != null ? t1.getPriority() : "NORMAL";
                String p2 = t2.getPriority() != null ? t2.getPriority() : "NORMAL";
                
                // Ordre de priorité: URGENT > HIGH > NORMAL > LOW
                int p1Value = getPriorityValue(p1);
                int p2Value = getPriorityValue(p2);
                
                return Integer.compare(p1Value, p2Value);
            });
            
            // Add each task to the list model
            for (Task task : tasks) {
                taskListModel.addElement(task);
            }
            
            // Refresh the dashboard and calendar views
            if (dashboardPanel != null) {
                updateDashboard();
            }
        } catch (Exception e) {
            System.out.println("Error loading tasks: " + e.getMessage());
        }
    }
    
    // Méthode d'aide pour attribuer une valeur numérique à chaque priorité pour le tri
    private int getPriorityValue(String priority) {
        switch (priority) {
            case "URGENT": return 1;
            case "HIGH": return 2;
            case "NORMAL": return 3;
            case "LOW": return 4;
            default: return 3; // Valeur par défaut pour NORMAL
        }
    }

    private JPanel createTaskListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setPreferredSize(new Dimension(300, getHeight()));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        
        // Header with title, filter, and add button
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(SIDEBAR_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title and buttons panel
        JPanel titleButtonPanel = new JPanel(new BorderLayout(10, 0));
        titleButtonPanel.setBackground(SIDEBAR_COLOR);
        
        // Title label on the left
        JLabel titleLabel = new JLabel("Task Manager");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleButtonPanel.add(titleLabel, BorderLayout.WEST);
        
        // Buttons panel for refresh and new
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(SIDEBAR_COLOR);
        
        // Refresh button
        JButton refreshButton = new JButton();
        refreshButton.setIcon(createRefreshIcon());
        refreshButton.setToolTipText("Refresh Tasks");
        refreshButton.setFocusPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            loadTasks();
            JOptionPane.showMessageDialog(this, "Tasks refreshed!", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Add button on the right
        JButton newButton = new JButton("+ New Task");
        newButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newButton.setFocusPainted(false);
        newButton.setBackground(PRIMARY_COLOR);
        newButton.setForeground(Color.WHITE);
        newButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        newButton.addActionListener(e -> {
            clearFields();
            showTaskDetailsView();
        });
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(newButton);
        titleButtonPanel.add(buttonsPanel, BorderLayout.EAST);
        
        headerPanel.add(titleButtonPanel, BorderLayout.NORTH);
        
        // Search field below the title
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(SIDEBAR_COLOR);
        searchPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        styleSearchField(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTasks(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTasks(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTasks(searchField.getText());
            }
        });
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        headerPanel.add(searchPanel, BorderLayout.CENTER);

        // Task list with custom renderer
        taskList.setBackground(SIDEBAR_COLOR);
        taskList.setBorder(null);
        taskList.setFixedCellHeight(90);
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(SIDEBAR_COLOR);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
    
    private void filterTasks(String searchText) {
        if (searchText.isEmpty()) {
            loadTasks(); // Reload all tasks
            return;
        }
        
        searchText = searchText.toLowerCase();
        DefaultListModel<Task> filteredModel = new DefaultListModel<>();
        
        for (int i = 0; i < taskListModel.getSize(); i++) {
            Task task = taskListModel.getElementAt(i);
            if (task.getTitle().toLowerCase().contains(searchText) || 
                (task.getDescription() != null && task.getDescription().toLowerCase().contains(searchText))) {
                filteredModel.addElement(task);
            }
        }
        
        taskList.setModel(filteredModel);
    }

    /**
     * Displays the details of a task in the task details panel
     */
    private void displayTaskDetails(Task task) {
        // Get the latest version of the task from the database
        Task freshTask = TaskController.getTaskById(task.getTaskId());
        if (freshTask != null) {
            task = freshTask;
        }
        
        titleField.setText(task.getTitle());
        descriptionField.setText(task.getDescription());
        
        // Handle status safely
        String status = task.getStatus();
        if (status != null) {
            // Find matching status in the combo box
            for (int i = 0; i < statusComboBox.getItemCount(); i++) {
                String comboStatus = statusComboBox.getItemAt(i);
                if (status.equalsIgnoreCase(comboStatus)) {
                    statusComboBox.setSelectedIndex(i);
                    break;
                }
            }
            // If no exact match found, try to match by substring
            if (statusComboBox.getSelectedIndex() == -1) {
                status = status.toLowerCase();
                if (status.contains("complete") || status.equals("done")) {
                    for (int i = 0; i < statusComboBox.getItemCount(); i++) {
                        if (statusComboBox.getItemAt(i).toLowerCase().contains("complete")) {
                            statusComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                } else if (status.contains("progress")) {
                    for (int i = 0; i < statusComboBox.getItemCount(); i++) {
                        if (statusComboBox.getItemAt(i).toLowerCase().contains("progress")) {
                            statusComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                } else if (status.contains("pending") || status.equals("waiting")) {
                    for (int i = 0; i < statusComboBox.getItemCount(); i++) {
                        if (statusComboBox.getItemAt(i).toLowerCase().contains("pending")) {
                            statusComboBox.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    // Default to first status if no match
                    statusComboBox.setSelectedIndex(0);
                }
            }
        } else {
            // If status is null, default to the first item
            statusComboBox.setSelectedIndex(0);
        }
        
        // Set due date
        if (task.getDueDate() != null) {
            dueDateSpinner.setValue(task.getDueDate());
        } else {
            dueDateSpinner.setValue(new Date());
        }
        
        // Set recurrence properties
        isRecurringCheckBox.setSelected(task.isRecurring());
        toggleRecurrenceControls();
        
        if (task.isRecurring()) {
            // Set recurrence type
            String recurrenceType = task.getRecurrenceType();
            if (recurrenceType != null) {
                for (int i = 0; i < recurrenceTypeComboBox.getItemCount(); i++) {
                    String type = recurrenceTypeComboBox.getItemAt(i);
                    if (recurrenceType.equalsIgnoreCase(type)) {
                        recurrenceTypeComboBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            // Set recurrence interval
            recurrenceIntervalSpinner.setValue(task.getRecurrenceInterval() > 0 ? 
                task.getRecurrenceInterval() : 1);
            
            // Set recurrence end date if available
            if (task.getRecurrenceEndDate() != null) {
                recurrenceEndDateSpinner.setValue(task.getRecurrenceEndDate());
            } else {
                // Default to 3 months in the future
                Calendar endCal = Calendar.getInstance();
                endCal.add(Calendar.MONTH, 3);
                recurrenceEndDateSpinner.setValue(endCal.getTime());
            }
        }
        
        // Set category
        if (task.getCategory() != null) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category cat = categoryComboBox.getItemAt(i);
                if (cat.getId() == task.getCategory().getId()) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            categoryComboBox.setSelectedIndex(0);
        }
        
                // Set priority if available        if (task.getPriority() != null) {            for (int i = 0; i < priorityComboBox.getItemCount(); i++) {                String priority = priorityComboBox.getItemAt(i);                if (task.getPriority().equals(priority)) {                    priorityComboBox.setSelectedIndex(i);                    break;                }            }        } else {            // Default to NORMAL priority            for (int i = 0; i < priorityComboBox.getItemCount(); i++) {                if ("NORMAL".equals(priorityComboBox.getItemAt(i))) {                    priorityComboBox.setSelectedIndex(i);                    break;                }            }        }                // Set the current subtasks        currentSubTasks = SubTaskController.getSubTasksByTaskId(task.getTaskId());        updateSubTasksPanel();
        
        // Enable delete button since a task is selected
        deleteButton.setEnabled(true);
        
        // Show the task details panel
        showTaskDetailsView();
    }
    
    /**
     * Shows the task details view
     */
    private void showTaskDetailsView() {
        CardLayout cardLayout = (CardLayout) ((JPanel) mainContentPanel.getComponent(0)).getLayout();
        cardLayout.show((JPanel) mainContentPanel.getComponent(0), "TASK_DETAILS");
    }
    
    /**
     * Shows the task list view
     */
    private void showTaskListView() {
        CardLayout cardLayout = (CardLayout) ((JPanel) mainContentPanel.getComponent(0)).getLayout();
        cardLayout.show((JPanel) mainContentPanel.getComponent(0), "TASK_LIST");
        taskList.clearSelection();
    }

    /**
     * Creates the calendar panel for viewing tasks by date
     */
    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        // Month navigation
        JPanel monthNavPanel = new JPanel(new BorderLayout());
        monthNavPanel.setBackground(BACKGROUND_COLOR);
        
        JButton prevButton = new JButton("◀");
        prevButton.setFocusPainted(false);
        prevButton.setBorderPainted(false);
        prevButton.setContentAreaFilled(false);
        prevButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        prevButton.setForeground(PRIMARY_COLOR);
        
        JButton nextButton = new JButton("▶");
        nextButton.setFocusPainted(false);
        nextButton.setBorderPainted(false);
        nextButton.setContentAreaFilled(false);
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nextButton.setForeground(PRIMARY_COLOR);
        
        // Current month label
        currentCalendar = Calendar.getInstance();
        monthLabel = new JLabel(monthFormat.format(currentCalendar.getTime()), JLabel.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        // Add buttons to navigate between months
        prevButton.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        
        nextButton.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });
        
        monthNavPanel.add(prevButton, BorderLayout.WEST);
        monthNavPanel.add(monthLabel, BorderLayout.CENTER);
        monthNavPanel.add(nextButton, BorderLayout.EAST);
        
        headerPanel.add(monthNavPanel, BorderLayout.NORTH);
        
        // Days of week header
        JPanel daysHeader = new JPanel(new GridLayout(1, 7));
        daysHeader.setBackground(BACKGROUND_COLOR);
        
        String[] daysOfWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : daysOfWeek) {
            JLabel dayLabel = new JLabel(day, JLabel.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            daysHeader.add(dayLabel);
        }
        
        headerPanel.add(daysHeader, BorderLayout.SOUTH);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Calendar grid
        JPanel calendarGrid = new JPanel(new GridLayout(6, 7));
        calendarGrid.setBackground(BACKGROUND_COLOR);
        
        // Fill the calendar grid with day cells
        for (int i = 0; i < 42; i++) {
            JPanel dayCell = new JPanel(new BorderLayout());
            dayCell.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            dayCell.setBackground(CARD_COLOR);
            
            JLabel dateLabel = new JLabel("", JLabel.RIGHT);
            dateLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
        JPanel tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksPanel.setBackground(CARD_COLOR);
            
            dayCell.add(dateLabel, BorderLayout.NORTH);
            dayCell.add(tasksPanel, BorderLayout.CENTER);
            
            calendarGrid.add(dayCell);
        }
        
        panel.add(calendarGrid, BorderLayout.CENTER);
        
        // Save panel reference first, then initialize calendar
        // Don't call updateCalendar here to avoid the circular dependency
        
        return panel;
    }
    
    /**
     * Updates the calendar display with the current month
     */
    private void updateCalendar() {
        monthLabel.setText(monthFormat.format(currentCalendar.getTime()));
        
        // Get tasks for the month INCLUDING generated occurrences
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Determine first day of month and adjust for week display
        int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;
        
        // Reset calendar grid - get it directly from calendarPanel instead of using hardcoded index
        JPanel calendarGrid = (JPanel) calendarPanel.getComponent(1);
        
        // Clear all cells
        for (Component comp : calendarGrid.getComponents()) {
            JPanel dayCell = (JPanel) comp;
            if (dayCell.getComponentCount() >= 2) {
                JLabel dateLabel = (JLabel) dayCell.getComponent(0);
                JPanel tasksPanel = (JPanel) dayCell.getComponent(1);
                
                dateLabel.setText("");
                tasksPanel.removeAll();
                dayCell.setBackground(CARD_COLOR);
            }
        }
        
        // Get days in month
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Get today's date for highlighting
        Calendar today = Calendar.getInstance();
        
        // Load fresh data from the database WITH RECURRING OCCURRENCES for calendar view
        List<Task> tasksWithOccurrences = TaskController.getTasksWithOccurrences(userId);
        
        // Fill calendar with dates
        for (int i = 0; i < daysInMonth; i++) {
            if (firstDayOfMonth + i >= calendarGrid.getComponentCount()) {
                continue; // Skip if out of bounds
            }
            
            JPanel dayCell = (JPanel) calendarGrid.getComponent(firstDayOfMonth + i);
            if (dayCell.getComponentCount() < 2) continue;
            
            JLabel dateLabel = (JLabel) dayCell.getComponent(0);
            JPanel tasksPanel = (JPanel) dayCell.getComponent(1);
            
            tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
            
            // Set date label
            int day = i + 1;
            dateLabel.setText(String.valueOf(day));
            
            // Highlight today's date
            if (currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                day == today.get(Calendar.DAY_OF_MONTH)) {
                dateLabel.setForeground(PRIMARY_COLOR);
                dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                dateLabel.setForeground(TEXT_COLOR);
                dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }
            
            // Add tasks for this day
            for (Task task : tasksWithOccurrences) {
                // Skip tasks without due dates
                if (task.getDueDate() == null) continue;
                
                try {
                    // Check if task is on this day
                    Calendar taskDate = Calendar.getInstance();
                    taskDate.setTime(task.getDueDate());
                    
                    if (taskDate.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                        taskDate.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                        taskDate.get(Calendar.DAY_OF_MONTH) == day) {
                        
                        // Create a panel for this task
                        JPanel taskPanel = new JPanel(new BorderLayout());
                        taskPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
                        taskPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                        
                        // Determine color based on status and priority
                        Color taskColor;
                        String status = task.getStatus() != null ? task.getStatus().toLowerCase() : "";
                        String priority = task.getPriority();
                        
                        // Priorité élevée (URGENT ou HIGH) a préséance sur le statut pour la couleur
                        if (priority != null && ("URGENT".equals(priority) || "HIGH".equals(priority))) {
                            taskColor = "URGENT".equals(priority) ? URGENT_PRIORITY_COLOR : HIGH_PRIORITY_COLOR;
                        } else if (status.contains("completed") || status.equals("done")) {
                            taskColor = COMPLETED_COLOR; // Green
                        } else if (status.contains("pending") || status.equals("waiting")) {
                            taskColor = PENDING_STATUS_COLOR; // Gray
                        } else if (status.contains("progress") || status.equals("in progress")) {
                            taskColor = IN_PROGRESS_COLOR; // Orange
                        } else {
                            taskColor = IN_PROGRESS_COLOR; // Default to orange for any other status
                        }
                        
                        // Create a color bar on the left
                        JPanel colorBar = new JPanel();
                        colorBar.setBackground(taskColor);
                        colorBar.setPreferredSize(new Dimension(3, 0));
                        
                        // Create the task label with priority indicator
                        JPanel labelPanel = new JPanel(new BorderLayout(5, 0));
                        labelPanel.setBackground(taskPanel.getBackground());
                        
                        JLabel taskLabel = new JLabel(task.getTitle());
                        taskLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        taskLabel.setForeground(TEXT_COLOR);
                        taskLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                        
                        // Ajouter l'indicateur de priorité si présent
                        if (priority != null && !"NORMAL".equals(priority)) {
                            JLabel priorityLabel = new JLabel();
                            priorityLabel.setIcon(createPriorityIcon(priority));
                            labelPanel.add(priorityLabel, BorderLayout.EAST);
                        }
                        
                        labelPanel.add(taskLabel, BorderLayout.CENTER);
                        
                        // Add a background color based on status (with transparency)
                        Color bgColor = new Color(
                            taskColor.getRed(),
                            taskColor.getGreen(),
                            taskColor.getBlue(),
                            30); // Light transparency
                        taskPanel.setBackground(bgColor);
                        
                        // Add components to task panel
                        taskPanel.add(colorBar, BorderLayout.WEST);
                        taskPanel.add(labelPanel, BorderLayout.CENTER);
                        
                        // Make task panel clickable to view task details
                        final Task finalTask = task;
                        taskPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                // Use the task ID to get a fresh copy of the task from the database
                                Task freshTask = TaskController.getTaskById(finalTask.getTaskId());
                                if (freshTask != null) {
                                    // Find the task in the list model
                                    for (int k = 0; k < taskListModel.getSize(); k++) {
                                        if (taskListModel.getElementAt(k).getTaskId() == freshTask.getTaskId()) {
                                            taskList.setSelectedIndex(k);
                                            break;
                                        }
                                    }
                                    displayTaskDetails(freshTask);
                                }
                            }
                        });
                        
                        // Add task to the day cell
                        tasksPanel.add(taskPanel);
                    }
                } catch (Exception e) {
                    System.out.println("Error displaying task in calendar: " + e.getMessage());
                }
            }
            
            tasksPanel.revalidate();
            tasksPanel.repaint();
        }
        
        // Ensure the entire calendar is properly refreshed
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    /**
     * Switches the main panel to show the requested view
     */
    private void showPanel(String name) {
        CardLayout cl = (CardLayout) mainContentPanel.getLayout();
        cl.show(mainContentPanel, name);
        
        // Update button states
        switch (name) {
            case "TASKS":
                // Find the Tasks button in navigation panel
                for (Component c : navigationPanel.getComponents()) {
                    if (c instanceof JButton && ((JButton)c).getText().contains("Tasks")) {
                        updateSelectedButton((JButton)c);
                        break;
                    }
                }
                // Show the task list view by default
                showTaskListView();
                break;
            case "POMODORO":
                if (pomodoroButton != null) {
                    updateSelectedButton(pomodoroButton);
                }
                break;
            case "DASHBOARD":
                if (dashboardButton != null) {
                    updateSelectedButton(dashboardButton);
                    updateDashboard(); // Refresh dashboard data
                }
                break;
            case "CALENDAR":
                if (calendarButton != null) {
                    updateSelectedButton(calendarButton);
                }
                // Only update calendar if it's not null
                if (calendarPanel != null) {
                    updateCalendar(); // Refresh calendar data
                }
                break;
            case "NOTES":
                // Handle notes view if we have one
                JButton notesButton = null;
                // Try to find the notes button in the navigation panel
                for (Component c : navigationPanel.getComponents()) {
                    if (c instanceof JButton && ((JButton)c).getText().contains("Notes")) {
                        notesButton = (JButton)c;
                        break;
                    }
                }
                if (notesButton != null) {
                    updateSelectedButton(notesButton);
                }
                // Show the notes list view by default
                showNotesListView();
                break;
        }
    }

    /**
     * Formats a date into a human-readable format for displaying due dates
     */
    private String formatDueDate(Date date) {
        if (date == null) return "";
        
        Calendar today = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        dueDate.setTime(date);

        if (today.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == dueDate.get(Calendar.DAY_OF_YEAR)) {
            return "Today";
        } else if (today.get(Calendar.YEAR) == dueDate.get(Calendar.YEAR) &&
                   today.get(Calendar.DAY_OF_YEAR) + 1 == dueDate.get(Calendar.DAY_OF_YEAR)) {
            return "Tomorrow";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
            return sdf.format(date);
        }
    }
    
    /**
     * Clears all form fields to create a new task
     */
    private void clearFields() {
        titleField.setText("");
        descriptionField.setText("");
                statusComboBox.setSelectedIndex(0);        // Réinitialiser la priorité à NORMAL        for (int i = 0; i < priorityComboBox.getItemCount(); i++) {            if ("NORMAL".equals(priorityComboBox.getItemAt(i))) {                priorityComboBox.setSelectedIndex(i);                break;            }        }        dueDateSpinner.setValue(new Date());        categoryComboBox.setSelectedIndex(0);        newCategoryField.setText("");
        taskList.clearSelection();
        deleteButton.setEnabled(false);
        
        // Clear recurrence settings
        isRecurringCheckBox.setSelected(false);
        recurrenceTypeComboBox.setSelectedIndex(0);
        recurrenceIntervalSpinner.setValue(1);
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.MONTH, 3);
        recurrenceEndDateSpinner.setValue(endCal.getTime());
        toggleRecurrenceControls();
        
        // Clear subtasks
        currentSubTasks.clear();
        updateSubTasksPanel();
    }
    
    /**
     * Loads all categories from the database into the combo box
     */
    private void loadCategories() {
        categoryComboBox.removeAllItems();
        
        // Add a "No Category" option
        categoryComboBox.addItem(new Category(0, "No Category"));
        
        // Load all categories from the database
        List<Category> categories = CategoryController.getAllCategories();
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
    }
    
    /**
     * Handles adding a new category
     */
    private void handleAddCategory() {
        String categoryName = newCategoryField.getText().trim();
        if (!categoryName.isEmpty()) {
            Category newCategory = CategoryController.createCategory(categoryName);
            if (newCategory != null) {
                loadCategories(); // Reload the categories
                categoryComboBox.setSelectedItem(newCategory); // Select the new category
                newCategoryField.setText(""); // Clear the field
            }
        }
    }
    
    /**
     * Handles updating an existing task
     */
        private void handleUpdateTask() {        Task selectedTask = taskList.getSelectedValue();        if (selectedTask == null) {            JOptionPane.showMessageDialog(this, "No task selected!", "Warning", JOptionPane.WARNING_MESSAGE);                return;            }                    String title = titleField.getText().trim();        String description = descriptionField.getText().trim();        String status = (String) statusComboBox.getSelectedItem();        String priority = (String) priorityComboBox.getSelectedItem();        Date dueDate = (Date) dueDateSpinner.getValue();        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();

        // Get recurrence settings
        boolean isRecurring = isRecurringCheckBox.isSelected();
        String recurrenceType = isRecurring ? (String) recurrenceTypeComboBox.getSelectedItem() : null;
        int recurrenceInterval = isRecurring ? (int) recurrenceIntervalSpinner.getValue() : 0;
        Date recurrenceEndDate = isRecurring ? (Date) recurrenceEndDateSpinner.getValue() : null;

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty!", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success;
        if (selectedCategory != null && selectedCategory.getId() != 0) {
                        success = TaskController.updateTask(                selectedTask.getTaskId(),                 title,                 description,                 status,                 dueDate,                 selectedCategory,                isRecurring,                recurrenceType,                recurrenceInterval,                recurrenceEndDate,                priority            );
        } else {
                        success = TaskController.updateTask(                selectedTask.getTaskId(),                 title,                 description,                 status,                 dueDate,                isRecurring,                recurrenceType,                recurrenceInterval,                recurrenceEndDate,                priority            );
        }
        
        if (success) {
            loadTasks();
            updateDashboard();
            updateCalendar(); // Update calendar to reflect the changes
            showTaskListView();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update task!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles deleting a task
     */
    private void handleDeleteTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this task and all its sub-tasks?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Delete subtasks first (optional, since we use ON DELETE CASCADE in SQL)
            SubTaskController.deleteSubTasksByTaskId(selectedTask.getTaskId());
            
            // Then delete the task
            if (TaskController.deleteTask(selectedTask.getTaskId())) {
                loadTasks();
                clearFields();
                updateDashboard();
                showTaskListView();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete task",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles adding a new task
     */
        private void handleAddTask() {        String title = titleField.getText().trim();        String description = descriptionField.getText().trim();        String status = (String) statusComboBox.getSelectedItem();        String priority = (String) priorityComboBox.getSelectedItem();        Date dueDate = (Date) dueDateSpinner.getValue();        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        
        // Get recurrence settings
        boolean isRecurring = isRecurringCheckBox.isSelected();
        String recurrenceType = isRecurring ? (String) recurrenceTypeComboBox.getSelectedItem() : null;
        int recurrenceInterval = isRecurring ? (int) recurrenceIntervalSpinner.getValue() : 0;
        Date recurrenceEndDate = isRecurring ? (Date) recurrenceEndDateSpinner.getValue() : null;
        // Get max occurrences value
        Integer maxOccurrences = isRecurring ? (Integer) occurrencesSpinner.getValue() : null;
        
        // Récupérer les valeurs spécifiques en fonction du type de récurrence
        Integer dayOfWeek = null;
        Integer dayOfMonth = null;
        Integer monthOfYear = null;
        
        if (isRecurring) {
            switch (recurrenceType) {
                case "Weekly":
                    // Convertir le jour de la semaine en entier (dimanche=1, lundi=2, etc.)
                    dayOfWeek = dayOfWeekComboBox.getSelectedIndex() + 1;
                    break;
                case "Monthly":
                    dayOfMonth = (Integer) dayOfMonthSpinner.getValue();
                    break;
                case "Yearly":
                    // Convertir le mois en entier (janvier=1, février=2, etc.)
                    monthOfYear = monthOfYearComboBox.getSelectedIndex() + 1;
                    break;
            }
        }
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty!", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        boolean success;
        if (selectedCategory != null && selectedCategory.getId() != 0) {
                        success = TaskController.createTask(                userId,                 title,                 description,                 status,                 dueDate,                 selectedCategory,                isRecurring,                recurrenceType,                recurrenceInterval,                recurrenceEndDate,                maxOccurrences,                dayOfWeek,                dayOfMonth,                monthOfYear,                priority            );
        } else {
                        success = TaskController.createTask(                userId,                 title,                 description,                 status,                 dueDate,                isRecurring,                recurrenceType,                recurrenceInterval,                recurrenceEndDate,                maxOccurrences,                dayOfWeek,                dayOfMonth,                monthOfYear,                priority            );
        }
        
        if (success) {
            loadTasks();
            clearFields();
            updateDashboard();
            updateCalendar(); // Update calendar to reflect the changes
            showTaskListView();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add task!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates the notes list panel for the left side of the notes view
     */
    private JPanel createNotesListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setPreferredSize(new Dimension(300, getHeight()));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        
        // Header with title and add button
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(SIDEBAR_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title and buttons panel
        JPanel titleButtonPanel = new JPanel(new BorderLayout(10, 0));
        titleButtonPanel.setBackground(SIDEBAR_COLOR);
        
        // Title label on the left
        JLabel titleLabel = new JLabel("Notes Manager");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        titleButtonPanel.add(titleLabel, BorderLayout.WEST);
        
        // Buttons panel for refresh and new
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setBackground(SIDEBAR_COLOR);
        
        // Refresh button
        JButton refreshButton = new JButton();
        refreshButton.setIcon(createRefreshIcon());
        refreshButton.setToolTipText("Refresh Notes");
        refreshButton.setFocusPainted(false);
        refreshButton.setContentAreaFilled(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> {
            loadNotes();
            JOptionPane.showMessageDialog(this, "Notes refreshed!", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Add button on the right
        JButton newButton = new JButton("+ New Note");
        newButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newButton.setFocusPainted(false);
        newButton.setBackground(PRIMARY_COLOR);
        newButton.setForeground(Color.WHITE);
        newButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        newButton.addActionListener(e -> {
            clearNotesFields();
            currentNote = null;
            showNotesDetailsView();
        });
        
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(newButton);
        titleButtonPanel.add(buttonsPanel, BorderLayout.EAST);
        
        headerPanel.add(titleButtonPanel, BorderLayout.NORTH);
        
        // Search field below the title
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(SIDEBAR_COLOR);
        searchPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchLabel, BorderLayout.WEST);
        
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        styleSearchField(searchField);
        searchField.putClientProperty("JTextField.placeholderText", "Search notes...");
        
        // Add document listener for search functionality
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterNotes(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterNotes(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterNotes(searchField.getText());
            }
        });
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Filter options
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(SIDEBAR_COLOR);
        
        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterPanel.add(filterLabel);
        
        JComboBox<Category> filterComboBox = new JComboBox<>();
        filterComboBox.addItem(new Category(0, "All Notes"));
        
        // Load categories
        List<Category> categories = CategoryController.getAllCategories();
        for (Category category : categories) {
            filterComboBox.addItem(category);
        }
        
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterComboBox.setPreferredSize(new Dimension(120, 25));
        filterComboBox.addActionListener(e -> {
            Category selectedCategory = (Category) filterComboBox.getSelectedItem();
            if (selectedCategory.getId() == 0) {
                loadNotes(); // Load all notes
            } else {
                loadNotesByCategory(selectedCategory.getId()); // Load notes by category
            }
        });
        
        filterPanel.add(filterComboBox);
        headerPanel.add(filterPanel, BorderLayout.SOUTH);
        
        // Notes list
        notesListModel = new DefaultListModel<>();
        notesList = new JList<>(notesListModel);
        notesList.setCellRenderer(new NoteListCellRenderer());
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Note selectedNote = notesList.getSelectedValue();
                if (selectedNote != null) {
                    displayNoteDetails(selectedNote);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(notesList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(SIDEBAR_COLOR);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Custom cell renderer for notes list
     */
    private class NoteListCellRenderer extends DefaultListCellRenderer {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Note) {
                Note note = (Note) value;
                
        JPanel panel = new JPanel(new BorderLayout());
                panel.setBackground(isSelected ? HOVER_COLOR : SIDEBAR_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(15, 10, 15, 10)
                ));
                
                JPanel headerRow = new JPanel(new BorderLayout());
                headerRow.setBackground(isSelected ? HOVER_COLOR : SIDEBAR_COLOR);
                
                JLabel titleLabel = new JLabel(note.getTitle());
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                titleLabel.setForeground(isSelected ? PRIMARY_COLOR : TEXT_COLOR);
                headerRow.add(titleLabel, BorderLayout.WEST);
                
                if (note.getCategory() != null) {
                    JPanel categoryWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                    categoryWrapper.setBackground(headerRow.getBackground());
                    
                    // Add bullet point icon
                    JLabel bulletIcon = new JLabel(createBulletIcon());
                    categoryWrapper.add(bulletIcon);
                    
                    JLabel categoryLabel = new JLabel(note.getCategory().getName());
                    categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    categoryLabel.setForeground(PRIMARY_COLOR);
                    categoryWrapper.add(categoryLabel);
                    
                    headerRow.add(categoryWrapper, BorderLayout.EAST);
                }
                
                panel.add(headerRow, BorderLayout.NORTH);
                
                // Preview of content
                String contentPreview = note.getContent();
                if (contentPreview != null && contentPreview.length() > 50) {
                    contentPreview = contentPreview.substring(0, 50) + "...";
                }
                
                JLabel previewText = new JLabel(contentPreview);
                previewText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                previewText.setForeground(Color.GRAY);
                
                panel.add(previewText, BorderLayout.CENTER);
                
                // Date at the bottom
                if (note.getCreatedDate() != null) {
                    JLabel dateLabel = new JLabel(dateFormat.format(note.getCreatedDate()));
                    dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    dateLabel.setForeground(new Color(160, 160, 160));
                    panel.add(dateLabel, BorderLayout.SOUTH);
                }
                
                setText("");
                setIcon(null);
                
                return panel;
            }
            
            return this;
        }
    }

    /**
     * Creates the note details panel for editing notes
     */
    private JPanel createNoteDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Note detail header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.setBackground(BACKGROUND_COLOR);
        
        JButton backButton = new JButton("← Back to Notes");
        backButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setForeground(PRIMARY_COLOR);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            showNotesListView();
        });
        
        JLabel headerLabel = new JLabel("Note Details");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(TEXT_COLOR);
        
        titlePanel.add(backButton, BorderLayout.WEST);
        titlePanel.add(headerLabel, BorderLayout.CENTER);
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        // Add panel to contain buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        
        JButton clearButton = new JButton("Clear Form");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clearButton.setFocusPainted(false);
        clearButton.setBackground(HOVER_COLOR);
        clearButton.setForeground(TEXT_COLOR);
        clearButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        clearButton.addActionListener(e -> clearNotesFields());
        
        JButton addButton = new JButton("Save Note");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setFocusPainted(false);
        addButton.setBackground(PRIMARY_COLOR);
        addButton.setForeground(Color.WHITE);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addButton.addActionListener(e -> saveNote());
        
        JButton deleteNoteButton = new JButton("Delete Note");
        deleteNoteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteNoteButton.setFocusPainted(false);
        deleteNoteButton.setBackground(new Color(220, 53, 69));
        deleteNoteButton.setForeground(Color.WHITE);
        deleteNoteButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        deleteNoteButton.addActionListener(e -> deleteNote());
        deleteNoteButton.setEnabled(false); // Only enable when a note is selected
        
        buttonsPanel.add(clearButton);
        buttonsPanel.add(addButton);
        buttonsPanel.add(deleteNoteButton);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);
        
        // Create a split panel for form and content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setResizeWeight(0.4); // Let the right panel get more space when resizing
        splitPane.setBackground(BACKGROUND_COLOR);
        
        // Set initial divider location to 40% for form fields, 60% for content
        splitPane.setDividerLocation(0.4);
        
        // Left panel for form fields
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 10, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        // Title field
        JLabel titleLabel = new JLabel("Title");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(titleLabel, gbc);
        
        gbc.gridy++;
        notesTitleField = new JTextField();
        notesTitleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notesTitleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(notesTitleField, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 10, 0);
        
        // Category selection
        JLabel categoryLabel = new JLabel("Category");
        categoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(categoryLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 15, 0);
        notesCategoryComboBox = new JComboBox<>();
        
        // Add "No Category" option
        notesCategoryComboBox.addItem(new Category(0, "No Category"));
        
        // Load categories
        List<Category> categories = CategoryController.getAllCategories();
        for (Category category : categories) {
            notesCategoryComboBox.addItem(category);
        }
        
        notesCategoryComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notesCategoryComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        formPanel.add(notesCategoryComboBox, gbc);
        
        // New Category section
        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 10, 0);
        
        JLabel newCategoryLabel = new JLabel("New Category");
        newCategoryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(newCategoryLabel, gbc);
        
        gbc.gridy++;
        
        JPanel newCategoryPanel = new JPanel(new BorderLayout(5, 0));
        newCategoryPanel.setBackground(BACKGROUND_COLOR);
        
        JTextField newCategoryField = new JTextField();
        newCategoryField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        newCategoryField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        JButton addCategoryButton = new JButton("Add");
        addCategoryButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addCategoryButton.setFocusPainted(false);
        addCategoryButton.setBackground(PRIMARY_COLOR);
        addCategoryButton.setForeground(Color.WHITE);
        addCategoryButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        addCategoryButton.addActionListener(e -> {
            String categoryName = newCategoryField.getText().trim();
            if (!categoryName.isEmpty()) {
                Category newCategory = CategoryController.createCategory(categoryName);
                if (newCategory != null) {
                    // Reload categories
                    notesCategoryComboBox.addItem(newCategory);
                    notesCategoryComboBox.setSelectedItem(newCategory);
                    newCategoryField.setText("");
                }
            }
        });
        
        newCategoryPanel.add(newCategoryField, BorderLayout.CENTER);
        newCategoryPanel.add(addCategoryButton, BorderLayout.EAST);
        
        formPanel.add(newCategoryPanel, gbc);
        
        // Right panel for content area
        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel contentLabel = new JLabel("Content");
        contentLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        contentPanel.add(contentLabel, BorderLayout.NORTH);
        
        notesContentArea = new JTextArea();
        notesContentArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        notesContentArea.setLineWrap(true);
        notesContentArea.setWrapStyleWord(true);
        
        JScrollPane contentScroll = new JScrollPane(notesContentArea);
        contentScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        contentPanel.add(contentScroll, BorderLayout.CENTER);
        
        // Add panels to split pane
        splitPane.setLeftComponent(formPanel);
        splitPane.setRightComponent(contentPanel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Load all notes for the current user
     */
    private void loadNotes() {
        notesListModel.clear();
        List<Note> notes = NoteController.getNotes(userId);
        for (Note note : notes) {
            notesListModel.addElement(note);
        }
    }

    /**
     * Load notes by category for the current user
     */
    private void loadNotesByCategory(int categoryId) {
        notesListModel.clear();
        List<Note> notes = NoteController.getNotesByCategory(userId, categoryId);
        for (Note note : notes) {
            notesListModel.addElement(note);
        }
    }

    /**
     * Filter notes by search text
     */
    private void filterNotes(String searchText) {
        if (searchText.isEmpty()) {
            loadNotes(); // Reload all notes
            return;
        }
        
        searchText = searchText.toLowerCase();
        DefaultListModel<Note> filteredModel = new DefaultListModel<>();
        List<Note> allNotes = NoteController.getNotes(userId);
        
        for (Note note : allNotes) {
            if (note.getTitle().toLowerCase().contains(searchText) || 
                (note.getContent() != null && note.getContent().toLowerCase().contains(searchText))) {
                filteredModel.addElement(note);
            }
        }
        
        notesListModel = filteredModel;
        notesList.setModel(notesListModel);
    }

    /**
     * Display the details of a selected note
     */
    private void displayNoteDetails(Note note) {
        currentNote = note;
        notesTitleField.setText(note.getTitle());
        notesContentArea.setText(note.getContent());
        
        // Set the category if available
        if (note.getCategory() != null) {
            for (int i = 0; i < notesCategoryComboBox.getItemCount(); i++) {
                Category category = notesCategoryComboBox.getItemAt(i);
                if (category.getId() == note.getCategory().getId()) {
                    notesCategoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            notesCategoryComboBox.setSelectedIndex(0); // No Category
        }
        
        // Enable delete button for notes panel
        // Find all components in the header panel's button panel
        JPanel notesPanel = (JPanel) mainContentPanel.getComponent(4);
        JPanel noteDetailsPanel = (JPanel) notesPanel.getComponent(1);  
        JPanel headerPanel = (JPanel) noteDetailsPanel.getComponent(0);
        
        // Find the delete button in the header panel's button panel and enable it
        Component[] headerComps = headerPanel.getComponents();
        for (Component comp : headerComps) {
            if (comp instanceof JPanel) {
                JPanel buttonsPanel = (JPanel) comp;
                Component[] buttons = buttonsPanel.getComponents();
                for (Component btn : buttons) {
                    if (btn instanceof JButton && ((JButton)btn).getText().equals("Delete Note")) {
                        btn.setEnabled(true);
                        break;
                    }
                }
            }
        }
        
        // Show the note details panel
        showNotesDetailsView();
    }
    
    /**
     * Shows the notes details view
     */
    private void showNotesDetailsView() {
        CardLayout cardLayout = (CardLayout) ((JPanel) mainContentPanel.getComponent(4)).getLayout();
        cardLayout.show((JPanel) mainContentPanel.getComponent(4), "NOTES_DETAILS");
    }
    
    /**
     * Shows the notes list view
     */
    private void showNotesListView() {
        CardLayout cardLayout = (CardLayout) ((JPanel) mainContentPanel.getComponent(4)).getLayout();
        cardLayout.show((JPanel) mainContentPanel.getComponent(4), "NOTES_LIST");
        notesList.clearSelection();
    }

    /**
     * Clear the note form fields
     */
    private void clearNotesFields() {
        notesTitleField.setText("");
        notesContentArea.setText("");
        notesCategoryComboBox.setSelectedIndex(0);
        currentNote = null;
        
        // Disable delete button for notes panel
        // Find all components in the header panel's button panel
        JPanel notesPanel = (JPanel) mainContentPanel.getComponent(4);
        JPanel noteDetailsPanel = (JPanel) notesPanel.getComponent(1);  
        JPanel headerPanel = (JPanel) noteDetailsPanel.getComponent(0);
        
        // Find the delete button in the header panel's button panel and disable it
        Component[] headerComps = headerPanel.getComponents();
        for (Component comp : headerComps) {
            if (comp instanceof JPanel) {
                JPanel buttonsPanel = (JPanel) comp;
                Component[] buttons = buttonsPanel.getComponents();
                for (Component btn : buttons) {
                    if (btn instanceof JButton && ((JButton)btn).getText().equals("Delete Note")) {
                        btn.setEnabled(false);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Save the current note (create new or update existing)
     */
    private void saveNote() {
        String title = notesTitleField.getText().trim();
        String content = notesContentArea.getText().trim();
        
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected category
        Category selectedCategory = (Category) notesCategoryComboBox.getSelectedItem();
        if (selectedCategory.getId() == 0) {
            selectedCategory = null; // No category selected
        }
        
        boolean success;
        
        if (currentNote == null) {
            // Create new note
            if (selectedCategory != null) {
                success = NoteController.createNote(userId, title, content, selectedCategory);
            } else {
                success = NoteController.createNote(userId, title, content);
            }
        } else {
            // Update existing note
            if (selectedCategory != null) {
                success = NoteController.updateNote(currentNote.getNoteId(), title, content, selectedCategory);
            } else {
                success = NoteController.updateNote(currentNote.getNoteId(), title, content);
            }
        }
        
        if (success) {
            loadNotes(); // Refresh the list
            JOptionPane.showMessageDialog(this, "Note saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            showNotesListView();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save note", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete the current note
     */
    private void deleteNote() {
        if (currentNote == null) {
            JOptionPane.showMessageDialog(this, "No note selected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this note?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (NoteController.deleteNote(currentNote.getNoteId())) {
                loadNotes(); // Refresh the list
                clearNotesFields();
                JOptionPane.showMessageDialog(this, "Note deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                showNotesListView();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete note", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Custom status icons for task list
    private Icon createStatusIcon(String status) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Convert status to lowercase for case-insensitive comparison
                String statusLower = status != null ? status.toLowerCase() : "";
                
                if ("completed".equals(statusLower)) {
                    // Checkmark icon
                    g2d.setColor(COMPLETED_COLOR);
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.drawLine(x + 3, y + 8, x + 6, y + 12);
                    g2d.drawLine(x + 6, y + 12, x + 13, y + 4);
                } else if ("in progress".equals(statusLower)) {
                    // Play/In Progress icon
                    g2d.setColor(IN_PROGRESS_COLOR);
                    int[] xPoints = {x + 4, x + 12, x + 4};
                    int[] yPoints = {y + 3, y + 8, y + 13};
                    g2d.fillPolygon(xPoints, yPoints, 3);
                } else {
                    // Pending/Empty circle icon - default for "pending" or any other status
                    g2d.setColor(PENDING_STATUS_COLOR);
                    g2d.setStroke(new BasicStroke(1.5f));
                    g2d.drawOval(x + 3, y + 3, 10, 10);
                }
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 16;
            }
            
            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    // Custom icon creation methods
    private Icon createRefreshIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(2f));
                
                // Draw circular refresh arrow
                g2d.drawArc(x + 2, y + 2, 16, 16, 45, 270);
                
                // Draw arrow head
                int[] xPoints = {x + 16, x + 20, x + 18};
                int[] yPoints = {y + 6, y + 10, y + 2};
                g2d.fillPolygon(xPoints, yPoints, 3);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 22;
            }
            
            @Override
            public int getIconHeight() {
                return 22;
            }
        };
    }
    
    private Icon createLightbulbIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw lightbulb body
                g2d.setColor(new Color(255, 204, 0)); // Yellow
                g2d.fillOval(x + 2, y, 10, 10);
                
                // Draw bulb rays
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawLine(x + 7, y - 3, x + 7, y - 1); // Top
                g2d.drawLine(x + 13, y + 5, x + 15, y + 5); // Right
                g2d.drawLine(x + 7, y + 12, x + 7, y + 14); // Bottom
                g2d.drawLine(x - 1, y + 5, x + 1, y + 5); // Left
                
                // Draw diagonal rays
                g2d.drawLine(x + 11, y + 1, x + 13, y - 1); // Top-right
                g2d.drawLine(x + 11, y + 9, x + 13, y + 11); // Bottom-right
                g2d.drawLine(x + 2, y + 9, x, y + 11); // Bottom-left
                g2d.drawLine(x + 2, y + 1, x, y - 1); // Top-left
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 16;
            }
            
            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }

    // Custom icon creation methods (Add these new methods with the other icon creation methods)
    
    private Icon createTaskIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(TEXT_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw clipboard outline
                g2d.drawRect(x + 2, y + 1, 12, 16);
                
                // Draw clip at top
                g2d.drawLine(x + 5, y, x + 5, y + 3);
                g2d.drawLine(x + 11, y, x + 11, y + 3);
                
                // Draw lines for text
                g2d.drawLine(x + 5, y + 7, x + 11, y + 7);
                g2d.drawLine(x + 5, y + 11, x + 11, y + 11);
                g2d.drawLine(x + 5, y + 15, x + 11, y + 15);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 18;
            }
            
            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }
    
    private Icon createPomodoroIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(TEXT_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw clock outline
                g2d.drawOval(x + 1, y + 1, 16, 16);
                
                // Draw clock center
                g2d.fillOval(x + 8, y + 8, 2, 2);
                
                // Draw hands
                g2d.drawLine(x + 9, y + 9, x + 9, y + 4);  // Minute hand
                g2d.drawLine(x + 9, y + 9, x + 14, y + 9); // Hour hand
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 18;
            }
            
            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }
    
    private Icon createDashboardIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(TEXT_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw four squares for dashboard
                g2d.drawRect(x + 2, y + 2, 6, 6);
                g2d.drawRect(x + 10, y + 2, 6, 6);
                g2d.drawRect(x + 2, y + 10, 6, 6);
                g2d.drawRect(x + 10, y + 10, 6, 6);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 18;
            }
            
            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }
    
    private Icon createCalendarIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(TEXT_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw calendar outline
                g2d.drawRect(x + 2, y + 3, 14, 14);
                
                // Draw top tabs
                g2d.drawLine(x + 6, y + 3, x + 6, y);
                g2d.drawLine(x + 12, y + 3, x + 12, y);
                
                // Draw horizontal lines for dates
                g2d.drawLine(x + 2, y + 8, x + 16, y + 8);
                g2d.drawLine(x + 2, y + 13, x + 16, y + 13);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 18;
            }
            
            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }
    
    private Icon createNoteIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(TEXT_COLOR);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw note shape
                g2d.drawRect(x + 2, y + 1, 14, 16);
                
                // Draw folded corner
                g2d.drawLine(x + 12, y + 1, x + 12, y + 5);
                g2d.drawLine(x + 12, y + 5, x + 16, y + 5);
                
                // Draw lines for text
                g2d.drawLine(x + 5, y + 8, x + 13, y + 8);
                g2d.drawLine(x + 5, y + 12, x + 13, y + 12);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 18;
            }
            
            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }
    
    private Icon createLogoutIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Door frame
                g2d.setColor(new Color(255, 89, 89));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(x + 2, y, x + 12, y + 16);
                
                // Door handle
                g2d.fillOval(x + 11, y + 7, 4, 4);
                
                // Arrow
                int[] xPoints = {x + 2, x + 2, x + 8};
                int[] yPoints = {y + 6, y + 10, y + 8};
                g2d.fillPolygon(xPoints, yPoints, 3);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 18;
            }
            
            @Override
            public int getIconHeight() {
                return 18;
            }
        };
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
        }
    }

    private Icon createBulletIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(PRIMARY_COLOR);
                g2d.fillOval(x + 1, y + 1, 4, 4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 6;
            }
            
            @Override
            public int getIconHeight() {
                return 6;
            }
        };
    }
    
    // Méthode pour créer une icône représentant le niveau de priorité
    private Icon createPriorityIcon(String priority) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Déterminer la couleur en fonction de la priorité
                Color color = NORMAL_PRIORITY_COLOR; // Couleur par défaut
                if (priority != null) {
                    if ("URGENT".equals(priority)) {
                        color = URGENT_PRIORITY_COLOR;
                    } else if ("HIGH".equals(priority)) {
                        color = HIGH_PRIORITY_COLOR;
                    } else if ("NORMAL".equals(priority)) {
                        color = NORMAL_PRIORITY_COLOR;
                    } else if ("LOW".equals(priority)) {
                        color = LOW_PRIORITY_COLOR;
                    }
                }
                
                // Dessiner un drapeau ou fanion pour la priorité
                g2d.setColor(color);
                int[] xPoints = {x, x + 10, x + 10, x};
                int[] yPoints = {y, y, y + 5, y + 10};
                g2d.fillPolygon(xPoints, yPoints, 4);
                
                g2d.dispose();
            }
            
            @Override
            public int getIconWidth() {
                return 12;
            }
            
            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }

    /**
     * Toggles the visibility of recurrence controls based on checkbox state
     */
    private void toggleRecurrenceControls() {
        recurrencePanel.setVisible(isRecurringCheckBox.isSelected());
        revalidate();
        repaint();
    }
}
        