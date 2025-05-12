package Model;

public class SubTask {
    private int subTaskId;
    private int taskId;
    private String description;
    private boolean completed;

    public SubTask(int subTaskId, int taskId, String description, boolean completed) {
        this.subTaskId = subTaskId;
        this.taskId = taskId;
        this.description = description;
        this.completed = completed;
    }

    public int getSubTaskId() {
        return subTaskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
} 