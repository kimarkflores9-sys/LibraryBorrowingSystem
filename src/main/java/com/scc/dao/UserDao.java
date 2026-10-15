package com.scc.dao;

import com.scc.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    private static final String COLUMNS = "id, username, password, roles, status";

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, roles, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.username());
            pstmt.setString(2, user.password());
            pstmt.setString(3, user.roles());
            pstmt.setString(4, user.status());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    // Now returns the User (with role + status) so the menu can decide admin vs user
    public Optional<User> login(String username, String password) {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ---------- ADMIN functions ----------

    public List<User> getAllUsers() {
        return query("SELECT " + COLUMNS + " FROM users ORDER BY id", null);
    }

    public List<User> getUsersByStatus(String status) {
        return query("SELECT " + COLUMNS + " FROM users WHERE status = ? ORDER BY id", status);
    }

    // Admin accounts are protected from status changes / deletion
    public boolean updateStatus(int userId, String newStatus) {
        return execute("UPDATE users SET status = ? WHERE id = ? AND roles <> 'ADMIN'", newStatus, userId);
    }

    public boolean deleteUser(int userId) {
        return execute("DELETE FROM users WHERE id = ? AND roles <> 'ADMIN'", userId);
    }

    // ---------- helpers ----------

    private List<User> query(String sql, String param) {
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (param != null) pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) users.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
        return users;
    }

    private boolean execute(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) pstmt.setObject(i + 1, params[i]);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                rs.getString("roles"), rs.getString("status"));
    }
}
