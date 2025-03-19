package View;

import Controller.TaskController;
import Model.Task;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

public class TaskFrame extends JFrame {
    private int userId; // Use user ID instead of username
    private TaskController taskController;
    private DefaultListModel<String> taskListModel;
    private JList<String> taskList;
    private JButton addButton, updateButton;
    private JTextField titleField, descriptionField;
    private JComboBox<String> statusComboBox;

    public TaskFrame(int userId) { // Constructor now receives userId
        this.userId = userId;
        this.taskController = new TaskController();

        setTitle("To-Do List - User " + userId);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        inputPanel.add(descriptionField);

        inputPanel.add(new JLabel("Status:"));
        statusComboBox = new JComboBox<>(new String[]{"pending", "in progress", "completed"});
        inputPanel.add(statusComboBox);

        addButton = new JButton("Add Task");
        updateButton = new JButton("Update Status");

        add(inputPanel, BorderLayout.NORTH);
        add(addButton, BorderLayout.WEST);
        add(updateButton, BorderLayout.EAST);

        loadTasks(); // Load tasks for the specific user

        addButton.addActionListener(new AddTaskAction());
        updateButton.addActionListener(new UpdateTaskAction());
    }

    private void loadTasks() {
        taskListModel.clear();
        List<Task> tasks = taskController.getTasks(userId); // Use the correct user ID
        for (Task task : tasks) {
            taskListModel.addElement(task.getTitle() + " - " + task.getStatus());
        }
    }

    private class AddTaskAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText();
            String description = descriptionField.getText();
            String status = (String) statusComboBox.getSelectedItem();

            if (taskController.createTask(userId, title, description, status)) { // Use correct user ID
                loadTasks();
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
                int taskId = taskController.getTasks(userId).get(selectedIndex).getTaskId(); // Get correct task ID
                if (taskController.updateTaskStatus(taskId, status)) { // Use correct task ID
                    loadTasks();
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update task!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
