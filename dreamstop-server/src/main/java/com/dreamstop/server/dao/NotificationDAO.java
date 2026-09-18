package com.dreamstop.server.dao;

import com.dreamstop.common.dto.NotificationDTO;
import com.dreamstop.common.model.NotificationType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for user notifications.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public interface NotificationDAO {

    /**
     * Creates a new notification for a recipient user.
     */
    NotificationDTO create(int recipientId, NotificationType type, String title, String message, Integer relatedItemId) throws SQLException;

    /**
     * Creates a new notification within an ongoing transactional connection.
     */
    NotificationDTO create(int recipientId, NotificationType type, String title, String message, Integer relatedItemId, Connection conn) throws SQLException;

    /**
     * Retrieves notifications for a given user, optionally filtering by unread only.
     */
    List<NotificationDTO> getByUserId(int userId, boolean unreadOnly) throws SQLException;

    /**
     * Marks a single notification as read.
     */
    boolean markAsRead(int notificationId) throws SQLException;

    /**
     * Marks all notifications for a given user as read.
     */
    boolean markAllAsRead(int userId) throws SQLException;

    /**
     * Returns the count of unread notifications for a user.
     */
    int getUnreadCount(int userId) throws SQLException;
}
