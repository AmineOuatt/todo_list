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
        mainContentPanel.add(createDashboardPanel(), "DASHBOARD");

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
        panel.setBorder(new EmptyBorder(20, 15, 20, 15));

        // Add logo/brand at the top
        JLabel logoLabel = new JLabel("TaskFrame");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(PRIMARY_COLOR);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(logoLabel);
        panel.add(Box.createVerticalStrut(25));

        // Create navigation sections
        String[][] sections = {
            {"TASKS", "📝 All Tasks", "📅 Today", "⭐ Important"},
            {"LISTS", "📁 Personal", "💼 Work", "🎓 Study"},
            {"TOOLS", "⏱️ Pomodoro", "📊 Dashboard", "📈 Statistics"}
        };

        for (String[] section : sections) {
            // Add section header
            JLabel sectionLabel = new JLabel(section[0]);
            sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            sectionLabel.setForeground(new Color(145, 145, 145));
            sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(sectionLabel);
            panel.add(Box.createVerticalStrut(10));

            // Add section items
            for (int i = 1; i < section.length; i++) {
                JButton button = new JButton(section[i]);
                styleNavigationButton(button);
                panel.add(button);
                panel.add(Box.createVerticalStrut(5));
            }
            panel.add(Box.createVerticalStrut(20));
        }

        panel.add(Box.createVerticalGlue());

        // Add separator before logout
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(SIDEBAR_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        panel.add(separator);
        panel.add(Box.createVerticalStrut(15));

        // Add logout button at the bottom
        JButton logoutButton = new JButton();
        logoutButton.setLayout(new BorderLayout(10, 0));
        
        // Create door icon panel
        JPanel doorIconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Door frame
                g2d.setColor(new Color(255, 89, 89));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRect(2, 0, 12, 16);
                
                // Door handle
                g2d.fillOval(11, 7, 4, 4);
                
                // Arrow
                int[] xPoints = {2, 2, 8};
                int[] yPoints = {6, 10, 8};
                g2d.fillPolygon(xPoints, yPoints, 3);
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(18, 16);
            }
        };
        doorIconPanel.setOpaque(false);
        
        // Create text label
        JLabel textLabel = new JLabel("Logout");
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textLabel.setForeground(new Color(255, 89, 89));
        
        // Add components to button
        JPanel buttonContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonContent.setOpaque(false);
        buttonContent.add(doorIconPanel);
        buttonContent.add(textLabel);
        
        logoutButton.add(buttonContent, BorderLayout.CENTER);
        styleNavigationButton(logoutButton);
        
        // Override the default style for logout button
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        logoutButton.addActionListener(e -> {
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
        
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(new Color(255, 235, 235)); // Light red background on hover
                buttonContent.setBackground(new Color(255, 235, 235));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(SIDEBAR_COLOR);
                buttonContent.setBackground(SIDEBAR_COLOR);
            }
        });

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
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Top section with title and actions
        JPanel topSection = new JPanel(new BorderLayout(15, 0));
        topSection.setBackground(BACKGROUND_COLOR);
        
        // Title field
        titleField = new JTextField();
        titleField.setFont(new Font("Segoe UI", Font.BOLD, 20));
        styleTextField(titleField);
        topSection.add(titleField, BorderLayout.CENTER);

        // Action buttons
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtons.setBackground(BACKGROUND_COLOR);
        
        addButton = new JButton("Save");
        deleteButton = new JButton("Delete");
        styleButton(addButton, true);
        styleButton(deleteButton, false);
        
        actionButtons.add(deleteButton);
        actionButtons.add(addButton);
        topSection.add(actionButtons, BorderLayout.EAST);

        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Description section
        JPanel descriptionSection = createDetailSection("Description");
        descriptionField = new JTextArea(5, 20);
        styleTextArea(descriptionField);
        JScrollPane descScrollPane = new JScrollPane(descriptionField);
        descScrollPane.setBorder(null);
        descriptionSection.add(descScrollPane);
        contentPanel.add(descriptionSection);
        contentPanel.add(Box.createVerticalStrut(20));

        // Due date section
        JPanel dateSection = createDetailSection("Due Date");
        dueDateSpinner = new JSpinner(new SpinnerDateModel());
        styleDateSpinner(dueDateSpinner);
        dateSection.add(dueDateSpinner);
        contentPanel.add(dateSection);
        contentPanel.add(Box.createVerticalStrut(20));

        // Status section
        JPanel statusSection = createDetailSection("Status");
        String[] statuses = {"Not Started", "In Progress", "Completed"};
        statusComboBox = new JComboBox<>(statuses);
        styleComboBox(statusComboBox);
        statusSection.add(statusComboBox);
        contentPanel.add(statusSection);

        // Add components to main panel
        panel.add(topSection, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
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
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            
            if (isSelected) {
                panel.setBackground(HOVER_COLOR);
            } else {
                panel.setBackground(BACKGROUND_COLOR);
            }

            // Checkbox for completion status
            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected("Completed".equals(task.getStatus()));
            checkbox.setBackground(panel.getBackground());
            panel.add(checkbox, BorderLayout.WEST);

            // Task details panel
            JPanel detailsPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            detailsPanel.setBackground(panel.getBackground());

            // Task title
            JLabel titleLabel = new JLabel(task.getTitle());
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            titleLabel.setForeground(TEXT_COLOR);
            
            // Task metadata (due date, etc)
            JLabel metaLabel = new JLabel(formatDueDate(task.getDueDate()));
            metaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            metaLabel.setForeground(new Color(145, 145, 145));

            detailsPanel.add(titleLabel);
            detailsPanel.add(metaLabel);
            panel.add(detailsPanel, BorderLayout.CENTER);

            return panel;
        }
    }

    private JPanel createTaskListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 1));

        // Create header with search
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // Add search field
        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 35));
        styleSearchField(searchField);
        headerPanel.add(searchField, BorderLayout.CENTER);

        // Task list with custom renderer
        taskList.setBackground(BACKGROUND_COLOR);
        taskList.setBorder(null);
        taskList.setFixedCellHeight(60);
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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

    private void showPanel(String name) {
        CardLayout cl = (CardLayout) mainContentPanel.getLayout();
        cl.show(mainContentPanel, name);
        
        // Update button states
        switch (name) {
            case "TASKS":
                updateSelectedButton((JButton) navigationPanel.getComponent(0));
                break;
            case "POMODORO":
                updateSelectedButton(pomodoroButton);
                break;
            case "DASHBOARD":
                updateSelectedButton(dashboardButton);
                updateDashboard(); // Refresh dashboard data
                break;
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

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

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
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));

        // Checkbox
        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(CARD_COLOR);
        checkbox.setSelected(task.getStatus().equalsIgnoreCase("completed"));
        panel.add(checkbox, BorderLayout.WEST);

        // Task details
        JPanel details = new JPanel(new BorderLayout(5, 0));
        details.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (task.getStatus().equalsIgnoreCase("completed")) {
            titleLabel.setText("<html><strike>" + task.getTitle() + "</strike></html>");
            titleLabel.setForeground(new Color(158, 158, 158));
        } else {
            titleLabel.setForeground(TEXT_COLOR);
        }
        details.add(titleLabel, BorderLayout.CENTER);

        if (task.getDueDate() != null) {
            JLabel dateLabel = new JLabel(formatDueDate(task.getDueDate()));
            dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateLabel.setForeground(new Color(158, 158, 158));
            details.add(dateLabel, BorderLayout.EAST);
        }

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
}
