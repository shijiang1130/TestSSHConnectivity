package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static String DB_URL;
    private static String USER;
    private static String PASS;

    static {
        // Load database configuration from a properties file
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("path/to/your/hosts.properties")) {
            properties.load(input);
            DB_URL = properties.getProperty("db.url");
            USER = properties.getProperty("db.user");
            PASS = properties.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
        }
    }

    public void saveHostInfo(String hostname, String fileName, String jumpuserPassword) {
        String query = "INSERT INTO hosts (hostname, filename, jumpuser_password) VALUES (?, ?, ?)";
        
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, hostname);
            preparedStatement.setString(2, fileName);
            preparedStatement.setString(3, jumpuserPassword);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving host info: " + e.getMessage());
        }
    }
} 
