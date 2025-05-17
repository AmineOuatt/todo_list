package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import Controller.TaskController;
import Model.Task;

/**
 * CalendarPanel class for displaying a monthly calendar view with tasks
 */
public class CalendarPanel extends JPanel {
    // Modern Microsoft-style colors
    private static final Color PRIMARY_COLOR = new Color(77, 100, 255);    // TickTick Blue
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255); // White
    private static final Color BORDER_COLOR = new Color(233, 234, 236);    // Light Border
    private static final Color TEXT_COLOR = new Color(37, 38, 43);         // Dark Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color COMPLETED_COLOR = new Color(52, 199, 89);   // Green
    private static final Color IN_PROGRESS_COLOR = new Color(255, 149, 0); // Orange for "In Progress"
    private static final Color PENDING_STATUS_COLOR = new Color(149, 149, 149); // Gray for "Pending"
    private static final Color URGENT_PRIORITY_COLOR = new Color(255, 59, 48);  // Red
    private static final Color HIGH_PRIORITY_COLOR = new Color(255, 149, 0);    // Orange
    private static final Color NORMAL_PRIORITY_COLOR = new Color(0, 122, 255);  // Blue
    private static final Color LOW_PRIORITY_COLOR = new Color(142, 142, 147);   // Gray
    
    // Calendar components
    private Calendar currentCalendar;
    private JLabel monthLabel;
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy");
    private JPanel calendarGrid;
    private int userId;
    private Consumer<Task> taskSelectedCallback;

    /**
     * Constructor for CalendarPanel
     * @param userId The ID of the current user
     * @param taskSelectedCallback Callback function when a task is selected
     */
    public CalendarPanel(int userId, Consumer<Task> taskSelectedCallback) {
        this.userId = userId;
        this.taskSelectedCallback = taskSelectedCallback;
        
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
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
        add(headerPanel, BorderLayout.NORTH);
        
        // Calendar grid
        calendarGrid = new JPanel(new GridLayout(6, 7));
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
        
        add(calendarGrid, BorderLayout.CENTER);
    }
    
    /**
     * Updates the calendar display with the current month
     */
    public void updateCalendar() {
        monthLabel.setText(monthFormat.format(currentCalendar.getTime()));
        
        // Get tasks for the month INCLUDING generated occurrences
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Determine first day of month and adjust for week display
        int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK) - 1;
        
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
                        
                        // Limit to 3 tasks displayed per day
                        if (tasksPanel.getComponentCount() < 3) {
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
                            
                            // Create the task label with truncated text if needed
                            String taskTitle = task.getTitle();
                            if (taskTitle.length() > 15) {
                                taskTitle = taskTitle.substring(0, 12) + "...";
                            }
                            
                            JLabel taskLabel = new JLabel(taskTitle);
                            taskLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                            taskLabel.setForeground(TEXT_COLOR);
                            taskLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                            
                            // Add a background color based on status (with transparency)
                            Color bgColor = new Color(
                                taskColor.getRed(),
                                taskColor.getGreen(),
                                taskColor.getBlue(),
                                30); // Light transparency
                            taskPanel.setBackground(bgColor);
                            
                            // Create a panel for the task label and priority icon (if needed)
                            JPanel labelPanel = new JPanel(new BorderLayout(5, 0));
                            labelPanel.setBackground(taskPanel.getBackground());
                            labelPanel.add(taskLabel, BorderLayout.CENTER);
                            
                            // Add priority icon for URGENT or HIGH priority tasks
                            if (priority != null && ("URGENT".equals(priority) || "HIGH".equals(priority))) {
                                JLabel priorityIcon = new JLabel("!");
                                priorityIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
                                priorityIcon.setForeground("URGENT".equals(priority) ? URGENT_PRIORITY_COLOR : HIGH_PRIORITY_COLOR);
                                labelPanel.add(priorityIcon, BorderLayout.EAST);
                            }
                            
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
                                    if (freshTask != null && taskSelectedCallback != null) {
                                        taskSelectedCallback.accept(freshTask);
                                    }
                                }
                            });
                            
                            // Add task to the day cell
                            tasksPanel.add(taskPanel);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error displaying task in calendar: " + e.getMessage());
                }
            }
            
            // Count the total tasks for this day
            int taskCount = 0;
            for (Task task : tasksWithOccurrences) {
                if (task.getDueDate() == null) continue;
                
                Calendar taskDate = Calendar.getInstance();
                taskDate.setTime(task.getDueDate());
                
                if (taskDate.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    taskDate.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                    taskDate.get(Calendar.DAY_OF_MONTH) == day) {
                    taskCount++;
                }
            }
            
            // Add "+X more" indicator if there are more than 3 tasks
            if (taskCount > 3) {
                JLabel moreLabel = new JLabel("+" + (taskCount - 3) + " more");
                moreLabel.setFont(new Font("Segoe UI", Font.ITALIC, 9));
                moreLabel.setForeground(PRIMARY_COLOR);
                moreLabel.setHorizontalAlignment(JLabel.CENTER);
                moreLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                tasksPanel.add(moreLabel);
            }
            
            // Add a subtle background color to cells with tasks
            if (taskCount > 0) {
                // Create a very subtle background color
                Color highlightColor = new Color(245, 248, 255); // Very light blue
                dayCell.setBackground(highlightColor);
                tasksPanel.setBackground(highlightColor);
                
                // Add an indicator dot in the date label for days with tasks
                if (taskCount > 0) {
                    dateLabel.setText(dateLabel.getText() + " •");
                    dateLabel.setForeground(PRIMARY_COLOR);
                }
            }
            
            tasksPanel.revalidate();
            tasksPanel.repaint();
        }
        
        // Ensure the entire calendar is properly refreshed
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }
} 