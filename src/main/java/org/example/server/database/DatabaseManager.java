package org.example.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5433/studs";
    private static final String USER = "postgres";
    private static final String PASSWORD = "";

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void testConnection() {
        try (Connection ignored = getConnection()) {
            System.out.println(" Successfully connected to the database!");

        } catch (SQLException e) {
            System.err.println(" Database connection failed: " + e.getMessage());
        }
    }
}
