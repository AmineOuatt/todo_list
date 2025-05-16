package Model;

import java.util.Date;
import java.sql.Timestamp;

public class Task {
    private int taskId;
    private int userId;
    private String title;
    private String description;
    private Timestamp dueDateTime;
    private String status;
    private Category category;
    private boolean isRecurring;
    private String recurrenceType;
    private int recurrenceInterval;
    private Date recurrenceEndDate;
    private Integer parentTaskId;
    private Integer recurringPatternId;
    private Integer maxOccurrences;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private Integer monthOfYear;
    private String priority;

    public Task(int taskId, int userId, String title, String description, Timestamp dueDateTime, String status) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDateTime = dueDateTime;
        this.status = status;
        this.category = null;
        this.isRecurring = false;
        this.recurrenceType = null;
        this.recurrenceInterval = 0;
        this.recurrenceEndDate = null;
        this.parentTaskId = null;
        this.recurringPatternId = null;
        this.maxOccurrences = null;
        this.dayOfWeek = null;
        this.dayOfMonth = null;
        this.monthOfYear = null;
        this.priority = "NORMAL";
    }
    
    public Task(int taskId, int userId, String title, String description, Date dueDate, String status) {
        this(taskId, userId, title, description, 
            dueDate != null ? new Timestamp(dueDate.getTime()) : null, 
            status);
    }

    public Task(int taskId, int userId, String title, String description, Timestamp dueDateTime, String status, Category category) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDateTime = dueDateTime;
        this.status = status;
        this.category = category;
        this.isRecurring = false;
        this.recurrenceType = null;
        this.recurrenceInterval = 0;
        this.recurrenceEndDate = null;
        this.parentTaskId = null;
        this.recurringPatternId = null;
        this.maxOccurrences = null;
        this.dayOfWeek = null;
        this.dayOfMonth = null;
        this.monthOfYear = null;
        this.priority = "NORMAL";
    }
    
    public Task(int taskId, int userId, String title, String description, Date dueDate, String status, Category category) {
        this(taskId, userId, title, description, 
            dueDate != null ? new Timestamp(dueDate.getTime()) : null, 
            status, category);
    }
    
    public Task(int taskId, int userId, String title, String description, Timestamp dueDateTime, String status, 
                Category category, boolean isRecurring, String recurrenceType, int recurrenceInterval, 
                Date recurrenceEndDate, Integer parentTaskId, Integer recurringPatternId) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDateTime = dueDateTime;
        this.status = status;
        this.category = category;
        this.isRecurring = isRecurring;
        this.recurrenceType = recurrenceType;
        this.recurrenceInterval = recurrenceInterval;
        this.recurrenceEndDate = recurrenceEndDate;
        this.parentTaskId = parentTaskId;
        this.recurringPatternId = recurringPatternId;
        this.maxOccurrences = null;
        this.dayOfWeek = null;
        this.dayOfMonth = null;
        this.monthOfYear = null;
        this.priority = "NORMAL";
    }
    
    public Task(int taskId, int userId, String title, String description, Date dueDate, String status, 
                Category category, boolean isRecurring, String recurrenceType, int recurrenceInterval, 
                Date recurrenceEndDate, Integer parentTaskId, Integer recurringPatternId) {
        this(taskId, userId, title, description, 
            dueDate != null ? new Timestamp(dueDate.getTime()) : null, 
            status, category, isRecurring, recurrenceType, recurrenceInterval,
            recurrenceEndDate, parentTaskId, recurringPatternId);
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

    public Timestamp getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(Timestamp dueDateTime) {
        this.dueDateTime = dueDateTime;
    }
    
    public Date getDueDate() {
        return dueDateTime != null ? new Date(dueDateTime.getTime()) : null;
    }

    public void setDueDate(Date dueDate) {
        this.dueDateTime = dueDate != null ? new Timestamp(dueDate.getTime()) : null;
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

    public Integer getMaxOccurrences() {
        return maxOccurrences;
    }
    
    public void setMaxOccurrences(Integer maxOccurrences) {
        this.maxOccurrences = maxOccurrences;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public Integer getDayOfMonth() {
        return dayOfMonth;
    }
    
    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }
    
    public Integer getMonthOfYear() {
        return monthOfYear;
    }
    
    public void setMonthOfYear(Integer monthOfYear) {
        this.monthOfYear = monthOfYear;
    }

    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
}
