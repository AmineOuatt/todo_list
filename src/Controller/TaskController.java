package Controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import Model.Category;
import Model.Task;
import Model.TaskDAO;

/**
 * Controller class for managing tasks
 */
public class TaskController {

    /**
     * Get all tasks for a user
     * @param userId The ID of the user
     * @return List of tasks for the user
     */
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

    /**
     * Create a new task
     * @param task The task to create
     * @return true if the creation was successful, false otherwise
     */
    public static boolean createTask(Task task) {
        return TaskDAO.insertTask(task);
    }
    
    // Create a new task with category
    public static boolean createTask(int userId, String title, String description, String status, Date date, Category category) {
        Task task = new Task(0, userId, title, description, date, status, category);
        return TaskDAO.insertTask(task);
    }

    // Create a new task without category
    public static boolean createTask(int userId, String title, String description, String status, Date date) {
        Task task = new Task(0, userId, title, description, date, status, null);
        return TaskDAO.insertTask(task);
    }

    /**
     * Update an existing task
     * @param task The task to update
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateTask(Task task) {
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

    // Update task without category
    public static boolean updateTask(int taskId, String title, String description, String status, Date date) {
        Task task = TaskDAO.getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDate(date);
        task.setCategory(null);
        
        return TaskDAO.updateTask(task);
    }

    /**
     * Delete a task
     * @param taskId The ID of the task to delete
     * @return true if the deletion was successful, false otherwise
     */
    public static boolean deleteTask(int taskId) {
        return TaskDAO.deleteTask(taskId);
    }

    /**
     * Update the status of a task
     * @param taskId The ID of the task to update
     * @param status The new status for the task
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateTaskStatus(int taskId, String status) {
        return TaskDAO.updateTaskStatus(taskId, status);
    }

    /**
     * Get a task by ID
     * @param taskId The ID of the task to retrieve
     * @return The task with the specified ID, or null if not found
     */
    public static Task getTaskById(int taskId) {
        return TaskDAO.getTaskById(taskId);
    }
    
    /**
     * Get tasks for a user filtered by category
     * @param userId The ID of the user
     * @param categoryId The ID of the category to filter by
     * @return List of tasks in the specified category
     */
    public static List<Task> getTasksByCategory(int userId, int categoryId) {
        return TaskDAO.getTasksByCategory(userId, categoryId);
    }
    
    /**
     * Update the category of a task
     * @param taskId The ID of the task to update
     * @param categoryId The ID of the new category, or null to remove the category
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateTaskCategory(int taskId, Integer categoryId) {
        return TaskDAO.updateTaskCategory(taskId, categoryId);
    }
}
