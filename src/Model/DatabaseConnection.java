package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/poo_pr"; // Change to your database name
    private static final String USER = "root";  // Change if needed
    private static final String PASSWORD = "@Mine0903";  // Change if needed

    // Method to establish connection
    public static Connection getConnection() {
        try {
            // Load MySQL JDBC Driver (optional for modern Java versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(" Connected successfully!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println(" JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println(" Connection failed: " + e.getMessage());
        }
        return null;
    }

    // Main method to test connection
    public static void main(String[] args) {
        Connection conn = getConnection();

        // Check if connection was successful
        if (conn != null) {
            try {
                conn.close();
                System.out.println(" Connection closed successfully.");
            } catch (SQLException e) {
                System.out.println(" Error closing connection: " + e.getMessage());
            }
        } else {
            System.out.println(" Connection test failed.");
        }
    }
}
