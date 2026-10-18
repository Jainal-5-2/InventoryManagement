package com.mycompany.inventorymanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection helper.
 * Edit the constants below to match your MySQL setup.
 */
public class DBConnection {

    // ===== CHANGE THESE TO MATCH YOUR SETUP =====
    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "inventory_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "root";
    // ============================================

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    static {
        try {
            // Ensures the MySQL driver is loaded (needed for older setups)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. "
                    + "Add mysql-connector-j to your project libraries.");
            e.printStackTrace();
        }
    }

    /** Returns a fresh connection. Callers must close it (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Quick sanity test — run this class directly. */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connected to " + DATABASE + " successfully.");
                System.out.println("  MySQL version: "
                        + conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            System.err.println("✗ Connection failed: " + e.getMessage());
        }
    }
}