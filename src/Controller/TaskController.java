package Controller;

import Model.Task;
import Model.TaskDAO;
import java.util.List;

public class TaskController {

    // Fetch tasks for a specific user
    public List<Task> getTasks(int userId) {
        return TaskDAO.getTasksByUserId(userId);
    }

    // Create a new task
    public boolean createTask(int userId, String title, String description, String status) {
        Task task = new Task(0, userId, title, description, null, status); // ID is 0, as it's auto-incremented
        return TaskDAO.insertTask(task);
    }

    // Update task status
    public boolean updateTaskStatus(int taskId, String status) {
        return TaskDAO.updateTaskStatus(taskId, status);
    }
}
