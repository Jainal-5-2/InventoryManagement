package com.mycompany.inventorymanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public enum RegisterResult { SUCCESS, USERNAME_TAKEN, ERROR }

    public User authenticate(String username, String password) {
        String sql = "SELECT id, username, password, full_name FROM users "
                   + "WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RegisterResult register(String username, String password, String fullName) {
        String checkSql  = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, full_name) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return RegisterResult.USERNAME_TAKEN;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setString(3, fullName);
                ps.executeUpdate();
            }

            return RegisterResult.SUCCESS;

        } catch (SQLException e) {
            e.printStackTrace();
            return RegisterResult.ERROR;
        }
    }

    public static void main(String[] args) {
        UserDAO dao = new UserDAO();

        User ok = dao.authenticate("admin", "admin123");
        System.out.println("Valid login:   " + ok);

        User bad = dao.authenticate("admin", "wrongpw");
        System.out.println("Invalid login: " + bad);

        System.out.println("Register new:  " + dao.register("testuser", "pw123", "Test User"));
        System.out.println("Register dupe: " + dao.register("admin", "x", "Dup"));
    }
}