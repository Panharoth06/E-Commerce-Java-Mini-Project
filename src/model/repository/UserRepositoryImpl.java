package model.repository;

import configuration.DbConnection;
import model.entities.User;
import util.PasswordEncryptor;
import util.LoginSession;

import java.sql.*;
import java.util.UUID;

public class UserRepositoryImpl {

    public boolean register(User user) {
        if (existsByUsername(user.getUserName())) {
            System.out.println("Username already exists. Please login instead.");
            return false;
        }

        try (Connection conn = DbConnection.getDatabaseConnection()) {
            String sql = """
                INSERT INTO users (u_uuid, user_name, email, password, is_deleted, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            UUID newUuid = UUID.randomUUID();

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, newUuid);
            stmt.setString(2, user.getUserName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, PasswordEncryptor.hashPassword(user.getPassword()));
            stmt.setBoolean(5, false);
            stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                LoginSession.saveLoggedInUsername(user.getUserName());
                System.out.println("User registered and logged in successfully.");
                return true;
            }
        } catch (SQLException e) {
            System.err.println(" Registration error: " + e.getMessage());
        }

        return false;
    }

    public boolean login(String username, String password) {
        if (LoginSession.isLoggedIn()) {
            System.out.println("Already logged in as: " + LoginSession.getLoggedInUsername());
            return true;
        }

        try (Connection conn = DbConnection.getDatabaseConnection()) {
            String sql = "SELECT password FROM users WHERE user_name = ? AND is_deleted = false";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (PasswordEncryptor.checkPassword(password, hashedPassword)) {
                    LoginSession.saveLoggedInUsername(username);

                    return true;
                } else {
                    System.out.println("Incorrect password.");
                }
            } else {
                System.out.println("Username not found or account deleted.");
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        return false;
    }

    private boolean existsByUsername(String username) {
        try (Connection conn = DbConnection.getDatabaseConnection()) {
            String sql = "SELECT COUNT(*) FROM users WHERE user_name = ? AND is_deleted = false";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }
}
