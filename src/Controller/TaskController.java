package Controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Calendar;

import Model.Category;
import Model.Task;
import Model.TaskDAO;
import Model.User;
import Model.RecurringPattern;

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
    
    /**
     * Alias method for CollaborationView
     * Get all tasks for a user
     * @param userId The ID of the user
     * @return List of tasks for the user
     */
    public static List<Task> getTasksByUser(int userId) {
        return getTasks(userId);
    }

    /**
     * Get all tasks for a user including generated occurrences of recurring tasks
     * @param userId The ID of the user
     * @return List of tasks for the user including recurring occurrences
     */
    public static List<Task> getTasksWithOccurrences(int userId) {
        return TaskDAO.getTasksWithOccurrences(userId);
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

    // Create a new task with category and priority
    public static boolean createTask(int userId, String title, String description, String status, Date date, Category category, String priority) {
        Task task = new Task(0, userId, title, description, date, status, category);
        task.setPriority(priority);
        return TaskDAO.insertTask(task);
    }

    // Create a new task without category
    public static boolean createTask(int userId, String title, String description, String status, Date date) {
        Task task = new Task(0, userId, title, description, date, status, null);
        return TaskDAO.insertTask(task);
    }

    // Create a new task without category but with priority
    public static boolean createTask(int userId, String title, String description, String status, Date date, String priority) {
        Task task = new Task(0, userId, title, description, date, status, null);
        task.setPriority(priority);
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

    // Update task with category and priority
    public static boolean updateTask(int taskId, String title, String description, String status, Date date, Category category, String priority) {
        Task task = TaskDAO.getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDate(date);
        task.setCategory(category);
        task.setPriority(priority);
        
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

    // Update task without category but with priority
    public static boolean updateTask(int taskId, String title, String description, String status, Date date, String priority) {
        Task task = TaskDAO.getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDate(date);
        task.setCategory(null);
        task.setPriority(priority);
        
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

    // Create a new recurring task (without category)
    public static boolean createTask(int userId, String title, String description, String status, Date dueDate,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate, 
                                    Integer maxOccurrences, Integer dayOfWeek, Integer dayOfMonth, Integer monthOfYear) {
        Task task = new Task(0, userId, title, description, dueDate, status);
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
            task.setMaxOccurrences(maxOccurrences);
            task.setDayOfWeek(dayOfWeek);
            task.setDayOfMonth(dayOfMonth);
            task.setMonthOfYear(monthOfYear);
        }
        return TaskDAO.insertTask(task);
    }

    // Create a new recurring task (without category) with priority
    public static boolean createTask(int userId, String title, String description, String status, Date dueDate,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate, 
                                    Integer maxOccurrences, Integer dayOfWeek, Integer dayOfMonth, Integer monthOfYear,
                                    String priority) {
        Task task = new Task(0, userId, title, description, dueDate, status);
        task.setPriority(priority);
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
            task.setMaxOccurrences(maxOccurrences);
            task.setDayOfWeek(dayOfWeek);
            task.setDayOfMonth(dayOfMonth);
            task.setMonthOfYear(monthOfYear);
        }
        return TaskDAO.insertTask(task);
    }
    
    // Create a new recurring task (with category)
    public static boolean createTask(int userId, String title, String description, String status, Date dueDate, Category category,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate, 
                                    Integer maxOccurrences, Integer dayOfWeek, Integer dayOfMonth, Integer monthOfYear) {
        Task task = new Task(0, userId, title, description, dueDate, status, category);
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
            task.setMaxOccurrences(maxOccurrences);
            task.setDayOfWeek(dayOfWeek);
            task.setDayOfMonth(dayOfMonth);
            task.setMonthOfYear(monthOfYear);
        }
        return TaskDAO.insertTask(task);
    }
    
    // Create a new recurring task (with category) with priority
    public static boolean createTask(int userId, String title, String description, String status, Date dueDate, Category category,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate, 
                                    Integer maxOccurrences, Integer dayOfWeek, Integer dayOfMonth, Integer monthOfYear,
                                    String priority) {
        Task task = new Task(0, userId, title, description, dueDate, status, category);
        task.setPriority(priority);
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
            task.setMaxOccurrences(maxOccurrences);
            task.setDayOfWeek(dayOfWeek);
            task.setDayOfMonth(dayOfMonth);
            task.setMonthOfYear(monthOfYear);
        }
        return TaskDAO.insertTask(task);
    }
    
    // Update recurring task (without category)
    public static boolean updateTask(int taskId, String title, String description, String status, Date dueDate,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate) {
        Task task = getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCategory(null);
        
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
        } else {
            task.setRecurrenceType(null);
            task.setRecurrenceInterval(0);
            task.setRecurrenceEndDate(null);
            task.setRecurringPatternId(null);
        }
        
        return TaskDAO.updateTask(task);
    }
    
    // Update recurring task (without category) with priority
    public static boolean updateTask(int taskId, String title, String description, String status, Date dueDate,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate,
                                    String priority) {
        Task task = getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCategory(null);
        task.setPriority(priority);
        
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
        } else {
            task.setRecurrenceType(null);
            task.setRecurrenceInterval(0);
            task.setRecurrenceEndDate(null);
            task.setRecurringPatternId(null);
        }
        
        return TaskDAO.updateTask(task);
    }

    // Update recurring task (with category)
    public static boolean updateTask(int taskId, String title, String description, String status, Date dueDate, Category category,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate) {
        Task task = getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCategory(category);
        
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
        } else {
            task.setRecurrenceType(null);
            task.setRecurrenceInterval(0);
            task.setRecurrenceEndDate(null);
            task.setRecurringPatternId(null);
        }
        
        return TaskDAO.updateTask(task);
    }

    // Update recurring task (with category) with priority
    public static boolean updateTask(int taskId, String title, String description, String status, Date dueDate, Category category,
                                    boolean isRecurring, String recurrenceType, int recurrenceInterval, Date recurrenceEndDate,
                                    String priority) {
        Task task = getTaskById(taskId);
        if (task == null) return false;
        
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCategory(category);
        task.setPriority(priority);
        
        task.setRecurring(isRecurring);
        if (isRecurring) {
            task.setRecurrenceType(recurrenceType);
            task.setRecurrenceInterval(recurrenceInterval);
            task.setRecurrenceEndDate(recurrenceEndDate);
        } else {
            task.setRecurrenceType(null);
            task.setRecurrenceInterval(0);
            task.setRecurrenceEndDate(null);
            task.setRecurringPatternId(null);
        }
        
        return TaskDAO.updateTask(task);
    }

    /**
     * Get all recurring tasks for a user
     * @param userId The ID of the user
     * @return List of recurring tasks for the user
     */
    public static List<Task> getRecurringTasks(int userId) {
        return TaskDAO.getTasksByUserId(userId).stream()
                .filter(Task::isRecurring)
                .collect(Collectors.toList());
    }

    /**
     * Get all recurring tasks that are due before a specific date
     * @param userId The ID of the user
     * @param endDate The end date to check
     * @return List of recurring tasks due before the end date
     */
    public static List<Task> getRecurringTasksBeforeDate(int userId, Date endDate) {
        return TaskDAO.getTasksByUserId(userId).stream()
                .filter(task -> task.isRecurring() && 
                        task.getRecurrenceEndDate() != null && 
                        !task.getRecurrenceEndDate().after(endDate))
                .collect(Collectors.toList());
    }

    /**
     * Update the recurrence pattern of a task
     * @param taskId The ID of the task to update
     * @param recurrenceType The type of recurrence (daily, weekly, monthly, yearly)
     * @param recurrenceInterval The interval of recurrence
     * @param recurrenceEndDate The end date of recurrence
     * @return true if the update was successful, false otherwise
     */
    public static boolean updateTaskRecurrence(int taskId, String recurrenceType, 
            int recurrenceInterval, Date recurrenceEndDate) {
        Task task = getTaskById(taskId);
        if (task == null) return false;
        
        task.setRecurring(true);
        task.setRecurrenceType(recurrenceType);
        task.setRecurrenceInterval(recurrenceInterval);
        task.setRecurrenceEndDate(recurrenceEndDate);
        
        return TaskDAO.updateTask(task);
    }

    /**
     * Remove recurrence from a task
     * @param taskId The ID of the task to update
     * @return true if the update was successful, false otherwise
     */
    public static boolean removeTaskRecurrence(int taskId) {
        Task task = getTaskById(taskId);
        if (task == null) return false;
        
        task.setRecurring(false);
        task.setRecurrenceType(null);
        task.setRecurrenceInterval(0);
        task.setRecurrenceEndDate(null);
        task.setRecurringPatternId(null);
        
        return TaskDAO.updateTask(task);
    }

    /**
     * Génère des instances futures pour une tâche récurrente
     * @param taskId ID de la tâche récurrente
     * @param numberOfInstances Nombre d'instances à générer
     * @return Liste des instances générées
     */
    public static List<Task> generateFutureInstances(int taskId, int numberOfInstances) {
        Task baseTask = TaskDAO.getTaskById(taskId);
        if (baseTask == null || !baseTask.isRecurring()) {
            return new ArrayList<>();
        }
        
        List<Task> generatedInstances = new ArrayList<>();
        Date baseDate = baseTask.getDueDate() != null ? baseTask.getDueDate() : new Date();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseDate);
        
        for (int i = 0; i < numberOfInstances; i++) {
            // Avancer à la prochaine date selon le type de récurrence
            if ("daily".equalsIgnoreCase(baseTask.getRecurrenceType())) {
                calendar.add(Calendar.DAY_OF_MONTH, baseTask.getRecurrenceInterval());
            } else if ("weekly".equalsIgnoreCase(baseTask.getRecurrenceType())) {
                calendar.add(Calendar.WEEK_OF_YEAR, baseTask.getRecurrenceInterval());
            } else if ("monthly".equalsIgnoreCase(baseTask.getRecurrenceType())) {
                calendar.add(Calendar.MONTH, baseTask.getRecurrenceInterval());
            } else if ("yearly".equalsIgnoreCase(baseTask.getRecurrenceType())) {
                calendar.add(Calendar.YEAR, baseTask.getRecurrenceInterval());
            }
            
            Date nextDate = calendar.getTime();
            
            // Vérifier si on a dépassé la date de fin de récurrence
            if (baseTask.getRecurrenceEndDate() != null && nextDate.after(baseTask.getRecurrenceEndDate())) {
                break;
            }
            
            // Créer une nouvelle instance
            Task newInstance = new Task(
                0, // Sera généré par la base de données
                baseTask.getUserId(),
                baseTask.getTitle(),
                baseTask.getDescription(),
                nextDate,
                "Pending", // Statut par défaut pour les nouvelles instances
                baseTask.getCategory(),
                false, // Les instances filles ne sont pas récurrentes
                null,
                0,
                null,
                baseTask.getTaskId(), // Référence à la tâche parente
                baseTask.getRecurringPatternId()
            );
            
            // Ajouter à la liste si l'insertion réussit
            if (TaskDAO.insertTask(newInstance)) {
                generatedInstances.add(newInstance);
            }
        }
        
        return generatedInstances;
    }
    
    /**
     * Génère toutes les instances en attente de tâches récurrentes pour un utilisateur
     * Cette méthode est utile lors du chargement initial de l'application
     * @param userId ID de l'utilisateur
     * @return Nombre d'instances générées
     */
    public static int generatePendingInstances(int userId) {
        List<Task> recurringTasks = getRecurringTasks(userId);
        int generatedCount = 0;
        
        Date today = new Date();
        
        for (Task recurringTask : recurringTasks) {
            // Ignorer les tâches sans date d'échéance
            if (recurringTask.getDueDate() == null) {
                continue;
            }
            
            // Trouver la dernière occurrence générée
            List<Task> childTasks = getChildTasks(recurringTask.getTaskId());
            Date lastOccurrenceDate = recurringTask.getDueDate();
            
            if (!childTasks.isEmpty()) {
                // Trouver la date la plus récente parmi les tâches enfants
                for (Task child : childTasks) {
                    if (child.getDueDate() != null && child.getDueDate().after(lastOccurrenceDate)) {
                        lastOccurrenceDate = child.getDueDate();
                    }
                }
            }
            
            // Calcul des occurrences manquantes
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastOccurrenceDate);
            
            while (true) {
                // Avancer à la prochaine date selon le type de récurrence
                if ("daily".equalsIgnoreCase(recurringTask.getRecurrenceType())) {
                    calendar.add(Calendar.DAY_OF_MONTH, recurringTask.getRecurrenceInterval());
                } else if ("weekly".equalsIgnoreCase(recurringTask.getRecurrenceType())) {
                    calendar.add(Calendar.WEEK_OF_YEAR, recurringTask.getRecurrenceInterval());
                } else if ("monthly".equalsIgnoreCase(recurringTask.getRecurrenceType())) {
                    calendar.add(Calendar.MONTH, recurringTask.getRecurrenceInterval());
                } else if ("yearly".equalsIgnoreCase(recurringTask.getRecurrenceType())) {
                    calendar.add(Calendar.YEAR, recurringTask.getRecurrenceInterval());
                } else {
                    break; // Type de récurrence inconnu
                }
                
                Date nextDate = calendar.getTime();
                
                // Arrêter si on a dépassé aujourd'hui ou la date de fin
                if (nextDate.after(today) || 
                    (recurringTask.getRecurrenceEndDate() != null && 
                     nextDate.after(recurringTask.getRecurrenceEndDate()))) {
                    break;
                }
                
                // Créer une nouvelle instance pour la date manquante
                Task newInstance = new Task(
                    0, // Sera généré par la base de données
                    recurringTask.getUserId(),
                    recurringTask.getTitle(),
                    recurringTask.getDescription(),
                    nextDate,
                    "Pending", // Statut par défaut pour les nouvelles instances
                    recurringTask.getCategory(),
                    false, // Les instances filles ne sont pas récurrentes
                    null,
                    0,
                    null,
                    recurringTask.getTaskId(), // Référence à la tâche parente
                    recurringTask.getRecurringPatternId()
                );
                
                // Ajouter à la liste si l'insertion réussit
                if (TaskDAO.insertTask(newInstance)) {
                    generatedCount++;
                }
            }
        }
        
        return generatedCount;
    }
    
    /**
     * Récupère toutes les tâches enfants générées à partir d'une tâche récurrente
     * @param parentTaskId ID de la tâche parente
     * @return Liste des tâches enfants
     */
    public static List<Task> getChildTasks(int parentTaskId) {
        List<Task> allTasks = TaskDAO.getTasksByUserId(
            TaskDAO.getTaskById(parentTaskId).getUserId()
        );
        
        return allTasks.stream()
            .filter(task -> task.getParentTaskId() != null && 
                   task.getParentTaskId() == parentTaskId)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère toutes les tâches accessibles à un utilisateur, incluant ses propres tâches
     * et les tâches partagées par ses collaborateurs
     * @param userId ID de l'utilisateur
     * @return Liste des tâches accessibles à l'utilisateur
     */
    public static List<Task> getAllAccessibleTasks(int userId) {
        // Récupérer les tâches propres à l'utilisateur
        List<Task> tasks = new ArrayList<>();
        
        // Récupérer la liste des collaborateurs
        List<User> collaborators = Controller.UserController.getCollaborators(userId);
        
        // Pour chaque collaborateur, récupérer ses tâches et les ajouter à notre liste
        for (User collaborator : collaborators) {
            List<Task> collaboratorTasks = TaskDAO.getTasksByUserId(collaborator.getUserId());
            tasks.addAll(collaboratorTasks);
        }
        
        return tasks;
    }

    /**
     * Check if a task is shared with a specific user
     * @param taskId The ID of the task to check
     * @param userId The ID of the user to check
     * @return true if the task is shared with the user, false otherwise
     */
    public static boolean isTaskSharedWithUser(int taskId, int userId) {
        // Get the task
        Task task = TaskDAO.getTaskById(taskId);
        if (task == null) return false;
        
        // Check if the task belongs to the user directly
        if (task.getUserId() == userId) return true;
        
        // Otherwise, check if the task is shared via collaboration
        return TaskDAO.isTaskSharedWithUser(taskId, userId);
    }
    
    /**
     * Met à jour le modèle de récurrence d'une tâche et régénère les occurrences futures
     * @param taskId ID de la tâche à mettre à jour
     * @param recurrenceType Type de récurrence (daily, weekly, monthly, yearly)
     * @param recurrenceInterval Intervalle de récurrence
     * @param recurrenceEndDate Date de fin de récurrence
     * @param regenerateInstances Indique s'il faut régénérer les instances futures
     * @return true si la mise à jour a réussi, false sinon
     */
    public static boolean updateTaskRecurrenceAndRegenerateInstances(
            int taskId, String recurrenceType, int recurrenceInterval, 
            Date recurrenceEndDate, boolean regenerateInstances) {
        
        // D'abord, mettre à jour le modèle de récurrence
        boolean updateSuccess = updateTaskRecurrence(taskId, recurrenceType, 
                recurrenceInterval, recurrenceEndDate);
        
        if (!updateSuccess) {
            return false;
        }
        
        if (regenerateInstances) {
            // Supprimer les occurrences futures existantes
            List<Task> childTasks = getChildTasks(taskId);
            Date today = new Date();
            
            for (Task child : childTasks) {
                if (child.getDueDate() != null && child.getDueDate().after(today)) {
                    TaskDAO.deleteTask(child.getTaskId());
                }
            }
            
            // Générer de nouvelles occurrences (3 par défaut)
            generateFutureInstances(taskId, 3);
        }
        
        return true;
    }

    /**
     * Récupère uniquement les tâches propres à l'utilisateur (pas celles des collaborateurs)
     * @param userId ID de l'utilisateur
     * @return Liste des tâches propres à l'utilisateur
     */
    public static List<Task> getUserOwnTasks(int userId) {
        return TaskDAO.getTasksByUserId(userId);
    }
}
