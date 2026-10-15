package com.scc.dao;

import com.scc.model.User;
import java.sql.*;

public class UserDao {

    // Register Account (Save new user details with default status PENDING)
    public boolean registerUser(User user) {
        String sql = "INSERT INTO Users(Username, PasswordHash, Role, Status) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getStatus());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration failed (Username might already exist): " + e.getMessage());
            return false;
        }
    }

    // Login Authentication (Verify username and password against the database)
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND PasswordHash = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("Role"),
                        rs.getString("Status"),
                        rs.getString("LastLoginDate")
                );
            }
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }
}
