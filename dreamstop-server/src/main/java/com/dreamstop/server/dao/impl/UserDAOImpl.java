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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Standard JDBC implementation of {@link UserDAO}.
 */
public class UserDAOImpl implements UserDAO {

    public UserDAOImpl() {
    }

    public UserDAOImpl(DatabaseManager db) {
    }

    @Override
    public UserDTO register(RegisterRequestDTO req, String fullName, String avatarColor, String bio) throws SQLException {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, balance, avatar_color, bio) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String hash = PasswordUtil.hashPassword(req.getPassword());
        BigDecimal bal = req.getInitialBalance() != null ? req.getInitialBalance() : BigDecimal.ZERO;
        String color = (avatarColor != null && !avatarColor.isBlank()) ? avatarColor : "#6366F1";
        String name = (fullName != null && !fullName.isBlank()) ? fullName : req.getUsername();

        int id = 0;
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, req.getUsername().trim());
            stmt.setString(2, req.getEmail().trim().toLowerCase());
            stmt.setString(3, hash);
            stmt.setString(4, name);
            stmt.setBigDecimal(5, bal);
            stmt.setString(6, color);
            stmt.setString(7, bio);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
        }

        return new UserDTO(id, req.getUsername(), req.getEmail(), bal);
    }

    @Override
    public Optional<UserDTO> authenticate(String usernameOrEmail, String plainPassword) throws SQLException {
        String sql = "SELECT id, username, email, password_hash, balance FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usernameOrEmail.trim());
            stmt.setString(2, usernameOrEmail.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(plainPassword, hash)) {
                        return Optional.of(mapUser(rs));
                    }
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserDTO> findById(int id) throws SQLException {
        String sql = "SELECT id, username, email, balance FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, balance FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<UserDTO> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, username, email, balance FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<UserDTO> searchUsers(String query, int excludeUserId) throws SQLException {
        List<UserDTO> list = new ArrayList<>();
        String sql = "SELECT id, username, email, balance FROM users WHERE (username LIKE ? OR full_name LIKE ? OR email LIKE ?) AND id <> ? ORDER BY username ASC LIMIT 50";
        String pattern = "%" + (query != null ? query.trim() : "") + "%";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setInt(4, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUser(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean rechargeBalance(int userId, BigDecimal amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deductBalance(int userId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, userId);
            stmt.setBigDecimal(3, amount);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean addBalance(int userId, BigDecimal amount, Connection conn) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public BigDecimal getBalance(int userId) throws SQLException {
        String sql = "SELECT balance FROM users WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal balance = rs.getBigDecimal("balance");
                    return balance != null ? balance : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private UserDTO mapUser(ResultSet rs) throws SQLException {
        return new UserDTO(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getBigDecimal("balance")
        );
    }
}
