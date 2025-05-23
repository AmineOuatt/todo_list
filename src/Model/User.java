package Model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private List<Integer> collaboratorIds; // Liste des IDs des collaborateurs

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.collaboratorIds = new ArrayList<>();
    }

    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.collaboratorIds = new ArrayList<>();
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<Integer> getCollaboratorIds() {
        return collaboratorIds;
    }

    public void setCollaboratorIds(List<Integer> collaboratorIds) {
        this.collaboratorIds = collaboratorIds;
    }

    public void addCollaborator(int collaboratorId) {
        if (!collaboratorIds.contains(collaboratorId)) {
            collaboratorIds.add(collaboratorId);
        }
    }

    public void removeCollaborator(int collaboratorId) {
        collaboratorIds.remove(Integer.valueOf(collaboratorId));
    }

    public boolean hasCollaborator(int collaboratorId) {
        return collaboratorIds.contains(collaboratorId);
    }

    @Override
    public String toString() {
        return username; // This makes JComboBox display usernames instead of Model.User@hashcode
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.passwordHash = password;
    }
}
