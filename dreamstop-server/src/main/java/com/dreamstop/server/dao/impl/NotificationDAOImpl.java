package com.dreamstop.server.dao.impl;

import com.dreamstop.common.dto.NotificationDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.server.dao.NotificationDAO;
import com.dreamstop.server.database.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Super-clean, compact JDBC implementation of {@link NotificationDAO}.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class NotificationDAOImpl implements NotificationDAO {

    private final DatabaseManager db;

    public NotificationDAOImpl() {
        this(DatabaseManager.getInstance());
    }

    public NotificationDAOImpl(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public NotificationDTO create(int recipientId, NotificationType type, String title, String message, Integer relId)
            throws SQLException {
        try (Connection conn = db.getConnection()) {
            return create(recipientId, type, title, message, relId, conn);
        }
    }

    @Override
    public NotificationDTO create(int recipientId, NotificationType type, String title, String message, Integer relId,
            Connection conn) throws SQLException {
        String sql = "INSERT INTO notifications (recipient_id, type, title, message, related_item_id, is_read) VALUES (?, ?, ?, ?, ?, FALSE)";
        int id = DatabaseManager.insertAndGetId(conn, sql, recipientId, type.name(), title, message, relId);
        return new NotificationDTO(id, recipientId, type, title, message, relId);
    }

    @Override
    public List<NotificationDTO> getByUserId(int userId, boolean unreadOnly) throws SQLException {
        String sql = "SELECT id, recipient_id, type, title, message, related_item_id, is_read, created_at FROM notifications "
                +
                "WHERE recipient_id = ? " + (unreadOnly ? "AND is_read = FALSE " : "") + "ORDER BY created_at DESC";
        return db.queryList(sql, this::mapNotification, userId);
    }

    @Override
    public boolean markAsRead(int notificationId) throws SQLException {
        return db.update("UPDATE notifications SET is_read = TRUE WHERE id = ?", notificationId) > 0;
    }

    @Override
    public boolean markAllAsRead(int userId) throws SQLException {
        return db.update("UPDATE notifications SET is_read = TRUE WHERE recipient_id = ? AND is_read = FALSE",
                userId) > 0;
    }

    @Override
    public int getUnreadCount(int userId) throws SQLException {
        return db.queryOne("SELECT COUNT(*) AS cnt FROM notifications WHERE recipient_id = ? AND is_read = FALSE",
                rs -> rs.getInt("cnt"), userId).orElse(0);
    }

    private NotificationDTO mapNotification(ResultSet rs) throws SQLException {
        int relId = rs.getInt("related_item_id");
        Integer relatedItemId = rs.wasNull() ? null : relId;
        Timestamp ts = rs.getTimestamp("created_at");
        NotificationDTO dto = new NotificationDTO(
                rs.getInt("id"), rs.getInt("recipient_id"), NotificationType.valueOf(rs.getString("type")),
                rs.getString("title"), rs.getString("message"), relatedItemId);
        dto.setRead(rs.getBoolean("is_read"));
        if (ts != null)
            dto.setCreatedAt(ts.toLocalDateTime());
        return dto;
    }
}
