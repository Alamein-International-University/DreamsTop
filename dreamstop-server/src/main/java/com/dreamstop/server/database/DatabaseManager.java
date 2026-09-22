package com.dreamstop.server.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages JDBC connections and schema script execution.
 */
public final class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static volatile DatabaseManager instance;
    private final DatabaseConfig config;

    private DatabaseManager() {
        this.config = DatabaseConfig.getInstance();
        try {
            Class.forName(config.getDriver());
            DriverManager.setLoginTimeout(config.getLoginTimeout());
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Could not load JDBC Driver", e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    public boolean testConnection() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized void initDatabaseIfAvailable() {
        if (!testConnection()) {
            LOGGER.warning("Could not connect to configured database at " + config.getUrl() + ". Falling back to embedded in-memory database.");
            config.overrideConfig(
                    "org.h2.Driver",
                    "jdbc:h2:mem:dreamstop_db;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
                    "sa",
                    ""
            );
            try {
                Class.forName(config.getDriver());
                executeScript("database/schema.sql");
                executeScript("database/seed.sql");
                LOGGER.info("Embedded in-memory database initialized successfully with seed data.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize embedded database", e);
            }
        } else if (config.isAutoInit()) {
            try {
                executeScript("database/schema.sql");
                executeScript("database/seed.sql");
                LOGGER.info("Database initialized successfully with schema and seed data.");
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Auto-init scripts skipped or already executed: " + e.getMessage());
            }
        }
    }

    public void initDatabase() {
        try {
            executeScript("database/schema.sql");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
        }
    }

    public void executeScript(String resourcePath) throws IOException, SQLException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("//")) {
                        continue;
                    }
                    sb.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String sql = sb.toString().trim();
                        sql = sql.substring(0, sql.length() - 1).trim();
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException e) {
                                LOGGER.warning("SQL execution failed: " + sql + " -> " + e.getMessage());
                            }
                        }
                        sb.setLength(0);
                    }
                }
            }
        }
    }
}
