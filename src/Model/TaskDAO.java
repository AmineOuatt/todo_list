package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskDAO {

    // Fetch tasks for a user
    public static List<Task> getTasksByUserId(int userId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT t.*, c.id as category_id, c.name as category_name, " +
                      "rp.pattern_type, rp.interval_value, rp.day_of_week, rp.day_of_month, " +
                      "rp.month_of_year, rp.end_date, rp.max_occurrences " +
                      "FROM Tasks t " +
                      "LEFT JOIN categories c ON t.category_id = c.id " +
                      "LEFT JOIN recurring_patterns rp ON t.recurring_pattern_id = rp.pattern_id " +
                      "WHERE t.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = null;
                if (rs.getObject("category_id") != null) {
                    category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                    );
                }
                
                Task task = new Task(
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getTimestamp("due_date"),
                    rs.getString("status")
                );
                
                task.setCategory(category);
                
                // Récupération de la priorité
                if (rs.getString("priority") != null) {
                    task.setPriority(rs.getString("priority"));
                }
                
                // Handle recurring task properties
                boolean isRecurring = rs.getBoolean("is_recurring");
                task.setRecurring(isRecurring);
                
                if (isRecurring && rs.getObject("recurring_pattern_id") != null) {
                    task.setRecurringPatternId(rs.getInt("recurring_pattern_id"));
                    task.setRecurrenceType(rs.getString("pattern_type"));
                    task.setRecurrenceInterval(rs.getInt("interval_value"));
                    if (rs.getDate("end_date") != null) {
                        task.setRecurrenceEndDate(rs.getDate("end_date"));
                    }
                }
                
                // Handle parent task reference
                if (rs.getObject("parent_task_id") != null) {
                    task.setParentTaskId(rs.getInt("parent_task_id"));
                }
                
                tasks.add(task);
            }
            
            // Ne plus ajouter automatiquement les occurrences générées
            // tasks.addAll(generateRecurringTaskOccurrences(tasks));
            
        } catch (SQLException e) {
            System.out.println("Error fetching tasks: " + e.getMessage());
        }
        return tasks;
    }

    // Nouvelle méthode pour obtenir les tâches y compris les occurrences générées
    public static List<Task> getTasksWithOccurrences(int userId) {
        List<Task> tasks = getTasksByUserId(userId);
        tasks.addAll(generateRecurringTaskOccurrences(tasks));
        return tasks;
    }

    // Get task by ID
    public static Task getTaskById(int taskId) {
        String query = "SELECT t.*, c.id as category_id, c.name as category_name, " +
                      "rp.pattern_type, rp.interval_value, rp.day_of_week, rp.day_of_month, " +
                      "rp.month_of_year, rp.end_date, rp.max_occurrences " +
                      "FROM Tasks t " +
                      "LEFT JOIN categories c ON t.category_id = c.id " +
                      "LEFT JOIN recurring_patterns rp ON t.recurring_pattern_id = rp.pattern_id " +
                      "WHERE t.task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Category category = null;
                if (rs.getObject("category_id") != null) {
                    category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                    );
                }
                
                Task task = new Task(
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getTimestamp("due_date"),
                    rs.getString("status")
                );
                
                task.setCategory(category);
                
                // Récupération de la priorité
                if (rs.getString("priority") != null) {
                    task.setPriority(rs.getString("priority"));
                }
                
                // Handle recurring task properties
                boolean isRecurring = rs.getBoolean("is_recurring");
                task.setRecurring(isRecurring);
                
                if (isRecurring && rs.getObject("recurring_pattern_id") != null) {
                    task.setRecurringPatternId(rs.getInt("recurring_pattern_id"));
                    task.setRecurrenceType(rs.getString("pattern_type"));
                    task.setRecurrenceInterval(rs.getInt("interval_value"));
                    if (rs.getDate("end_date") != null) {
                        task.setRecurrenceEndDate(rs.getDate("end_date"));
                    }
                }
                
                // Handle parent task reference
                if (rs.getObject("parent_task_id") != null) {
                    task.setParentTaskId(rs.getInt("parent_task_id"));
                }
                
                return task;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching task: " + e.getMessage());
        }
        return null;
    }

    // Insert a new task
    public static boolean insertTask(Task task) {
        Connection conn = null;
        PreparedStatement stmtPattern = null;
        PreparedStatement stmtTask = null;
        ResultSet generatedKeys = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            Integer patternId = null;
            
            // If task is recurring, first create the pattern
            if (task.isRecurring()) {
                String patternQuery = "INSERT INTO recurring_patterns (pattern_type, interval_value, end_date, max_occurrences, day_of_week, day_of_month, month_of_year) VALUES (?, ?, ?, ?, ?, ?, ?)";
                stmtPattern = conn.prepareStatement(patternQuery, Statement.RETURN_GENERATED_KEYS);
                stmtPattern.setString(1, task.getRecurrenceType());
                stmtPattern.setInt(2, task.getRecurrenceInterval());
                
                if (task.getRecurrenceEndDate() != null) {
                    stmtPattern.setDate(3, new java.sql.Date(task.getRecurrenceEndDate().getTime()));
                } else {
                    stmtPattern.setNull(3, java.sql.Types.DATE);
                }
                
                if (task.getMaxOccurrences() != null) {
                    stmtPattern.setInt(4, task.getMaxOccurrences());
                } else {
                    stmtPattern.setNull(4, java.sql.Types.INTEGER);
                }
                
                if (task.getDayOfWeek() != null) {
                    stmtPattern.setInt(5, task.getDayOfWeek());
                } else {
                    stmtPattern.setNull(5, java.sql.Types.INTEGER);
                }
                
                if (task.getDayOfMonth() != null) {
                    stmtPattern.setInt(6, task.getDayOfMonth());
                } else {
                    stmtPattern.setNull(6, java.sql.Types.INTEGER);
                }
                
                if (task.getMonthOfYear() != null) {
                    stmtPattern.setInt(7, task.getMonthOfYear());
                } else {
                    stmtPattern.setNull(7, java.sql.Types.INTEGER);
                }
                
                int patternResult = stmtPattern.executeUpdate();
                
                if (patternResult > 0) {
                    generatedKeys = stmtPattern.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        patternId = generatedKeys.getInt(1);
                    }
                }
            }
            
            // Now insert the task
            String taskQuery = "INSERT INTO Tasks (user_id, title, description, due_date, status, category_id, is_recurring, recurring_pattern_id, parent_task_id, priority) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmtTask = conn.prepareStatement(taskQuery);
            stmtTask.setInt(1, task.getUserId());
            stmtTask.setString(2, task.getTitle());
            stmtTask.setString(3, task.getDescription());
            
            if (task.getDueDateTime() != null) {
                stmtTask.setTimestamp(4, task.getDueDateTime());
            } else {
                stmtTask.setNull(4, java.sql.Types.TIMESTAMP);
            }
            
            stmtTask.setString(5, task.getStatus());
            
            if (task.getCategory() != null) {
                stmtTask.setInt(6, task.getCategory().getId());
            } else {
                stmtTask.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmtTask.setBoolean(7, task.isRecurring());
            
            if (patternId != null) {
                stmtTask.setInt(8, patternId);
            } else {
                stmtTask.setNull(8, java.sql.Types.INTEGER);
            }
            
            if (task.getParentTaskId() != null) {
                stmtTask.setInt(9, task.getParentTaskId());
            } else {
                stmtTask.setNull(9, java.sql.Types.INTEGER);
            }
            
            // Ajout de la priorité
            stmtTask.setString(10, task.getPriority());

            int taskResult = stmtTask.executeUpdate();
            
            if (taskResult > 0) {
                conn.commit();
                success = true;
            } else {
                conn.rollback();
            }

        } catch (SQLException e) {
            System.out.println("Error inserting task: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmtPattern != null) stmtPattern.close();
                if (stmtTask != null) stmtTask.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
        
        return success;
    }

    // Update task
    public static boolean updateTask(Task task) {
        Connection conn = null;
        PreparedStatement stmtPattern = null;
        PreparedStatement stmtTask = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            Integer patternId = task.getRecurringPatternId();
            
            // Gérer la récurrence
            if (task.isRecurring()) {
                // Cas 1: Tâche déjà récurrente avec pattern existant - mettre à jour le pattern
                if (patternId != null) {
                    String patternQuery = "UPDATE recurring_patterns SET pattern_type = ?, interval_value = ?, end_date = ?, max_occurrences = ?, " +
                                          "day_of_week = ?, day_of_month = ?, month_of_year = ? WHERE pattern_id = ?";
                    stmtPattern = conn.prepareStatement(patternQuery);
                    stmtPattern.setString(1, task.getRecurrenceType());
                    stmtPattern.setInt(2, task.getRecurrenceInterval());
                    
                    if (task.getRecurrenceEndDate() != null) {
                        stmtPattern.setDate(3, new java.sql.Date(task.getRecurrenceEndDate().getTime()));
                    } else {
                        stmtPattern.setNull(3, java.sql.Types.DATE);
                    }
                    
                    if (task.getMaxOccurrences() != null) {
                        stmtPattern.setInt(4, task.getMaxOccurrences());
                    } else {
                        stmtPattern.setNull(4, java.sql.Types.INTEGER);
                    }
                    
                    if (task.getDayOfWeek() != null) {
                        stmtPattern.setInt(5, task.getDayOfWeek());
                    } else {
                        stmtPattern.setNull(5, java.sql.Types.INTEGER);
                    }
                    
                    if (task.getDayOfMonth() != null) {
                        stmtPattern.setInt(6, task.getDayOfMonth());
                    } else {
                        stmtPattern.setNull(6, java.sql.Types.INTEGER);
                    }
                    
                    if (task.getMonthOfYear() != null) {
                        stmtPattern.setInt(7, task.getMonthOfYear());
                    } else {
                        stmtPattern.setNull(7, java.sql.Types.INTEGER);
                    }
                    
                    stmtPattern.setInt(8, patternId);
                    stmtPattern.executeUpdate();
                } 
                // Cas 2: Nouvelle tâche récurrente - créer un nouveau pattern
                else {
                    String patternQuery = "INSERT INTO recurring_patterns (pattern_type, interval_value, end_date, max_occurrences, day_of_week, day_of_month, month_of_year) " +
                                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    stmtPattern = conn.prepareStatement(patternQuery, Statement.RETURN_GENERATED_KEYS);
                    stmtPattern.setString(1, task.getRecurrenceType());
                    stmtPattern.setInt(2, task.getRecurrenceInterval());
                    
                    if (task.getRecurrenceEndDate() != null) {
                        stmtPattern.setDate(3, new java.sql.Date(task.getRecurrenceEndDate().getTime()));
                    } else {
                        stmtPattern.setNull(3, java.sql.Types.DATE);
                    }
                    
                    if (task.getMaxOccurrences() != null) {
                        stmtPattern.setInt(4, task.getMaxOccurrences());
                    } else {
                        stmtPattern.setNull(4, java.sql.Types.INTEGER);
                    }
                    
                    if (task.getDayOfWeek() != null) {
                        stmtPattern.setInt(5, task.getDayOfWeek());
                    } else {
                        stmtPattern.setNull(5, java.sql.Types.INTEGER);
                    }
                    
                    if (task.getDayOfMonth() != null) {
                        stmtPattern.setInt(6, task.getDayOfMonth());
                    } else {
                        stmtPattern.setNull(6, java.sql.Types.INTEGER);
                    }
                    
                    if (task.getMonthOfYear() != null) {
                        stmtPattern.setInt(7, task.getMonthOfYear());
                    } else {
                        stmtPattern.setNull(7, java.sql.Types.INTEGER);
                    }
                    
                    int patternResult = stmtPattern.executeUpdate();
                    
                    if (patternResult > 0) {
                        ResultSet generatedKeys = stmtPattern.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            patternId = generatedKeys.getInt(1);
                            task.setRecurringPatternId(patternId);
                        }
                    }
                }
            }
            
            // Update the task
            String taskQuery = "UPDATE Tasks SET title = ?, description = ?, due_date = ?, status = ?, " +
                               "category_id = ?, is_recurring = ?, recurring_pattern_id = ?, parent_task_id = ?, priority = ? " +
                               "WHERE task_id = ?";
            
            stmtTask = conn.prepareStatement(taskQuery);
            stmtTask.setString(1, task.getTitle());
            stmtTask.setString(2, task.getDescription());
            
            if (task.getDueDateTime() != null) {
                stmtTask.setTimestamp(3, task.getDueDateTime());
            } else {
                stmtTask.setNull(3, java.sql.Types.TIMESTAMP);
            }
            
            stmtTask.setString(4, task.getStatus());
            
            if (task.getCategory() != null) {
                stmtTask.setInt(5, task.getCategory().getId());
            } else {
                stmtTask.setNull(5, java.sql.Types.INTEGER);
            }
            
            stmtTask.setBoolean(6, task.isRecurring());
            
            if (patternId != null) {
                stmtTask.setInt(7, patternId);
            } else {
                stmtTask.setNull(7, java.sql.Types.INTEGER);
            }
            
            if (task.getParentTaskId() != null) {
                stmtTask.setInt(8, task.getParentTaskId());
            } else {
                stmtTask.setNull(8, java.sql.Types.INTEGER);
            }
            
            // Ajout de la priorité
            stmtTask.setString(9, task.getPriority());
            
            stmtTask.setInt(10, task.getTaskId());
            
            int taskResult = stmtTask.executeUpdate();
            
            if (taskResult > 0) {
                conn.commit();
                success = true;
            } else {
                conn.rollback();
            }
            
        } catch (SQLException e) {
            System.out.println("Error updating task: " + e.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (stmtPattern != null) stmtPattern.close();
                if (stmtTask != null) stmtTask.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error closing resources: " + ex.getMessage());
            }
        }
        
        return success;
    }

    // Delete task
    public static boolean deleteTask(int taskId) {
        String query = "DELETE FROM Tasks WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting task: " + e.getMessage());
        }
        return false;
    }

    // Update task status
    public static boolean updateTaskStatus(int taskId, String status) {
        String query = "UPDATE Tasks SET status = ? WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, taskId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating task status: " + e.getMessage());
        }
        return false;
    }
    
    // Get tasks by category
    public static List<Task> getTasksByCategory(int userId, int categoryId) {
        List<Task> tasks = new ArrayList<>();
        String query = "SELECT t.*, c.id as category_id, c.name as category_name, " +
                      "rp.pattern_type, rp.interval_value, rp.day_of_week, rp.day_of_month, " +
                      "rp.month_of_year, rp.end_date, rp.max_occurrences " +
                      "FROM Tasks t " +
                      "LEFT JOIN categories c ON t.category_id = c.id " +
                      "LEFT JOIN recurring_patterns rp ON t.recurring_pattern_id = rp.pattern_id " +
                      "WHERE t.user_id = ? AND t.category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                );
                
                Task task = new Task(
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getTimestamp("due_date"),
                    rs.getString("status")
                );
                
                task.setCategory(category);
                
                // Récupération de la priorité
                if (rs.getString("priority") != null) {
                    task.setPriority(rs.getString("priority"));
                }
                
                // Handle recurring task properties
                boolean isRecurring = rs.getBoolean("is_recurring");
                task.setRecurring(isRecurring);
                
                if (isRecurring && rs.getObject("recurring_pattern_id") != null) {
                    task.setRecurringPatternId(rs.getInt("recurring_pattern_id"));
                    task.setRecurrenceType(rs.getString("pattern_type"));
                    task.setRecurrenceInterval(rs.getInt("interval_value"));
                    if (rs.getDate("end_date") != null) {
                        task.setRecurrenceEndDate(rs.getDate("end_date"));
                    }
                }
                
                // Handle parent task reference
                if (rs.getObject("parent_task_id") != null) {
                    task.setParentTaskId(rs.getInt("parent_task_id"));
                }
                
                tasks.add(task);
            }
            
            // Ne plus ajouter automatiquement les occurrences générées
            // tasks.addAll(generateRecurringTaskOccurrences(tasks));
            
        } catch (SQLException e) {
            System.out.println("Error fetching tasks by category: " + e.getMessage());
        }
        return tasks;
    }
    
    // Nouvelle méthode pour obtenir les tâches par catégorie y compris les occurrences
    public static List<Task> getTasksByCategoyWithOccurrences(int userId, int categoryId) {
        List<Task> tasks = getTasksByCategory(userId, categoryId);
        tasks.addAll(generateRecurringTaskOccurrences(tasks));
        return tasks;
    }
    
    // Update task category
    public static boolean updateTaskCategory(int taskId, Integer categoryId) {
        String query = "UPDATE Tasks SET category_id = ? WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            if (categoryId != null) {
                stmt.setInt(1, categoryId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setInt(2, taskId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating task category: " + e.getMessage());
        }
        return false;
    }
    
    // Create a recurring pattern and return its ID
    public static Integer createRecurringPattern(String patternType, int intervalValue, Date endDate, Integer maxOccurrences, 
                                               Integer dayOfWeek, Integer dayOfMonth, Integer monthOfYear) {
        String query = "INSERT INTO recurring_patterns (pattern_type, interval_value, end_date, max_occurrences, day_of_week, day_of_month, month_of_year) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, patternType);
            stmt.setInt(2, intervalValue);
            
            if (endDate != null) {
                stmt.setDate(3, new java.sql.Date(endDate.getTime()));
            } else {
                stmt.setNull(3, java.sql.Types.DATE);
            }
            
            if (maxOccurrences != null) {
                stmt.setInt(4, maxOccurrences);
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            if (dayOfWeek != null) {
                stmt.setInt(5, dayOfWeek);
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            if (dayOfMonth != null) {
                stmt.setInt(6, dayOfMonth);
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            if (monthOfYear != null) {
                stmt.setInt(7, monthOfYear);
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error creating recurring pattern: " + e.getMessage());
        }
        return null;
    }
    
    // Get recurring pattern by ID
    public static RecurringPattern getRecurringPatternById(int patternId) {
        String query = "SELECT * FROM recurring_patterns WHERE pattern_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patternId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                RecurringPattern pattern = new RecurringPattern();
                pattern.setPatternId(rs.getInt("pattern_id"));
                pattern.setPatternType(rs.getString("pattern_type"));
                pattern.setIntervalValue(rs.getInt("interval_value"));
                pattern.setDayOfWeek(rs.getObject("day_of_week") != null ? rs.getInt("day_of_week") : null);
                pattern.setDayOfMonth(rs.getObject("day_of_month") != null ? rs.getInt("day_of_month") : null);
                pattern.setMonthOfYear(rs.getObject("month_of_year") != null ? rs.getInt("month_of_year") : null);
                pattern.setEndDate(rs.getDate("end_date"));
                pattern.setMaxOccurrences(rs.getObject("max_occurrences") != null ? rs.getInt("max_occurrences") : null);
                
                return pattern;
            }
        } catch (SQLException e) {
            System.out.println("Error getting recurring pattern: " + e.getMessage());
        }
        return null;
    }
    
    // Generate recurring task occurrences based on the pattern
    private static List<Task> generateRecurringTaskOccurrences(List<Task> baseTasks) {
        List<Task> generatedTasks = new ArrayList<>();
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        
        for (Task baseTask : baseTasks) {
            if (!baseTask.isRecurring() || baseTask.getRecurringPatternId() == null) {
                continue;
            }
            
            RecurringPattern pattern = getRecurringPatternById(baseTask.getRecurringPatternId());
            if (pattern == null) continue;
            
            // Get the base due date
            Date baseDueDate = baseTask.getDueDate();
            if (baseDueDate == null) continue;
            
            // Set up for generating occurrences
            Calendar taskCal = Calendar.getInstance();
            taskCal.setTime(baseDueDate);
            
            // Generate future occurrences up to a reasonable limit (e.g., next 3 months)
            Calendar limitCal = Calendar.getInstance();
            limitCal.add(Calendar.MONTH, 3);
            Date limitDate = limitCal.getTime();
            
            // Consider end date if specified
            Date endDate = pattern.getEndDate();
            if (endDate != null && endDate.before(limitDate)) {
                limitDate = endDate;
            }
            
            // Skip if base date is already in the future
            if (baseDueDate.after(today)) {
                continue;
            }
            
            // Generate occurrences based on pattern type
            switch (pattern.getPatternType().toUpperCase()) {
                case "DAILY":
                    generateDailyOccurrences(baseTask, pattern, taskCal, today, limitDate, generatedTasks);
                    break;
                    
                case "WEEKLY":
                    generateWeeklyOccurrences(baseTask, pattern, taskCal, today, limitDate, generatedTasks);
                    break;
                    
                case "MONTHLY":
                    generateMonthlyOccurrences(baseTask, pattern, taskCal, today, limitDate, generatedTasks);
                    break;
                    
                case "YEARLY":
                    generateYearlyOccurrences(baseTask, pattern, taskCal, today, limitDate, generatedTasks);
                    break;
            }
        }
        
        return generatedTasks;
    }
    
    private static void generateDailyOccurrences(Task baseTask, RecurringPattern pattern, 
                                               Calendar taskCal, Date today, Date limitDate, 
                                               List<Task> generatedTasks) {
        int intervalDays = pattern.getIntervalValue();
        
        // Find the next occurrence after today
        while (taskCal.getTime().before(today)) {
            taskCal.add(Calendar.DAY_OF_MONTH, intervalDays);
        }
        
        // Generate occurrences up to the limit date
        while (taskCal.getTime().before(limitDate)) {
            Date occurrenceDate = taskCal.getTime();
            
            // Create a new task instance for this occurrence
            Task occurrenceTask = createOccurrenceTask(baseTask, occurrenceDate);
            generatedTasks.add(occurrenceTask);
            
            // Move to next occurrence
            taskCal.add(Calendar.DAY_OF_MONTH, intervalDays);
        }
    }
    
    private static void generateWeeklyOccurrences(Task baseTask, RecurringPattern pattern, 
                                                Calendar taskCal, Date today, Date limitDate, 
                                                List<Task> generatedTasks) {
        int intervalWeeks = pattern.getIntervalValue();
        
        // Find the next occurrence after today
        while (taskCal.getTime().before(today)) {
            taskCal.add(Calendar.WEEK_OF_YEAR, intervalWeeks);
        }
        
        // Generate occurrences up to the limit date
        while (taskCal.getTime().before(limitDate)) {
            Date occurrenceDate = taskCal.getTime();
            
            // Create a new task instance for this occurrence
            Task occurrenceTask = createOccurrenceTask(baseTask, occurrenceDate);
            generatedTasks.add(occurrenceTask);
            
            // Move to next occurrence
            taskCal.add(Calendar.WEEK_OF_YEAR, intervalWeeks);
        }
    }
    
    private static void generateMonthlyOccurrences(Task baseTask, RecurringPattern pattern, 
                                                 Calendar taskCal, Date today, Date limitDate, 
                                                 List<Task> generatedTasks) {
        int intervalMonths = pattern.getIntervalValue();
        
        // Find the next occurrence after today
        while (taskCal.getTime().before(today)) {
            taskCal.add(Calendar.MONTH, intervalMonths);
        }
        
        // Generate occurrences up to the limit date
        while (taskCal.getTime().before(limitDate)) {
            Date occurrenceDate = taskCal.getTime();
            
            // Create a new task instance for this occurrence
            Task occurrenceTask = createOccurrenceTask(baseTask, occurrenceDate);
            generatedTasks.add(occurrenceTask);
            
            // Move to next occurrence
            taskCal.add(Calendar.MONTH, intervalMonths);
        }
    }
    
    private static void generateYearlyOccurrences(Task baseTask, RecurringPattern pattern, 
                                                Calendar taskCal, Date today, Date limitDate, 
                                                List<Task> generatedTasks) {
        int intervalYears = pattern.getIntervalValue();
        
        // Find the next occurrence after today
        while (taskCal.getTime().before(today)) {
            taskCal.add(Calendar.YEAR, intervalYears);
        }
        
        // Generate occurrences up to the limit date
        while (taskCal.getTime().before(limitDate)) {
            Date occurrenceDate = taskCal.getTime();
            
            // Create a new task instance for this occurrence
            Task occurrenceTask = createOccurrenceTask(baseTask, occurrenceDate);
            generatedTasks.add(occurrenceTask);
            
            // Move to next occurrence
            taskCal.add(Calendar.YEAR, intervalYears);
        }
    }
    
    private static Task createOccurrenceTask(Task baseTask, Date occurrenceDate) {
        // Create a virtual occurrence task (not saved to database)
        Task occurrenceTask = new Task(
            -1, // Virtual ID for UI display only
            baseTask.getUserId(),
            baseTask.getTitle(),
            baseTask.getDescription(),
            occurrenceDate,
            "pending" // Default to pending for generated occurrences
        );
        
        occurrenceTask.setCategory(baseTask.getCategory());
        occurrenceTask.setRecurring(true);
        occurrenceTask.setRecurringPatternId(baseTask.getRecurringPatternId());
        occurrenceTask.setRecurrenceType(baseTask.getRecurrenceType());
        occurrenceTask.setRecurrenceInterval(baseTask.getRecurrenceInterval());
        occurrenceTask.setRecurrenceEndDate(baseTask.getRecurrenceEndDate());
        occurrenceTask.setParentTaskId(baseTask.getTaskId());
        occurrenceTask.setPriority(baseTask.getPriority()); // Copier la priorité
        
        return occurrenceTask;
    }
}
