package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    // Get all notes for a user
    public static List<Note> getNotesByUserId(int userId) {
        List<Note> notes = new ArrayList<>();
        // Query to get all notes for a specific user, including their category information
        // Joins the notes table with categories table to get category details
        // Orders notes by creation date (newest first)
        String query = "SELECT n.*, c.id as category_id, c.name as category_name " +
                      "FROM notes n " +
                      "LEFT JOIN categories c ON n.category_id = c.id " +
                      "WHERE n.user_id = ? " +
                      "ORDER BY n.created_date DESC";

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
                
                Note note = new Note(
                    rs.getInt("note_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_date")
                );
                
                note.setCategory(category);
                notes.add(note);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching notes: " + e.getMessage());
        }
        return notes;
    }

    // Get notes by category for a user
    public static List<Note> getNotesByCategory(int userId, int categoryId) {
        List<Note> notes = new ArrayList<>();
        // Query to get notes for a specific user and category
        // Joins notes with categories table to get category details
        // Filters by both user_id and category_id
        String query = "SELECT n.*, c.id as category_id, c.name as category_name " +
                      "FROM notes n " +
                      "LEFT JOIN categories c ON n.category_id = c.id " +
                      "WHERE n.user_id = ? AND n.category_id = ? " +
                      "ORDER BY n.created_date DESC";

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
                
                Note note = new Note(
                    rs.getInt("note_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_date")
                );
                
                note.setCategory(category);
                notes.add(note);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching notes by category: " + e.getMessage());
        }
        return notes;
    }

    // Get a note by ID
    public static Note getNoteById(int noteId) {
        String query = "SELECT n.*, c.id as category_id, c.name as category_name " +
                      "FROM notes n " +
                      "LEFT JOIN categories c ON n.category_id = c.id " +
                      "WHERE n.note_id = ?";
        // Query to get a specific note by its ID
        // Joins with categories table to get category information

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, noteId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Category category = null;
                if (rs.getObject("category_id") != null) {
                    category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                    );
                }
                
                Note note = new Note(
                    rs.getInt("note_id"),
                    rs.getInt("user_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_date")
                );
                
                note.setCategory(category);
                return note;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching note: " + e.getMessage());
        }
        return null;
    }

    // Insert a new note
    public static boolean insertNote(Note note) {
        String query = "INSERT INTO notes (user_id, title, content, category_id) VALUES (?, ?, ?, ?)";
        // Query to insert a new note
        // Inserts user_id, title, content, and optional category_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, note.getUserId());
            stmt.setString(2, note.getTitle());
            stmt.setString(3, note.getContent());
            
            if (note.getCategory() != null) {
                stmt.setInt(4, note.getCategory().getId());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    note.setNoteId(generatedKeys.getInt(1));
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("Error inserting note: " + e.getMessage());
        }
        return false;
    }

    // Update a note
    public static boolean updateNote(Note note) {
        String query = "UPDATE notes SET title = ?, content = ?, category_id = ? WHERE note_id = ?";
        // Query to update an existing note
        // Updates title, content, and category_id for a specific note_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getContent());
            
            if (note.getCategory() != null) {
                stmt.setInt(3, note.getCategory().getId());
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }
            
            stmt.setInt(4, note.getNoteId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error updating note: " + e.getMessage());
        }
        return false;
    }

    // Delete a note
    public static boolean deleteNote(int noteId) {
        String query = "DELETE FROM notes WHERE note_id = ?";
        // Query to delete a note by its ID

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, noteId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting note: " + e.getMessage());
        }
        return false;
    }
} 