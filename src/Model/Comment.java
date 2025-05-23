package Model;

import java.util.Date;

/**
 * Représente un commentaire associé à une tâche
 */
public class Comment {
    private int commentId;
    private int taskId;
    private int userId;
    private String content;
    private Date createdDate;
    private User author; // Pour stocker les informations de l'auteur

    /**
     * Constructeur avec tous les champs
     */
    public Comment(int commentId, int taskId, int userId, String content, Date createdDate) {
        this.commentId = commentId;
        this.taskId = taskId;
        this.userId = userId;
        this.content = content;
        this.createdDate = createdDate;
    }

    /**
     * Constructeur sans ID (pour la création d'un nouveau commentaire)
     */
    public Comment(int taskId, int userId, String content) {
        this(-1, taskId, userId, content, new Date());
    }

    // Getters et setters
    public int getCommentId() {
        return commentId;
    }

    public void setCommentId(int commentId) {
        this.commentId = commentId;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
} 