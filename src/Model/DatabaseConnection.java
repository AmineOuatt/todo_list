package Model;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    // Database credentials
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "poo_pr";
    private static final String USER = "root";  // Change if needed
    private static final String PASSWORD = "";  // Change if needed
    
    private static boolean isDriverLoaded = false;

    // Method to establish connection
    public static Connection getConnection() {
        // If driver hasn't been successfully loaded yet, try to load it
        if (!isDriverLoaded) {
            loadDriver();
        }
        
        try {
            // Build connection URL with proper parameters
            String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC", 
                                      HOST, PORT, DATABASE);
            
            // Set connection properties
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("connectTimeout", "5000");
            
            // Try to establish connection
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection successful!");
            return conn;
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void loadDriver() {
        try {
            // Try loading with Class.forName first
            System.out.println("Attempting to load MySQL JDBC driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver registered successfully!");
            isDriverLoaded = true;
        } catch (ClassNotFoundException e1) {
            System.out.println("Standard driver loading failed: " + e1.getMessage());
            
            // Try with explicit path to lib folder
            try {
                System.out.println("Trying to load driver from lib folder...");
                File driverJar = new File("lib/mysql-connector-j-9.2.0.jar");
                
                if (driverJar.exists()) {
                    URLClassLoader classLoader = new URLClassLoader(
                        new URL[] { driverJar.toURI().toURL() },
                        DatabaseConnection.class.getClassLoader()
                    );
                    
                    Class.forName("com.mysql.cj.jdbc.Driver", true, classLoader);
                    System.out.println("Driver loaded successfully from lib folder!");
                    isDriverLoaded = true;
                } else {
                    System.out.println("MySQL JDBC driver JAR not found in lib folder: " + driverJar.getAbsolutePath());
                }
            } catch (Exception e2) {
                System.out.println("Failed to load driver from JAR: " + e2.getMessage());
                e2.printStackTrace();
                
                // One last attempt - try with MySQL Connector/J 8.0 class name
                try {
                    Class.forName("com.mysql.jdbc.Driver");  // Older driver class name
                    System.out.println("Older MySQL JDBC Driver registered successfully!");
                    isDriverLoaded = true;
                } catch (ClassNotFoundException e3) {
                    System.out.println("All attempts to load MySQL driver failed.");
                }
            }
        }
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
