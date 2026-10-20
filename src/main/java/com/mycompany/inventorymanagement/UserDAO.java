package com.mycompany.inventorymanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public enum RegisterResult {
        SUCCESS, USERNAME_TAKEN, ERROR
    }

    /**
     * Thrown when the DB itself is unreachable (distinct from bad credentials).
     */
    public static class AuthException extends Exception {

        public AuthException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * @return the authenticated user, or {@code null} if the username/password
     * are wrong.
     * @throws AuthException if the database cannot be reached — callers should
     * show a connection error rather than "invalid credentials".
     */
    public User authenticate(String username, String password) throws AuthException {
        String sql = "SELECT id, username, password, full_name FROM users "
                + "WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
            return null;

        } catch (SQLException e) {
            throw new AuthException("Could not reach the database.", e);
        }
    }

    public RegisterResult register(String username, String password, String fullName) {
        String checkSql = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, full_name) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return RegisterResult.USERNAME_TAKEN;
                    }
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

    // ---- Reads for the Users panel ----
    public List<User> findAll() {
        String sql = "SELECT id, username, password, full_name, created_at "
                + "FROM users ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("full_name")
                );
                u.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public User findById(int id) {
        String sql = "SELECT id, username, password, full_name, created_at "
                + "FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("full_name")
                    );
                    u.setCreatedAt(rs.getTimestamp("created_at"));
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ---- Writes ----
    /**
     * Updates full name and password in one shot. Username is immutable from
     * the UI (it's the login key). Pass {@code null} for newPassword to keep
     * the existing password unchanged.
     *
     * @return true on success. Duplicates aren't possible here because the
     * username isn't changed.
     */
    public boolean updateUser(int id, String fullName, String newPassword) {
        String sql = newPassword == null
                ? "UPDATE users SET full_name = ? WHERE id = ?"
                : "UPDATE users SET full_name = ?, password = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            if (newPassword == null) {
                ps.setInt(2, id);
            } else {
                ps.setString(2, newPassword);
                ps.setInt(3, id);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        // stock_movements.user_id has ON DELETE SET NULL, so historical
        // movements survive the account being removed.
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
