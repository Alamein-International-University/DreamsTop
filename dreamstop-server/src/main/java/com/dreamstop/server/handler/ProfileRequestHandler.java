package com.dreamstop.server.handler;

import com.dreamstop.common.dto.UpdateProfileRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.FriendshipDAO;
import com.dreamstop.server.dao.UserDAO;
import com.dreamstop.server.network.ClientHandler;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(ProfileRequestHandler.class.getName());
    private final UserDAO userDAO;
    private final FriendshipDAO friendshipDAO;

    public ProfileRequestHandler() {
        this(DAOFactory.getInstance().getUserDAO(), DAOFactory.getInstance().getFriendshipDAO());
    }

    public ProfileRequestHandler(UserDAO userDAO) {
        this(userDAO, DAOFactory.getInstance().getFriendshipDAO());
    }

    public ProfileRequestHandler(UserDAO userDAO, FriendshipDAO friendshipDAO) {
        this.userDAO = userDAO;
        this.friendshipDAO = friendshipDAO;
    }

    public Response handleUpdateProfile(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        UpdateProfileRequestDTO updateReq = request.getPayloadAs(UpdateProfileRequestDTO.class);
        if (updateReq == null || updateReq.getFullName() == null || updateReq.getFullName().trim().isEmpty()) {
            return Response.badRequest("Full name is required");
        }

        try {
            boolean updated = userDAO.updateProfile(
                    userId,
                    updateReq.getFullName().trim(),
                    updateReq.getAvatarColor(),
                    updateReq.getBio()
            );

            if (!updated) {
                return Response.error("Failed to update profile");
            }

            Optional<UserDTO> updatedUserOpt = userDAO.findById(userId);
            UserDTO updatedUser = updatedUserOpt.orElse(null);

            // Broadcast to online friends
            try {
                List<UserDTO> friends = friendshipDAO.getFriends(userId);
                for (UserDTO friend : friends) {
                    ServerNotification notif = new ServerNotification(
                            NotificationType.PROFILE_UPDATED,
                            "Profile Updated",
                            (updatedUser != null ? updatedUser.getFullName() : updateReq.getFullName().trim()) + " updated their profile."
                    );
                    if (updatedUser != null) {
                        notif.setExtraDataJson(com.dreamstop.common.protocol.JsonUtils.toJson(updatedUser));
                    }
                    client.getSessionManager().push(friend.getId(), notif);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to push profile update notification to friends", e);
            }

            return Response.success(updatedUser, "Profile updated successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update profile for user " + userId, e);
            return Response.error("Database error updating profile");
        }
    }

    public Response handleGetProfile(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        try {
            Optional<UserDTO> userOpt = userDAO.findById(userId);
            if (userOpt.isEmpty()) {
                return Response.error("User not found");
            }
            return Response.success(userOpt.get(), "Profile retrieved successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve profile for user " + userId, e);
            return Response.error("Database error retrieving profile");
        }
    }

    public Response handleRechargeBalance(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        BigDecimal amount = extractAmount(request);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.badRequest("Recharge amount must be greater than zero");
        }

        try {
            boolean success = userDAO.rechargeBalance(userId, amount);
            if (!success) {
                return Response.error("Failed to recharge balance");
            }
            BigDecimal newBalance = userDAO.getBalance(userId);
            return Response.success(newBalance, "Balance recharged successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to recharge balance for user " + userId, e);
            return Response.error("Database error while recharging balance");
        }
    }

    private BigDecimal extractAmount(Request request) {
        if (request == null) return null;
        try {
            BigDecimal direct = request.getPayloadAs(BigDecimal.class);
            if (direct != null) {
                return direct;
            }
            JsonObject obj = request.getPayloadAs(JsonObject.class);
            if (obj != null && obj.has("amount")) {
                return obj.get("amount").getAsBigDecimal();
            }
        } catch (Exception ignored) {}
        return null;
    }
}