package com.dreamstop.server.handler;

import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.UserDAO;
import com.dreamstop.server.network.ClientHandler;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(ProfileRequestHandler.class.getName());
    private final UserDAO userDAO;

    public ProfileRequestHandler() {
        this(DAOFactory.getInstance().getUserDAO());
    }

    public ProfileRequestHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
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
        try {
            BigDecimal direct = request.getPayloadAs(BigDecimal.class);
            if (direct != null) {
                return direct;
            }
        } catch (Exception ignored) {}

        try {
            JsonObject obj = request.getPayloadAs(JsonObject.class);
            if (obj != null && obj.has("amount")) {
                return obj.get("amount").getAsBigDecimal();
            }
        } catch (Exception ignored) {}

        return null;
    }
}