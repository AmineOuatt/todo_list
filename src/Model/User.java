package Model;

public class User {
    private int userId;
    private String username;

    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
    @Override
    public String toString() {
    return username; // This makes JComboBox display usernames instead of Model.User@hashcode
}

}
