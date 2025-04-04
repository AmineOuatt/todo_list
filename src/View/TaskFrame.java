package View;

import Controller.TaskController;
import Model.Task;
import java.awt.*;
import java.awt.event.*;
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
    
    // Modern Microsoft-style colors
    private static final Color PRIMARY_COLOR = new Color(42, 120, 255);    // Microsoft Blue
    private static final Color BACKGROUND_COLOR = new Color(243, 243, 243); // Light Gray
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(33, 33, 33);
    private static final Color BORDER_COLOR = new Color(225, 225, 225);
    private static final Color HOVER_COLOR = new Color(230, 240, 255);
    private static final Color COMPLETED_COLOR = new Color(76, 175, 80);   // Green
    private static final Color PENDING_COLOR = new Color(255, 152, 0);     // Orange

    public TaskFrame(int userId) {
        this.userId = userId;
        setTitle("My Tasks");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        // Create split pane for task list and details
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(300);

        // Left panel - Task List
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(CARD_COLOR);
        leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        headerPanel.add(createHeaderPanel(), BorderLayout.CENTER);
        leftPanel.add(headerPanel, BorderLayout.NORTH);

        // Task list
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new TaskListCellRenderer());
        taskList.setBackground(CARD_COLOR);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane listScrollPane = new JScrollPane(taskList);
        listScrollPane.setBorder(null);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Right panel - Task Details
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        rightPanel.add(createTaskDetailsPanel(), BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane);
        loadTasks();

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

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);

        addButton = new JButton("+");
        styleButton(addButton, true);
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        addButton.setPreferredSize(new Dimension(40, 40));
        addButton.addActionListener(e -> handleAddTask());
        panel.add(addButton, BorderLayout.EAST);

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
}
