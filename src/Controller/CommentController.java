package Controller;

import Model.Comment;
import Model.CommentDAO;
import java.util.List;

/**
 * Contrôleur pour gérer les opérations liées aux commentaires de tâches
 */
public class CommentController {

    /**
     * Récupère tous les commentaires pour une tâche
     * @param taskId ID de la tâche
     * @return Liste des commentaires
     */
    public static List<Comment> getCommentsByTaskId(int taskId) {
        return CommentDAO.getCommentsByTaskId(taskId);
    }
    
    /**
     * Ajoute un nouveau commentaire à une tâche
     * @param taskId ID de la tâche
     * @param userId ID de l'utilisateur qui commente
     * @param content Contenu du commentaire
     * @return true si l'ajout a réussi, false sinon
     */
    public static boolean addComment(int taskId, int userId, String content) {
        // Vérifier que l'utilisateur a accès à la tâche
        if (!TaskController.isTaskSharedWithUser(taskId, userId)) {
            return false;
        }
        
        Comment comment = new Comment(taskId, userId, content);
        return CommentDAO.insertComment(comment);
    }
    
    /**
     * Modifie un commentaire existant
     * @param commentId ID du commentaire
     * @param userId ID de l'utilisateur (pour vérifier qu'il est l'auteur)
     * @param newContent Nouveau contenu
     * @return true si la modification a réussi, false sinon
     */
    public static boolean updateComment(int commentId, int userId, String newContent) {
        // Récupérer d'abord le commentaire à partir de son ID
        List<Comment> taskComments = CommentDAO.getCommentsByTaskId(0); // Obtient la liste des commentaires (toutes tâches)
        Comment commentToUpdate = null;
        
        // Chercher le commentaire par son ID
        for (Comment comment : taskComments) {
            if (comment.getCommentId() == commentId) {
                commentToUpdate = comment;
                break;
            }
        }
        
        // Si le commentaire n'existe pas, retourner false
        if (commentToUpdate == null) {
            return false;
        }
        
        // Vérifier que l'utilisateur est bien l'auteur du commentaire
        if (commentToUpdate.getUserId() != userId) {
            return false;
        }
        
        // Mettre à jour le contenu
        commentToUpdate.setContent(newContent);
        
        // La vérification de l'auteur est faite dans la méthode updateComment de CommentDAO
        // qui vérifie également que l'utilisateur est bien l'auteur du commentaire
        return CommentDAO.updateComment(commentToUpdate);
    }
    
    /**
     * Supprime un commentaire
     * @param commentId ID du commentaire
     * @param userId ID de l'utilisateur (pour vérifier l'autorisation)
     * @return true si la suppression a réussi, false sinon
     */
    public static boolean deleteComment(int commentId, int userId) {
        return CommentDAO.deleteComment(commentId, userId);
    }
} 