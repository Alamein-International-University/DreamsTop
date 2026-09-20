package com.dreamstop.server.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages JDBC connections, transactions, and lightweight query execution helpers.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public final class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static volatile DatabaseManager instance;
    private final DatabaseConfig config;

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionCallback<T> {
        T execute(Connection conn) throws Exception;
    }

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
                if (instance == null) instance = new DatabaseManager();
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    public boolean testConnection() {
        try {
            return queryOne("SELECT 1", rs -> true).orElse(false);
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

    // ==========================================
    // Lightweight Query & Execution Helpers
    // ==========================================

    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepare(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? Optional.ofNullable(mapper.map(rs)) : Optional.empty();
        }
    }

    public <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepare(conn, sql, params);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapper.map(rs));
        }
        return list;
    }

    public int update(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = prepare(conn, sql, params)) {
            return stmt.executeUpdate();
        }
    }

    public int insertAndGetId(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection()) {
            return insertAndGetId(conn, sql, params);
        }
    }

    public static int insertAndGetId(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(stmt, params);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Insert failed, no ID returned.");
    }

    public static PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        setParams(stmt, params);
        return stmt;
    }

    public static void setParams(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }

    // ==========================================
    // Script & Transaction Utilities
    // ==========================================

    public void initDatabase() {
        try {
            executeScript("database/schema.sql");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema", e);
        }
    }

    public void executeScript(String resourcePath) throws IOException, SQLException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Resource not found: " + resourcePath);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                 Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("//")) continue;
                    sb.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String sql = sb.toString().trim();
                        sql = sql.substring(0, sql.length() - 1).trim();
                        if (!sql.isEmpty()) {
                            try { stmt.execute(sql); } catch (SQLException e) {
                                LOGGER.warning("SQL execution failed: " + sql + " -> " + e.getMessage());
                            }
                        }
                        sb.setLength(0);
                    }
                }
            }
        }
    }

    public <T> T runInTransaction(TransactionCallback<T> callback) throws Exception {
        Connection conn = null;
        boolean original = true;
        try {
            conn = getConnection();
            original = conn.getAutoCommit();
            conn.setAutoCommit(false);
            T result = callback.execute(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(original); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
