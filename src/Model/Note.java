package Model;

import java.util.Date;

public class Note {
    private int noteId;
    private int userId;
    private String title;
    private String content;
    private Category category;
    private Date createdDate;

    public Note(int noteId, int userId, String title, String content, Category category, Date createdDate) {
        this.noteId = noteId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createdDate = createdDate;
    }

    public Note(int noteId, int userId, String title, String content, Date createdDate) {
        this.noteId = noteId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.category = null;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String toString() {
        return title;
    }
} 