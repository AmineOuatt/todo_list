package View;

import Controller.TaskController;
import Model.Task;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class TaskFrame extends JFrame {
    private int userId;
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private JButton addButton, deleteButton;
    private JTextField titleField;
    private JTextArea descriptionField;
    private JComboBox<String> statusComboBox;
    private JSpinner dueDateSpinner;
    
    // Pomodoro components
    private Timer pomodoroTimer;
    private JLabel timerLabel;
    private JButton startPomodoroButton;
    private JButton resetPomodoroButton;
    private int remainingSeconds;
    private boolean isBreak = false;
    private int workDuration = 25 * 60; // 25 minutes in seconds
    private int breakDuration = 5 * 60; // 5 minutes in seconds
    
    // Modern Microsoft-style colors
    private static final Color PRIMARY_COLOR = new Color(42, 120, 255);    // Microsoft Blue
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243); // Light Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = new Color(225, 225, 225);
    private static final Color HOVER_COLOR = new Color(230, 240, 255);
    private static final Color COMPLETED_COLOR = new Color(76, 175, 80);   // Green
    private static final Color PENDING_COLOR = new Color(255, 152, 0);     // Orange
    private static final Color SIDEBAR_COLOR = new Color(250, 250, 250);

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

    public TaskFrame(int userId) {
        this.userId = userId;
        setTitle("Task Manager");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Initialize task list components
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new TaskListCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BACKGROUND_COLOR);

        // Create sidebar container
        JPanel sidebarContainer = new JPanel(new BorderLayout());
        sidebarContainer.setPreferredSize(new Dimension(250, getHeight()));
        sidebarContainer.setBackground(SIDEBAR_COLOR);
        sidebarContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Create navigation panel
        navigationPanel = createNavigationPanel();
        sidebarContainer.add(navigationPanel, BorderLayout.NORTH);

        // Create main content panel with CardLayout
        mainContentPanel = new JPanel(new CardLayout());
        mainContentPanel.setBackground(BACKGROUND_COLOR);

        // Create task details panel
        JPanel taskDetailsPanel = new JPanel(new BorderLayout());
        taskDetailsPanel.setBackground(BACKGROUND_COLOR);

        // Add task list panel
        JPanel taskListPanel = createTaskListPanel();
        taskDetailsPanel.add(taskListPanel, BorderLayout.WEST);

        // Add task details form
        JPanel detailsForm = createTaskDetailsPanel();
        taskDetailsPanel.add(detailsForm, BorderLayout.CENTER);

        // Add panels to card layout
        mainContentPanel.add(taskDetailsPanel, "TASKS");
        mainContentPanel.add(createPomodoroPanel(), "POMODORO");

        // Add sidebar and main content to main container
        mainContainer.add(sidebarContainer, BorderLayout.WEST);
        mainContainer.add(mainContentPanel, BorderLayout.CENTER);

        // Add main container to frame
        add(mainContainer);
        
        initializePomodoroTimer();
        loadTasks();
        updateDashboard();

        // Add selection listener for task list
        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Task selectedTask = taskList.getSelectedValue();
                if (selectedTask != null) {
                    titleField.setText(selectedTask.getTitle());
                    descriptionField.setText(selectedTask.getDescription());
                    statusComboBox.setSelectedItem(selectedTask.getStatus());
                    dueDateSpinner.setValue(selectedTask.getDueDate() != null ? 
                        selectedTask.getDueDate() : new Date());
                }
            }
        });
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tasks button
        JButton tasksButton = new JButton("📝 Tasks");
        styleButton(tasksButton, true);  // Start with Tasks selected
        tasksButton.addActionListener(e -> {
            showPanel("TASKS");
            updateSelectedButton(tasksButton);
        });

        // Pomodoro button
        pomodoroButton = new JButton("⏱️ Pomodoro");
        styleButton(pomodoroButton, false);
        pomodoroButton.addActionListener(e -> {
            showPanel("POMODORO");
            updateSelectedButton(pomodoroButton);
        });

        // Dashboard button
        dashboardButton = new JButton("📊 Dashboard");
        styleButton(dashboardButton, false);
        dashboardButton.addActionListener(e -> {
            new DashboardView(userId, this).setVisible(true);
            setVisible(false);
        });

        // Add buttons to panel with spacing
        panel.add(tasksButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(pomodoroButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(dashboardButton);
        panel.add(Box.createVerticalGlue());

        return panel;
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
        private Color[] colors = {COMPLETED_COLOR, PRIMARY_COLOR, PENDING_COLOR};

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
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Timer display panel
        JPanel timerPanel = new JPanel(new BorderLayout(10, 10));
        timerPanel.setBackground(Color.WHITE);

        // Create circular progress panel
        progressPanel = new CircularProgressPanel();
        progressPanel.setPreferredSize(new Dimension(200, 200));
        timerPanel.add(progressPanel, BorderLayout.CENTER);

        // Timer label
        timerLabel = new JLabel("25:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setForeground(TEXT_COLOR);
        progressPanel.add(timerLabel);

        // Control buttons panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.setBackground(Color.WHITE);

        startPomodoroButton = new JButton("Start");
        resetPomodoroButton = new JButton("Reset");
        styleButton(startPomodoroButton, true);
        styleButton(resetPomodoroButton, false);

        startPomodoroButton.addActionListener(e -> togglePomodoro());
        controlPanel.add(startPomodoroButton);

        controlPanel.add(Box.createHorizontalStrut(10));

        resetPomodoroButton = new JButton("Reset");
        styleButton(resetPomodoroButton, false);
        resetPomodoroButton.addActionListener(e -> resetPomodoro());
        controlPanel.add(resetPomodoroButton);

        panel.add(controlPanel);

        // Settings Panel
        JPanel settingsPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        settingsPanel.setBackground(SIDEBAR_COLOR);
        settingsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel workLabel = new JLabel("Work (min):");
        JSpinner workSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 60, 1));
        workSpinner.addChangeListener(e -> workDuration = (int)workSpinner.getValue() * 60);

        JLabel breakLabel = new JLabel("Break (min):");
        JSpinner breakSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        breakSpinner.addChangeListener(e -> breakDuration = (int)breakSpinner.getValue() * 60);

        settingsPanel.add(workLabel);
        settingsPanel.add(workSpinner);
        settingsPanel.add(breakLabel);
        settingsPanel.add(breakSpinner);

        panel.add(Box.createVerticalStrut(10));
        panel.add(settingsPanel);

        return panel;
    }

    private JPanel createCalendarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SIDEBAR_COLOR);
        panel.setBorder(new EmptyBorder(0, 20, 20, 20));

        JLabel titleLabel = new JLabel("Calendar");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        // Calendar navigation panel
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setBackground(SIDEBAR_COLOR);
        navigationPanel.setMaximumSize(new Dimension(200, 30));

        currentCalendar = Calendar.getInstance();
        monthLabel = new JLabel(monthFormat.format(currentCalendar.getTime()), SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton prevButton = new JButton("←");
        JButton nextButton = new JButton("→");
        styleButton(prevButton, false);
        styleButton(nextButton, false);

        prevButton.addActionListener(e -> changeMonth(-1));
        nextButton.addActionListener(e -> changeMonth(1));

        navigationPanel.add(prevButton, BorderLayout.WEST);
        navigationPanel.add(monthLabel, BorderLayout.CENTER);
        navigationPanel.add(nextButton, BorderLayout.EAST);

        panel.add(navigationPanel);
        panel.add(Box.createVerticalStrut(10));

        // Calendar grid
        calendarPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        calendarPanel.setBackground(SIDEBAR_COLOR);
        updateCalendar();
        
        panel.add(calendarPanel);
        return panel;
    }

    private void changeMonth(int delta) {
        currentCalendar.add(Calendar.MONTH, delta);
        monthLabel.setText(monthFormat.format(currentCalendar.getTime()));
        updateCalendar();
    }

    private void updateCalendar() {
        calendarPanel.removeAll();
        
        // Add day labels
        String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
        for (String day : days) {
            JLabel label = new JLabel(day, SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            calendarPanel.add(label);
        }

        // Get the first day of month and number of days
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty labels for days before the first day of month
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel());
        }

        // Add day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            styleCalendarDayButton(dayButton);
            calendarPanel.add(dayButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private void styleCalendarDayButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(TEXT_COLOR);
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(25, 25));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(HOVER_COLOR);
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Left side panel (Tasks and Statistics)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        // Recent Tasks Section
        JPanel tasksPanel = createSectionPanel("Recent Tasks", true);
        tasksPanel.add(createTaskList());
        leftPanel.add(tasksPanel);
        leftPanel.add(Box.createVerticalStrut(20));

        // Statistics Section
        JPanel statsPanel = createSectionPanel("Statistics", true);
        statsPanel.add(createStatisticsChart());
        leftPanel.add(statsPanel);

        // Right side panel (Folders and Notes)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);

        // Folders Section
        JPanel foldersPanel = createSectionPanel("Folders", true);
        foldersPanel.add(createFoldersGrid());
        rightPanel.add(foldersPanel);
        rightPanel.add(Box.createVerticalStrut(20));

        // Notes Section
        JPanel notesPanel = createSectionPanel("Notes", true);
        notesPanel.add(createNotesList());
        rightPanel.add(notesPanel);

        // Add panels to main dashboard
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setDividerSize(20);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
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

    private JPanel createTaskList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        List<Task> tasks = TaskController.getTasks(userId);
        for (Task task : tasks) {
            panel.add(createTaskItem(task));
            panel.add(Box.createVerticalStrut(10));
        }

        return panel;
    }

    private JPanel createTaskItem(Task task) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(Color.WHITE);
        checkbox.setSelected(task.getStatus().equalsIgnoreCase("completed"));
        
        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (task.getStatus().equalsIgnoreCase("completed")) {
            titleLabel.setForeground(Color.GRAY);
            // Add strikethrough effect for completed tasks
            titleLabel.setText("<html><strike>" + task.getTitle() + "</strike></html>");
        }

        JLabel dateLabel = new JLabel(formatDueDate(task.getDueDate()));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(150, 150, 150));

        panel.add(checkbox, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(dateLabel, BorderLayout.EAST);

        return panel;
    }

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

    private JPanel createNotesList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        // Sample notes - you can modify these or load from your data
        addNote(panel, "Bank working hours 09:00 - 19:00");
        addNote(panel, "To be, or not to be, that is the question:");
        addNote(panel, "Whether 'tis nobler in the mind...");

        return panel;
    }

    private void addNote(JPanel parent, String text) {
        JLabel noteLabel = new JLabel(text);
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noteLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        parent.add(noteLabel);
    }

    private JPanel createStatisticsChart() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Get task statistics for the week
                int[] dailyTasks = getWeeklyTaskStats();
                
                int width = getWidth() - 40;
                int height = getHeight() - 40;
                int x = 20;
                int y = height + 20;
                
                // Draw smooth line chart
                g2.setColor(new Color(100, 180, 255, 50));
                int[] xPoints = new int[dailyTasks.length];
                int[] yPoints = new int[dailyTasks.length];
                
                for (int i = 0; i < dailyTasks.length; i++) {
                    xPoints[i] = x + (i * width / (dailyTasks.length - 1));
                    yPoints[i] = y - (dailyTasks[i] * height / 20);
                }
                
                // Draw filled area
                int[] polygonX = new int[dailyTasks.length + 2];
                int[] polygonY = new int[dailyTasks.length + 2];
                
                // Start from bottom left
                polygonX[0] = x;
                polygonY[0] = y;
                
                // Add all points
                System.arraycopy(xPoints, 0, polygonX, 1, xPoints.length);
                System.arraycopy(yPoints, 0, polygonY, 1, yPoints.length);
                
                // End at bottom right
                polygonX[polygonX.length - 1] = x + width;
                polygonY[polygonY.length - 1] = y;
                
                g2.fillPolygon(polygonX, polygonY, polygonX.length);
                
                // Draw line
                g2.setColor(PRIMARY_COLOR);
                g2.setStroke(new BasicStroke(2f));
                for (int i = 0; i < dailyTasks.length - 1; i++) {
                    g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
                }
                
                // Draw points
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                for (int i = 0; i < dailyTasks.length; i++) {
                    g2.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
                    g2.setColor(PRIMARY_COLOR);
                    g2.drawOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
                }
                
                // Draw x-axis labels
                g2.setColor(TEXT_COLOR);
                String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                for (int i = 0; i < days.length; i++) {
                    g2.drawString(days[i], xPoints[i] - 15, y + 15);
                }
            }
        };
        
        panel.setPreferredSize(new Dimension(0, 200));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private int[] getWeeklyTaskStats() {
        // This should be implemented to get actual task statistics
        // For now, returning sample data
        return new int[]{8, 12, 15, 10, 14, 9, 11};
    }

    private void initializePomodoroTimer() {
        remainingSeconds = workDuration;
        pomodoroTimer = new Timer(1000, e -> updateTimer());
        updateTimerLabel();
    }

    private void togglePomodoro() {
        if (pomodoroTimer.isRunning()) {
            pomodoroTimer.stop();
            startPomodoroButton.setText("Start");
        } else {
            pomodoroTimer.start();
            startPomodoroButton.setText("Pause");
        }
    }

    private void resetPomodoro() {
        pomodoroTimer.stop();
        remainingSeconds = workDuration;
        isBreak = false;
        startPomodoroButton.setText("Start");
        updateTimerLabel();
    }

    private void updateTimer() {
        remainingSeconds--;
        if (remainingSeconds <= 0) {
            pomodoroTimer.stop();
            Toolkit.getDefaultToolkit().beep();
            
            if (isBreak) {
                remainingSeconds = workDuration;
                isBreak = false;
                JOptionPane.showMessageDialog(this, "Break is over! Time to work!");
            } else {
                remainingSeconds = breakDuration;
                isBreak = true;
                JOptionPane.showMessageDialog(this, "Time for a break!");
            }
            
            startPomodoroButton.setText("Start");
        }
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create a left panel for title
        JLabel titleLabel = new JLabel("Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);

        // Create a right panel for buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(CARD_COLOR);

        // Add button
        addButton = new JButton("+");
        styleButton(addButton, true);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        addButton.setPreferredSize(new Dimension(40, 40));
        addButton.addActionListener(e -> handleAddTask());

        // Logout button
        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton, false);
        logoutButton.addActionListener(e -> {
            dispose();
            new UserSelectionView(username -> new LoginView(username).setVisible(true)).setVisible(true);
        });

        rightPanel.add(logoutButton);
        rightPanel.add(addButton);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTaskDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Title
        JLabel titleLabel = new JLabel("Title");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));

        titleField = new JTextField();
        styleTextField(titleField);
        panel.add(titleField);
        panel.add(Box.createVerticalStrut(15));

        // Description
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_COLOR);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(5));

        descriptionField = new JTextArea();
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        styleTextArea(descriptionField);
        JScrollPane scrollPane = new JScrollPane(descriptionField);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(15));

        // Status
        JLabel statusLabel = new JLabel("Status");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_COLOR);
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(5));

        statusComboBox = new JComboBox<>(new String[]{"pending", "in progress", "completed"});
        styleComboBox(statusComboBox);
        panel.add(statusComboBox);
        panel.add(Box.createVerticalStrut(15));

        // Due Date
        JLabel dueDateLabel = new JLabel("Due Date");
        dueDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dueDateLabel.setForeground(TEXT_COLOR);
        panel.add(dueDateLabel);
        panel.add(Box.createVerticalStrut(5));

        dueDateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "MMM d, yyyy");
        dueDateSpinner.setEditor(dateEditor);
        styleDateSpinner(dueDateSpinner);
        panel.add(dueDateSpinner);
        panel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton saveButton = new JButton("Save Changes");
        styleButton(saveButton, true);
        saveButton.addActionListener(e -> handleUpdateTask());
        buttonPanel.add(saveButton);

        buttonPanel.add(Box.createHorizontalStrut(10));

        deleteButton = new JButton("Delete");
        styleButton(deleteButton, false);
        deleteButton.addActionListener(e -> handleDeleteTask());
        buttonPanel.add(deleteButton);

        panel.add(buttonPanel);

        return panel;
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

    private void styleTextArea(JTextArea area) {
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBackground(Color.WHITE);
        area.setForeground(TEXT_COLOR);
        area.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
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

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setPreferredSize(new Dimension(300, 35));
        comboBox.setMaximumSize(new Dimension(2000, 35));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(TEXT_COLOR);
        ((JComponent) comboBox.getRenderer()).setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleDateSpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(300, 35));
        spinner.setMaximumSize(new Dimension(2000, 35));
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(Color.WHITE);
            tf.setForeground(TEXT_COLOR);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }
    }

    private void loadTasks() {
        taskListModel.clear();
        List<Task> tasks = TaskController.getTasks(userId);
        for (Task task : tasks) {
            taskListModel.addElement(task);
        }
    }

    private void handleAddTask() {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String status = (String) statusComboBox.getSelectedItem();
        Date dueDate = (Date) dueDateSpinner.getValue();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a title for the task",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (TaskController.createTask(userId, title, description, status, dueDate)) {
            loadTasks();
            clearFields();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to create task",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) return;

        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();
        Date dueDate = (Date) dueDateSpinner.getValue();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a title for the task",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (TaskController.updateTask(selectedTask.getTaskId(), title, description, status, dueDate)) {
            loadTasks();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update task",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteTask() {
        Task selectedTask = taskList.getSelectedValue();
        if (selectedTask == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this task?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (TaskController.deleteTask(selectedTask.getTaskId())) {
                loadTasks();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete task",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        titleField.setText("");
        descriptionField.setText("");
        statusComboBox.setSelectedIndex(0);
        dueDateSpinner.setValue(new Date());
    }

    // Custom cell renderer for tasks
    private class TaskListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            Task task = (Task) value;
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 15, 10, 15));
            
            if (isSelected) {
                panel.setBackground(HOVER_COLOR);
                } else {
                panel.setBackground(CARD_COLOR);
            }

            // Title and status
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.setBackground(panel.getBackground());
            
            JLabel titleLabel = new JLabel(task.getTitle());
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            titleLabel.setForeground(TEXT_COLOR);
            titlePanel.add(titleLabel, BorderLayout.CENTER);

            // Status indicator
            JLabel statusLabel = new JLabel("●");
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            switch (task.getStatus().toLowerCase()) {
                case "completed":
                    statusLabel.setForeground(COMPLETED_COLOR);
                    break;
                case "in progress":
                    statusLabel.setForeground(PRIMARY_COLOR);
                    break;
                default:
                    statusLabel.setForeground(PENDING_COLOR);
            }
            titlePanel.add(statusLabel, BorderLayout.WEST);
            titlePanel.add(Box.createHorizontalStrut(10), BorderLayout.EAST);

            panel.add(titlePanel, BorderLayout.CENTER);

            // Due date
            if (task.getDueDate() != null) {
                JLabel dateLabel = new JLabel(String.format("%tB %<td", task.getDueDate()));
                dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                dateLabel.setForeground(new Color(117, 117, 117));
                panel.add(dateLabel, BorderLayout.EAST);
            }

            return panel;
        }
    }

    private JPanel createTaskListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Add header
        panel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Add task list with scroll
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(CARD_COLOR);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void showPanel(String name) {
        CardLayout cl = (CardLayout) mainContentPanel.getLayout();
        cl.show(mainContentPanel, name);
        
        // Update button states
        if (name.equals("TASKS")) {
            updateSelectedButton((JButton) navigationPanel.getComponent(0));
        } else if (name.equals("POMODORO")) {
            updateSelectedButton(pomodoroButton);
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
            g2.setColor(PRIMARY_COLOR);
            g2.drawArc(x, y, size, size, 90, -(int) (progress * 360));
        }
    }
}
