package View;

import Controller.TaskController;
import Model.Task;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class TaskFrame extends JFrame {
    private int userId;
    private TaskController taskController;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    private JButton addButton, updateButton;
    private JTextField titleField, descriptionField;
    private JComboBox<String> statusComboBox;
    private static final Color BRIGHT_GREEN = new Color(46, 204, 113);
    private static final Color DARK_GREEN = new Color(39, 174, 96);
    private static final Color DARK_BLACK = new Color(20, 20, 20);
    private static final Color LIGHT_GREEN = new Color(88, 214, 141);
    private static final Color DARKER_GREEN = new Color(34, 139, 34);
    private static final Color LIGHT_GRAY = new Color(180, 180, 180);

    public TaskFrame(int userId) {
        this.userId = userId;
        this.taskController = new TaskController();

        setTitle("To-Do List App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(DARK_BLACK);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(DARK_BLACK);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(DARK_BLACK);
        JLabel titleLabel = new JLabel("My Tasks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(BRIGHT_GREEN);
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // User info panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setBackground(DARK_BLACK);
        JLabel userLabel = new JLabel("User ID: " + userId);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        userLabel.setForeground(LIGHT_GREEN);
        userPanel.add(userLabel);
        mainPanel.add(userPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBackground(DARK_BLACK);

        // Title input
        JLabel titleInputLabel = new JLabel("Title:");
        titleInputLabel.setForeground(LIGHT_GREEN);
        titleInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(titleInputLabel);
        titleField = new JTextField();
        styleTextField(titleField);
        inputPanel.add(titleField);

        // Description input
        JLabel descInputLabel = new JLabel("Description:");
        descInputLabel.setForeground(LIGHT_GREEN);
        descInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(descInputLabel);
        descriptionField = new JTextField();
        styleTextField(descriptionField);
        inputPanel.add(descriptionField);

        // Status input
        JLabel statusInputLabel = new JLabel("Status:");
        statusInputLabel.setForeground(LIGHT_GREEN);
        statusInputLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(statusInputLabel);
        statusComboBox = new JComboBox<>(new String[]{"pending", "in progress", "completed"});
        styleComboBox(statusComboBox);
        inputPanel.add(statusComboBox);

        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(20));

        // Task list
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setBackground(DARKER_GREEN);
        taskList.setForeground(Color.WHITE);
        taskList.setFont(new Font("Arial", Font.PLAIN, 14));
        taskList.setSelectionBackground(BRIGHT_GREEN);
        taskList.setSelectionForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBackground(DARK_BLACK);
        scrollPane.getViewport().setBackground(DARKER_GREEN);
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(DARK_BLACK);

        addButton = new JButton("Add Task");
        updateButton = new JButton("Update Status");
        styleButton(addButton);
        styleButton(updateButton);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        mainPanel.add(buttonPanel);

        loadTasks();

        addButton.addActionListener(new AddTaskAction());
        updateButton.addActionListener(new UpdateTaskAction());

        add(mainPanel);
    }

    private void styleTextField(JTextField textField) {
        textField.setBackground(DARKER_GREEN);
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(LIGHT_GREEN);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setPreferredSize(new Dimension(200, 35));
        textField.setBorder(new LineBorder(BRIGHT_GREEN, 1));
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(DARKER_GREEN);
        comboBox.setForeground(Color.WHITE);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setPreferredSize(new Dimension(200, 35));
        comboBox.setBorder(new LineBorder(BRIGHT_GREEN, 1));
    }

    private void styleButton(JButton button) {
        button.setBackground(BRIGHT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(LIGHT_GREEN);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BRIGHT_GREEN);
            }
        });
    }

    private void loadTasks() {
        taskListModel.clear();
        List<Task> tasks = taskController.getTasks(userId);
        for (Task task : tasks) {
            taskListModel.addElement(task.getTitle() + " - " + task.getStatus());
        }
    }

    private class AddTaskAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText();
            String description = descriptionField.getText();
            String status = (String) statusComboBox.getSelectedItem();

            if (taskController.createTask(userId, title, description, status)) {
                loadTasks();
                titleField.setText("");
                descriptionField.setText("");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to add task!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class UpdateTaskAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                String status = (String) statusComboBox.getSelectedItem();
                int taskId = taskController.getTasks(userId).get(selectedIndex).getTaskId();
                if (taskController.updateTaskStatus(taskId, status)) {
                    loadTasks();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update task!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
