package org.example.server.database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public static void registerUser(String login, String password) {
        String hashedPassword = hashPassword(password);
        String sql = "INSERT INTO users (login, password) VALUES (?, ?)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2,hashedPassword);
            statement.executeUpdate();

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                System.err.println(" User '" + login + "' already exists");
            } else {
                System.err.println(" Registration failed: " + e.getMessage());
            }
        }
    }


    public static boolean authenticateUser(String login, String password) {
        if (login == null || password == null) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        String sql = "SELECT login FROM users WHERE login = ? AND password = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2, hashedPassword);
            ResultSet resultset = statement.executeQuery();
            return resultset.next();

        } catch (SQLException e) {
            System.err.println("❌ Credential verification failed: " + e.getMessage());
            return false;
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-384 algorithm not found", e);
        }
    }
}
