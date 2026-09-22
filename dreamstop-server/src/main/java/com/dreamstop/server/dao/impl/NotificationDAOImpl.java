package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.NotificationDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.server.dao.NotificationDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard JDBC implementation of {@link NotificationDAO}.
 */
public class NotificationDAOImpl implements NotificationDAO {

    public NotificationDAOImpl() {
    }

    public NotificationDAOImpl(DatabaseManager db) {
    }

    @Override
    public NotificationDTO create(int recipientId, NotificationType type, String title, String message, Integer relId)
            throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return create(recipientId, type, title, message, relId, conn);
        }
    }

    @Override
    public NotificationDTO create(int recipientId, NotificationType type, String title, String message, Integer relId,
            Connection conn) throws SQLException {
        String sql = "INSERT INTO notifications (recipient_id, type, title, message, related_item_id, is_read) VALUES (?, ?, ?, ?, ?, FALSE)";
        int id = 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, recipientId);
            stmt.setString(2, type.name());
            stmt.setString(3, title);
            stmt.setString(4, message);
            if (relId != null) {
                stmt.setInt(5, relId);
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    id = keys.getInt(1);
                }
            }
        }

        return new NotificationDTO(id, recipientId, type, title, message, relId);
    }

    @Override
    public List<NotificationDTO> getByUserId(int userId, boolean unreadOnly) throws SQLException {
        List<NotificationDTO> list = new ArrayList<>();
        String sql = "SELECT id, recipient_id, type, title, message, related_item_id, is_read, created_at FROM notifications "
                + "WHERE recipient_id = ? " + (unreadOnly ? "AND is_read = FALSE " : "") + "ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapNotification(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE recipient_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM notifications WHERE recipient_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cnt");
                }
            }
        }
        return 0;
    }

    private NotificationDTO mapNotification(ResultSet rs) throws SQLException {
        int relId = rs.getInt("related_item_id");
        Integer relatedItemId = rs.wasNull() ? null : relId;
        Timestamp ts = rs.getTimestamp("created_at");
        NotificationDTO dto = new NotificationDTO(
                rs.getInt("id"),
                rs.getInt("recipient_id"),
                NotificationType.valueOf(rs.getString("type")),
                rs.getString("title"),
                rs.getString("message"),
                relatedItemId
        );
        dto.setRead(rs.getBoolean("is_read"));
        if (ts != null) {
            dto.setCreatedAt(ts.toLocalDateTime());
        }
        return dto;
    }
}
