package View;

import Controller.TaskController;
import Model.Task;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardView extends JFrame {
    private int userId;
    private TaskFrame parentFrame;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(42, 120, 255);
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = new Color(225, 225, 225);
    private static final Color COMPLETED_COLOR = new Color(76, 175, 80);
    private static final Color PENDING_COLOR = new Color(255, 152, 0);

    public DashboardView(int userId, TaskFrame parentFrame) {
        this.userId = userId;
        this.parentFrame = parentFrame;
        
        setTitle("Dashboard");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);

        // Create main container
        JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
        mainContainer.setBackground(BACKGROUND_COLOR);
        mainContainer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add back button in header
        JPanel headerPanel = createHeaderPanel();
        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // Create content panel with left and right sections
        JSplitPane contentPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        contentPane.setBorder(null);
        contentPane.setDividerSize(20);
        contentPane.setDividerLocation(480);
        contentPane.setResizeWeight(0.5);

        // Left panel (Tasks and Statistics)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(BACKGROUND_COLOR);

        // Recent Tasks Section
        JPanel tasksPanel = createSectionPanel("Recent Tasks", true);
        tasksPanel.add(createTaskList());
        leftPanel.add(tasksPanel);
        leftPanel.add(Box.createVerticalStrut(20));

        // Statistics Section
        JPanel statsPanel = createSectionPanel("Statistics", true);
        statsPanel.add(createStatisticsChart());
        leftPanel.add(statsPanel);

        // Right panel (Folders and Notes)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(BACKGROUND_COLOR);

        // Folders Section
        JPanel foldersPanel = createSectionPanel("Folders", true);
        foldersPanel.add(createFoldersGrid());
        rightPanel.add(foldersPanel);
        rightPanel.add(Box.createVerticalStrut(20));

        // Notes Section
        JPanel notesPanel = createSectionPanel("Notes", true);
        notesPanel.add(createNotesList());
        rightPanel.add(notesPanel);

        contentPane.setLeftComponent(leftPanel);
        contentPane.setRightComponent(rightPanel);
        mainContainer.add(contentPane, BorderLayout.CENTER);

        add(mainContainer);

        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                parentFrame.setVisible(true);
                dispose();
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // Back button
        JButton backButton = new JButton("← Back");
        styleButton(backButton, false);
        backButton.addActionListener(e -> {
            parentFrame.setVisible(true);
            dispose();
        });

        // Title
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);

        panel.add(backButton, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);

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
        addNote(panel, "Meeting notes for project review");
        addNote(panel, "Remember to update documentation");
        addNote(panel, "Follow up with team members");

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

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 200);
            }
        };
        
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private int[] getWeeklyTaskStats() {
        List<Task> tasks = TaskController.getTasks(userId);
        int[] dailyTasks = new int[7];
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        for (Task task : tasks) {
            Date taskDate = task.getDueDate();
            if (taskDate != null) {
                Calendar taskCal = Calendar.getInstance();
                taskCal.setTime(taskDate);
                if (taskCal.get(Calendar.WEEK_OF_YEAR) == cal.get(Calendar.WEEK_OF_YEAR)) {
                    int dayIndex = taskCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
                    if (dayIndex >= 0 && dayIndex < 7) {
                        dailyTasks[dayIndex]++;
                    }
                }
            }
        }
        
        return dailyTasks;
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
                    button.setBackground(new Color(245, 245, 245));
                }
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(isPrimary ? PRIMARY_COLOR : Color.WHITE);
            }
        });
    }
}