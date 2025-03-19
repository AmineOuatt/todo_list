package Model;

import java.util.Date;

public class Task {
    private int taskId;
    private int userId;
    private String title;
    private String description;
    private Date dueDate;
    private String status;

    public Task(int taskId, int userId, String title, String description, Date dueDate, String status) {
        this.taskId = taskId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.status = status;
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

    public String getDescription() {
        return description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getStatus() {
        return status;
    }
}
