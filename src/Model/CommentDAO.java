package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DAO pour gérer les opérations de base de données liées aux commentaires de tâches
 */
public class CommentDAO {

    /**
     * Récupère tous les commentaires pour une tâche spécifique
     * @param taskId ID de la tâche
     * @return Liste des commentaires pour cette tâche
     */
    public static List<Comment> getCommentsByTaskId(int taskId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT c.*, u.username " +
                       "FROM task_comments c " +
                       "JOIN users u ON c.user_id = u.user_id " +
                       "WHERE c.task_id = ? " +
                       "ORDER BY c.created_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Comment comment = new Comment(
                    rs.getInt("comment_id"),
                    rs.getInt("task_id"),
                    rs.getInt("user_id"),
                    rs.getString("content"),
                    rs.getTimestamp("created_date")
                );
                
                // Créer et définir l'auteur avec le nom d'utilisateur récupéré de la jointure
                User author = new User(
                    rs.getInt("user_id"),
                    rs.getString("username")
                );
                comment.setAuthor(author);
                
                comments.add(comment);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    /**
     * Ajoute un nouveau commentaire à une tâche
     * @param comment Le commentaire à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public static boolean insertComment(Comment comment) {
        String query = "INSERT INTO task_comments (task_id, user_id, content, created_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, comment.getTaskId());
            stmt.setInt(2, comment.getUserId());
            stmt.setString(3, comment.getContent());
            stmt.setTimestamp(4, new java.sql.Timestamp(comment.getCreatedDate().getTime()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setCommentId(generatedKeys.getInt(1));
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Modifie un commentaire existant
     * @param comment Le commentaire à modifier
     * @return true si la modification a réussi, false sinon
     */
    public static boolean updateComment(Comment comment) {
        String query = "UPDATE task_comments SET content = ? WHERE comment_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getCommentId());
            stmt.setInt(3, comment.getUserId()); // Vérifier que l'utilisateur est bien l'auteur

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Supprime un commentaire
     * @param commentId ID du commentaire à supprimer
     * @param userId ID de l'utilisateur (pour vérifier l'autorisation)
     * @return true si la suppression a réussi, false sinon
     */
    public static boolean deleteComment(int commentId, int userId) {
        String query = "DELETE FROM task_comments WHERE comment_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, commentId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du commentaire: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Supprime tous les commentaires d'une tâche (utilisé lors de la suppression d'une tâche)
     * @param taskId ID de la tâche
     * @return true si la suppression a réussi, false sinon
     */
    public static boolean deleteAllCommentsForTask(int taskId) {
        String query = "DELETE FROM task_comments WHERE task_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, taskId);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression des commentaires: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
} 