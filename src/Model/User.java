package Model;

public class User {
    private int userId;
    private String username;
    private String password;

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public User(int userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return username; // This makes JComboBox display usernames instead of Model.User@hashcode
    }
}
