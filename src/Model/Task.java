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

    public Task(int taskId, int userId, String title, String description, Date dueDate, String status) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.category = null;
    }

    public Task(int taskId, int userId, String title, String description, Date dueDate, String status, Category category) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
        this.category = category;
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
}
