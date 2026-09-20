package com.dreamstop.server.handler;

import com.dreamstop.common.dto.NotificationDTO;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.NotificationDAO;
import com.dreamstop.server.network.ClientHandler;
import com.google.gson.JsonObject;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(NotificationRequestHandler.class.getName());
    private final NotificationDAO notificationDAO;

    public NotificationRequestHandler() {
        this(DAOFactory.getInstance().getNotificationDAO());
    }

    public NotificationRequestHandler(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public Response handleGetNotifications(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        boolean unreadOnly = extractUnreadOnly(request);

        try {
            List<NotificationDTO> list = notificationDAO.getByUserId(userId, unreadOnly);
            return Response.success(list, "Notifications retrieved successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve notifications for user " + userId, e);
            return Response.error("Database error retrieving notifications");
        }
    }

    public Response handleMarkNotificationRead(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int notificationId = extractNotificationId(request);

        try {
            if (notificationId <= 0) {
                notificationDAO.markAllAsRead(userId);
                return Response.success("All notifications marked as read");
            } else {
                notificationDAO.markAsRead(notificationId);
                return Response.success("Notification marked as read");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to mark notification read for user " + userId, e);
            return Response.error("Database error updating notification");
        }
    }

    private boolean extractUnreadOnly(Request request) {
        try {
            Boolean b = request.getPayloadAs(Boolean.class);
            if (b != null) return b;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && json.has("unreadOnly")) {
                return json.get("unreadOnly").getAsBoolean();
            }
        } catch (Exception ignored) {}

        return false;
    }

    private int extractNotificationId(Request request) {
        try {
            Integer id = request.getPayloadAs(Integer.class);
            if (id != null) return id;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null) {
                if (json.has("notificationId")) return json.get("notificationId").getAsInt();
                if (json.has("id")) return json.get("id").getAsInt();
            }
        } catch (Exception ignored) {}

        return 0;
    }
}