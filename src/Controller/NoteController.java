package Controller;

import java.util.Date;
import java.util.List;

import Model.Category;
import Model.Note;
import Model.NoteDAO;

public class NoteController {
    
    // Get all notes for a user
    public static List<Note> getNotes(int userId) {
        return NoteDAO.getNotesByUserId(userId);
    }
    
    // Get notes by category
    public static List<Note> getNotesByCategory(int userId, int categoryId) {
        return NoteDAO.getNotesByCategory(userId, categoryId);
    }
    
    // Get note by ID
    public static Note getNoteById(int noteId) {
        return NoteDAO.getNoteById(noteId);
    }
    
    // Create a new note
    public static boolean createNote(int userId, String title, String content) {
        Note note = new Note(0, userId, title, content, new Date());
        return NoteDAO.insertNote(note);
    }
    
    // Create a new note with category
    public static boolean createNote(int userId, String title, String content, Category category) {
        Note note = new Note(0, userId, title, content, category, new Date());
        return NoteDAO.insertNote(note);
    }
    
    // Update note
    public static boolean updateNote(int noteId, String title, String content) {
        Note note = getNoteById(noteId);
        if (note == null) return false;
        
        note.setTitle(title);
        note.setContent(content);
        
        return NoteDAO.updateNote(note);
    }
    
    // Update note with category
    public static boolean updateNote(int noteId, String title, String content, Category category) {
        Note note = getNoteById(noteId);
        if (note == null) return false;
        
        note.setTitle(title);
        note.setContent(content);
        note.setCategory(category);
        
        return NoteDAO.updateNote(note);
    }
    
    // Delete note
    public static boolean deleteNote(int noteId) {
        return NoteDAO.deleteNote(noteId);
    }
} 