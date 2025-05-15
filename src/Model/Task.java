package Model;

import java.util.Date;

public class Task {
    private int taskId;
    private int userId;
    private String title;
    private String description;
    private Date dueDate;
    private String status;
    private Category category;
    private boolean isRecurring;
    private String recurrenceType;
    private int recurrenceInterval;
    private Date recurrenceEndDate;
    private Integer parentTaskId;
    private Integer recurringPatternId;

    public Task(int taskId, int userId, String title, String description, Date dueDate, String status) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.category = null;
        this.isRecurring = false;
        this.recurrenceType = null;
        this.recurrenceInterval = 0;
        this.recurrenceEndDate = null;
        this.parentTaskId = null;
        this.recurringPatternId = null;
    }

    public Task(int taskId, int userId, String title, String description, Date dueDate, String status, Category category) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.category = category;
        this.isRecurring = false;
        this.recurrenceType = null;
        this.recurrenceInterval = 0;
        this.recurrenceEndDate = null;
        this.parentTaskId = null;
        this.recurringPatternId = null;
    }
    
    public Task(int taskId, int userId, String title, String description, Date dueDate, String status, 
                Category category, boolean isRecurring, String recurrenceType, int recurrenceInterval, 
                Date recurrenceEndDate, Integer parentTaskId, Integer recurringPatternId) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.category = category;
        this.isRecurring = isRecurring;
        this.recurrenceType = recurrenceType;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceEndDate = recurrenceEndDate;
        this.parentTaskId = parentTaskId;
        this.recurringPatternId = recurringPatternId;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return getDueDate();
    }

    public void setDate(Date date) {
        setDueDate(date);
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public int getCategoryId() {
        return category != null ? category.getId() : 0;
    }
    
    public boolean isRecurring() {
        return isRecurring;
    }
    
    public void setRecurring(boolean isRecurring) {
        this.isRecurring = isRecurring;
    }
    
    public String getRecurrenceType() {
        return recurrenceType;
    }
    
    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }
    
    public int getRecurrenceInterval() {
        return recurrenceInterval;
    }
    
    public void setRecurrenceInterval(int recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
    }
    
    public Date getRecurrenceEndDate() {
        return recurrenceEndDate;
    }
    
    public void setRecurrenceEndDate(Date recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }
    
    public Integer getParentTaskId() {
        return parentTaskId;
    }
    
    public void setParentTaskId(Integer parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
    
    public Integer getRecurringPatternId() {
        return recurringPatternId;
    }
    
    public void setRecurringPatternId(Integer recurringPatternId) {
        this.recurringPatternId = recurringPatternId;
    }
}
