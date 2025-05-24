package Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class DatabaseConnection {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/poo_pr"; // Change to your database name
    private static final String USER = "root";  // Change if needed
    private static final String PASSWORD = "";  // Change if needed

    // Method to establish connection
    public static Connection getConnection() {
        try {
            // Try to load driver using alternative method if standard method fails
            try {
                // Standard loading
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("Trying to load driver from lib folder...");
                
                // Try to load from lib folder
                File file = new File("lib/mysql-connector-j-9.2.0.jar");
                if (file.exists()) {
                    URLClassLoader classLoader = new URLClassLoader(
                            new URL[] { file.toURI().toURL() },
                            DatabaseConnection.class.getClassLoader()
                    );
                    Class.forName("com.mysql.cj.jdbc.Driver", true, classLoader);
                    System.out.println("Driver loaded successfully from lib folder!");
                } else {
                    System.out.println("MySQL JDBC driver JAR not found in lib folder!");
                    throw e; // Rethrow the original exception if file doesn't exist
                }
            }

            // Establish connection
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected successfully!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        } else {
            System.out.println("Connection test failed.");
        }
    }
}
