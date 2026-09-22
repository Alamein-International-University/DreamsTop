package com.dreamstop.server.handler;

import com.dreamstop.common.dto.FriendshipDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.FriendshipDAO;
import com.dreamstop.server.dao.NotificationDAO;
import com.dreamstop.server.dao.UserDAO;
import com.dreamstop.server.network.ClientHandler;
import com.google.gson.JsonObject;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FriendRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(FriendRequestHandler.class.getName());

    private final FriendshipDAO friendshipDAO;
    private final UserDAO userDAO;
    private final NotificationDAO notificationDAO;

    public FriendRequestHandler() {
        this(
                DAOFactory.getInstance().getFriendshipDAO(),
                DAOFactory.getInstance().getUserDAO(),
                DAOFactory.getInstance().getNotificationDAO()
        );
    }

    public FriendRequestHandler(FriendshipDAO friendshipDAO, UserDAO userDAO, NotificationDAO notificationDAO) {
        this.friendshipDAO = friendshipDAO;
        this.userDAO = userDAO;
        this.notificationDAO = notificationDAO;
    }

    public Response handleGetFriends(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        try {
            List<UserDTO> friends = friendshipDAO.getFriends(userId);
            return Response.success(friends, "Friends list retrieved successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve friends for user " + userId, e);
            return Response.error("Database error retrieving friends");
        }
    }

    public Response handleSearchUsers(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        int excludeUserId = userId != null ? userId : 0;

        String query = extractQuery(request);
        if (query == null || query.trim().isEmpty()) {
            return Response.success(Collections.emptyList(), "Search query is empty");
        }

        try {
            List<UserDTO> users = userDAO.searchUsers(query.trim(), excludeUserId);
            return Response.success(users, "Users found");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to search users with query: " + query, e);
            return Response.error("Database error while searching users");
        }
    }

    public Response handleSendFriendRequest(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int targetUserId = extractTargetUserId(request);
        if (targetUserId <= 0 || targetUserId == userId) {
            return Response.badRequest("Invalid target user ID for friend request");
        }

        try {
            FriendshipDTO friendship = friendshipDAO.sendFriendRequest(userId, targetUserId);

            String senderName = "A user";
            try {
                Optional<UserDTO> sender = userDAO.findById(userId);
                if (sender.isPresent()) {
                    senderName = sender.get().getUsername();
                }
            } catch (Exception ignored) {}

            String title = "New Friend Request";
            String msg = senderName + " sent you a friend request.";
            notificationDAO.create(targetUserId, NotificationType.FRIEND_REQUEST, title, msg, null);

            client.getSessionManager().push(
                    targetUserId,
                    new ServerNotification(NotificationType.FRIEND_REQUEST, title, msg)
            );

            return Response.success(friendship, "Friend request sent successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to send friend request from " + userId + " to " + targetUserId, e);
            return Response.error("Database error sending friend request");
        }
    }

    public Response handleAcceptFriendRequest(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int requestIdOrSenderId = extractId(request);
        if (requestIdOrSenderId <= 0) {
            return Response.badRequest("Valid request ID or sender user ID is required");
        }

        try {
            int notifyRecipientUserId = requestIdOrSenderId;
            Optional<FriendshipDTO> friendshipOpt = friendshipDAO.getFriendshipById(requestIdOrSenderId);
            if (friendshipOpt.isPresent()) {
                FriendshipDTO f = friendshipOpt.get();
                notifyRecipientUserId = (f.getRequester().getId() == userId) ? f.getAddressee().getId() : f.getRequester().getId();
            }

            boolean accepted = friendshipDAO.acceptFriendRequest(requestIdOrSenderId);
            if (!accepted) {
                accepted = friendshipDAO.acceptFriendRequest(requestIdOrSenderId, userId);
            }

            if (accepted) {
                String accepterName = "A friend";
                try {
                    Optional<UserDTO> accepter = userDAO.findById(userId);
                    if (accepter.isPresent()) {
                        accepterName = accepter.get().getUsername();
                    }
                } catch (Exception ignored) {}

                String title = "Friend Request Accepted";
                String msg = accepterName + " accepted your friend request!";
                notificationDAO.create(notifyRecipientUserId, NotificationType.FRIEND_REQUEST_ACCEPTED, title, msg, null);

                client.getSessionManager().push(
                        notifyRecipientUserId,
                        new ServerNotification(NotificationType.FRIEND_REQUEST_ACCEPTED, title, msg)
                );

                return Response.success("Friend request accepted successfully");
            } else {
                return Response.badRequest("Pending friend request not found");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to accept friend request " + requestIdOrSenderId, e);
            return Response.error("Database error accepting friend request");
        }
    }

    public Response handleDeclineFriendRequest(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int requestId = extractId(request);
        if (requestId <= 0) {
            return Response.badRequest("Valid request ID is required");
        }

        try {
            boolean declined = friendshipDAO.declineFriendRequest(requestId);
            if (declined) {
                return Response.success("Friend request declined");
            } else {
                return Response.badRequest("Pending friend request not found");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to decline friend request " + requestId, e);
            return Response.error("Database error declining friend request");
        }
    }

    public Response handleRemoveFriend(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int friendUserId = extractId(request);
        if (friendUserId <= 0) {
            return Response.badRequest("Valid friend user ID is required");
        }

        try {
            boolean removed = friendshipDAO.removeFriend(userId, friendUserId);
            if (removed) {
                String userName = "A friend";
                try {
                    Optional<UserDTO> userOpt = userDAO.findById(userId);
                    if (userOpt.isPresent()) {
                        userName = userOpt.get().getUsername();
                    }
                } catch (Exception ignored) {}

                client.getSessionManager().push(
                        friendUserId,
                        new ServerNotification(NotificationType.FRIEND_REMOVED, "Friend Removed", userName + " removed you from their friends list.")
                );

                return Response.success("Friend removed successfully");
            } else {
                return Response.badRequest("Friend relationship not found");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove friend " + friendUserId + " for user " + userId, e);
            return Response.error("Database error removing friend");
        }
    }

    public Response handleGetFriendRequests(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        try {
            List<FriendshipDTO> requests = friendshipDAO.getIncomingRequests(userId);
            return Response.success(requests, "Friend requests retrieved successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve friend requests for user " + userId, e);
            return Response.error("Database error retrieving friend requests");
        }
    }

    private String extractQuery(Request request) {
        try {
            String str = request.getPayloadAs(String.class);
            if (str != null && !str.isEmpty()) return str;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && json.has("query")) {
                return json.get("query").getAsString();
            }
        } catch (Exception ignored) {}

        return null;
    }

    private int extractTargetUserId(Request request) {
        try {
            Integer id = request.getPayloadAs(Integer.class);
            if (id != null) return id;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null) {
                if (json.has("targetUserId")) return json.get("targetUserId").getAsInt();
                if (json.has("friendId")) return json.get("friendId").getAsInt();
                if (json.has("userId")) return json.get("userId").getAsInt();
            }
        } catch (Exception ignored) {}

        return 0;
    }

    private int extractId(Request request) {
        try {
            Integer id = request.getPayloadAs(Integer.class);
            if (id != null) return id;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null) {
                if (json.has("requestId")) return json.get("requestId").getAsInt();
                if (json.has("friendId")) return json.get("friendId").getAsInt();
                if (json.has("userId")) return json.get("userId").getAsInt();
                if (json.has("id")) return json.get("id").getAsInt();
            }
        } catch (Exception ignored) {}

        return 0;
    }
}