package model.repository;

import configuration.DbConnection;
import model.entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
//    @Override
//    public User save(User user) {
//        try (Connection conn = DbConnection.getDatabaseConnection()) {
//            String sql = """
//                    INSERT INTO users (user_name, email, password, is_deleted, u_uuid, create_at)
//                    VALUES (?, ?, ?, ?, ?, ?)
//                    """;
//            PreparedStatement statement = conn.prepareStatement(sql);
//            statement.setString(1, user.getUserName());
//            statement.setString(2, user.getEmail());
//            statement.setString(3, user.getPassword());
//            statement.setBoolean(4, user.getIsDeleted());
//            statement.setString(5, user.getUserUuid());
//            statement.setTimestamp(6, (Timestamp) user.getCreateAt());
//            int rowAffected = statement.executeUpdate();
//            if (rowAffected > 0) return user;
//
//        } catch (SQLException e) {
//            System.err.println(e.getMessage());
//        }
//        return null;
//    }

    public List<User> findAll() {
        try (Connection connection = DbConnection.getDatabaseConnection()) {
            String query = "SELECT * FROM users";
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            List<User> users = new ArrayList<>();
            User user;
            while (rs.next()) {
                user = new User();
                user.setUserName(rs.getString("user_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setIsDeleted(rs.getBoolean("is_deleted"));
                user.setUserUuid(rs.getString("u_uuid"));
                user.setCreateAt(rs.getTimestamp("create_at"));
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

//    @Override
//    public Integer delete(Integer id) {
//        User user = findUserById(id);
//        if (user == null) {
//            System.err.println("[!]User with ID " + id + " not found");
//            return 0;
//        }
//        user.setIsDeleted(true);
//        return 1;
//    }

    public User findUserById(Integer id) {
        try (Connection connection = DbConnection.getDatabaseConnection()) {
            String query = """
                    SELECT * FROM users WHERE id = ?;
                    """;
            PreparedStatement statement = connection.prepareStatement(query);
            User user = new User();
            ResultSet rs = statement.executeQuery();
            user.setUserUuid(rs.getString("u_uuid"));
            user.setUserName(rs.getString("user_name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setIsDeleted(rs.getBoolean("is_deleted"));
            user.setCreateAt(rs.getTimestamp("create_at"));
            user.setId(rs.getInt("id"));
            return user;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
