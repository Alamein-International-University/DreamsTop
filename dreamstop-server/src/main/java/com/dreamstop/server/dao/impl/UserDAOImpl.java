package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.RegisterRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.server.dao.UserDAO;
import com.dreamstop.server.database.DatabaseManager;
import com.dreamstop.server.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Concise, clean JDBC implementation of {@link UserDAO}.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class UserDAOImpl implements UserDAO {

    private final DatabaseManager db;

    public UserDAOImpl() {
        this(DatabaseManager.getInstance());
    }

    public UserDAOImpl(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public UserDTO register(RegisterRequestDTO req, String fullName, String avatarColor, String bio) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, balance, avatar_color, bio) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String hash = PasswordUtil.hashPassword(req.getPassword());
        BigDecimal bal = req.getInitialBalance() != null ? req.getInitialBalance() : BigDecimal.ZERO;
        String color = (avatarColor != null && !avatarColor.isBlank()) ? avatarColor : "#6366F1";
        String name = (fullName != null && !fullName.isBlank()) ? fullName : req.getUsername();

        int id = db.insertAndGetId(sql, req.getUsername().trim(), req.getEmail().trim().toLowerCase(), hash, name, bal, color, bio);
        return new UserDTO(id, req.getUsername(), req.getEmail(), bal);
    }

    @Override
    public Optional<UserDTO> authenticate(String usernameOrEmail, String plainPassword) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, balance FROM users WHERE username = ? OR email = ?";
        return db.queryOne(sql, rs -> {
            if (PasswordUtil.verifyPassword(plainPassword, rs.getString("password_hash"))) {
                return mapUser(rs);
            }
            return null;
        }, usernameOrEmail.trim(), usernameOrEmail.trim().toLowerCase());
    }

    @Override
    public Optional<UserDTO> findById(int id) throws SQLException {
        return db.queryOne("SELECT id, username, email, balance FROM users WHERE id = ?", this::mapUser, id);
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) throws SQLException {
        return db.queryOne("SELECT id, username, email, balance FROM users WHERE username = ?", this::mapUser, username.trim());
    }

    @Override
    public Optional<UserDTO> findByEmail(String email) throws SQLException {
        return db.queryOne("SELECT id, username, email, balance FROM users WHERE email = ?", this::mapUser, email.trim().toLowerCase());
    }

    @Override
    public List<UserDTO> searchUsers(String query, int excludeUserId) throws SQLException {
        String sql = "SELECT id, username, email, balance FROM users WHERE (username LIKE ? OR full_name LIKE ? OR email LIKE ?) AND id <> ? ORDER BY username ASC LIMIT 50";
        String pattern = "%" + (query != null ? query.trim() : "") + "%";
        return db.queryList(sql, this::mapUser, pattern, pattern, pattern, excludeUserId);
    }

    @Override
    public boolean rechargeBalance(int userId, BigDecimal amount) throws SQLException {
        return db.update("UPDATE users SET balance = balance + ? WHERE id = ?", amount, userId) > 0;
    }

    @Override
    public boolean deductBalance(int userId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        try (PreparedStatement stmt = DatabaseManager.prepare(conn, sql, amount, userId, amount)) {
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean addBalance(int userId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.prepare(conn, sql, amount, userId)) {
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public BigDecimal getBalance(int userId) throws SQLException {
        return db.queryOne("SELECT balance FROM users WHERE id = ?", rs -> rs.getBigDecimal("balance"), userId).orElse(BigDecimal.ZERO);
    }

    private UserDTO mapUser(ResultSet rs) throws SQLException {
        return new UserDTO(rs.getInt("id"), rs.getString("username"), rs.getString("email"), rs.getBigDecimal("balance"));
    }
}
