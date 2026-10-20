package com.mycompany.inventorymanagement;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection helper.
 *
 * <p>
 * Settings are read from {@code db.properties} on the classpath
 * ({@code src/main/resources/db.properties}). If a key is missing the fallback
 * defaults below are used, so the app still runs out of the box.</p>
 */
public class DBConnection {

    private static final String CONFIG_RESOURCE = "db.properties";

    // Fallback values — only used if db.properties is missing or incomplete.
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "inventory_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties cfg = loadConfig();

        String host = cfg.getProperty("db.host", DEFAULT_HOST);
        String port = cfg.getProperty("db.port", DEFAULT_PORT);
        String db = cfg.getProperty("db.name", DEFAULT_DATABASE);
        USER = cfg.getProperty("db.user", DEFAULT_USER);
        PASSWORD = cfg.getProperty("db.password", DEFAULT_PASSWORD);

        URL = "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. "
                    + "Add mysql-connector-j to your project libraries.");
            e.printStackTrace();
        }
    }

    private static Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = DBConnection.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_RESOURCE)) {

            if (in == null) {
                System.err.println("Warning: " + CONFIG_RESOURCE
                        + " not found on classpath; using default DB settings.");
                return p;
            }
            p.load(in);

        } catch (IOException e) {
            System.err.println("Warning: could not read " + CONFIG_RESOURCE
                    + " (" + e.getMessage() + "); using default DB settings.");
        }
        return p;
    }

    /**
     * Returns a fresh connection. Callers must close it (try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Quick sanity test — run this class directly.
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Connected successfully.");
                System.out.println("  URL: " + URL);
                System.out.println("  MySQL version: "
                        + conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (SQLException e) {
            System.err.println("✗ Connection failed: " + e.getMessage());
        }
    }
}
