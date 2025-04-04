package Model;

public class User {
    private int userId;
    private String username;
    private String passwordHash;

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
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
