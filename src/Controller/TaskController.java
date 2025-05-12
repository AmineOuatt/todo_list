package Controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import Model.Category;
import Model.Task;
import Model.TaskDAO;

public class TaskController {

    // Fetch tasks for a specific user
    public static List<Task> getTasks(int userId) {
        return TaskDAO.getTasksByUserId(userId);
    }

    // Get tasks filtered by date range
    public static List<Task> getTasksByDateRange(int userId, Date fromDate, Date toDate) {
        List<Task> allTasks = TaskDAO.getTasksByUserId(userId);
        return allTasks.stream()
                .filter(task -> {
                    Date taskDate = task.getDate();
                    return taskDate != null && 
                           !taskDate.before(fromDate) && 
                           !taskDate.after(toDate);
                })
                .collect(Collectors.toList());
    }

    // Create a new task
    public static boolean createTask(int userId, String title, String description, String status, Date date) {
        Task task = new Task(0, userId, title, description, date, status);
        return TaskDAO.insertTask(task);
    }
    
    // Create a new task with category
    public static boolean createTask(int userId, String title, String description, String status, Date date, Category category) {
        Task task = new Task(0, userId, title, description, date, status, category);
        return TaskDAO.insertTask(task);
    }

    // Update task
    public static boolean updateTask(int taskId, String title, String description, String status, Date date) {
        Task task = TaskDAO.getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDate(date);
        
        return TaskDAO.updateTask(task);
    }
    
    // Update task with category
    public static boolean updateTask(int taskId, String title, String description, String status, Date date, Category category) {
        Task task = TaskDAO.getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDate(date);
        task.setCategory(category);
        
        return TaskDAO.updateTask(task);
    }

    // Delete task
    public static boolean deleteTask(int taskId) {
        return TaskDAO.deleteTask(taskId);
    }

    // Update task status
    public static boolean updateTaskStatus(int taskId, String status) {
        return TaskDAO.updateTaskStatus(taskId, status);
    }

    // Get task by ID
    public static Task getTaskById(int taskId) {
        return TaskDAO.getTaskById(taskId);
    }
    
    // Get tasks by category
    public static List<Task> getTasksByCategory(int userId, int categoryId) {
        return TaskDAO.getTasksByCategory(userId, categoryId);
    }
    
    // Update task category
    public static boolean updateTaskCategory(int taskId, Integer categoryId) {
        return TaskDAO.updateTaskCategory(taskId, categoryId);
    }
}
