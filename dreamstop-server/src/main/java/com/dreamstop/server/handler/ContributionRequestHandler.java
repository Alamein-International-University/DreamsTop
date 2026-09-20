package com.dreamstop.server.handler;

import com.dreamstop.common.dto.ContributeRequestDTO;
import com.dreamstop.common.dto.ContributionDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;
import com.dreamstop.server.dao.ContributionDAO;
import com.dreamstop.server.dao.ContributionResult;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.WishlistDAO;
import com.dreamstop.server.network.ClientHandler;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContributionRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(ContributionRequestHandler.class.getName());

    private final ContributionDAO contributionDAO;
    private final WishlistDAO wishlistDAO;

    public ContributionRequestHandler() {
        this(
                DAOFactory.getInstance().getContributionDAO(),
                DAOFactory.getInstance().getWishlistDAO()
        );
    }

    public ContributionRequestHandler(ContributionDAO contributionDAO, WishlistDAO wishlistDAO) {
        this.contributionDAO = contributionDAO;
        this.wishlistDAO = wishlistDAO;
    }

    public Response handleContribution(Request request, ClientHandler client) {
        Integer contributorId = client.resolveUserId(request);
        if (contributorId == null) {
            return Response.unauthorized("You must be logged in to contribute");
        }

        int wishlistItemId = extractWishlistItemId(request);
        BigDecimal amount = extractAmount(request);

        if (wishlistItemId <= 0 || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Response.badRequest("Valid wishlist item ID and positive amount are required");
        }

        try {
            int ownerId = wishlistDAO.getOwnerUserId(wishlistItemId);
            if (ownerId == contributorId) {
                return Response.badRequest("You cannot contribute to your own wishlist item");
            }

            ContributionResult result = contributionDAO.contribute(contributorId, wishlistItemId, amount);
            if (!result.isSuccess()) {
                return Response.badRequest(result.getMessage());
            }

            dispatchRealtimeNotifications(client, wishlistItemId, ownerId, contributorId, result);

            return Response.success(result, result.getMessage());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during contribution by user " + contributorId, e);
            return Response.error("Database error while processing contribution: " + e.getMessage());
        }
    }

    private void dispatchRealtimeNotifications(ClientHandler client, int wishlistItemId, int ownerId, int contributorId, ContributionResult result) {
        try {
            client.getSessionManager().push(
                    ownerId,
                    new ServerNotification(
                            NotificationType.CONTRIBUTION_RECEIVED,
                            "Contribution Received",
                            "You received a contribution of " + result.getAcceptedAmount() + " EGP!",
                            wishlistItemId
                    )
            );

            if (result.isItemCompleted()) {
                client.getSessionManager().push(
                        ownerId,
                        new ServerNotification(
                                NotificationType.ITEM_COMPLETED_RECEIVER,
                                "Gift Goal Reached!",
                                "Your wishlist item has been 100% fully funded!",
                                wishlistItemId
                        )
                );

                List<Integer> buyers = contributionDAO.getUniqueContributorIds(wishlistItemId);
                for (int buyerId : buyers) {
                    client.getSessionManager().push(
                            buyerId,
                            new ServerNotification(
                                    NotificationType.ITEM_COMPLETED_BUYER,
                                    "Gift Completed",
                                    "The wishlist item you contributed to is now fully funded!",
                                    wishlistItemId
                            )
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to dispatch real-time push notifications for contribution", e);
        }
    }

    private int extractWishlistItemId(Request request) {
        try {
            ContributeRequestDTO dto = request.getPayloadAs(ContributeRequestDTO.class);
            if (dto != null && dto.getWishlistItemId() > 0) {
                return dto.getWishlistItemId();
            }
        } catch (Exception ignored) {}

        try {
            ContributionDTO dto = request.getPayloadAs(ContributionDTO.class);
            if (dto != null && dto.getWishlistItemId() > 0) {
                return dto.getWishlistItemId();
            }
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && json.has("wishlistItemId")) {
                return json.get("wishlistItemId").getAsInt();
            }
        } catch (Exception ignored) {}

        return 0;
    }

    private BigDecimal extractAmount(Request request) {
        try {
            ContributeRequestDTO dto = request.getPayloadAs(ContributeRequestDTO.class);
            if (dto != null && dto.getAmount() != null) {
                return dto.getAmount();
            }
        } catch (Exception ignored) {}

        try {
            ContributionDTO dto = request.getPayloadAs(ContributionDTO.class);
            if (dto != null && dto.getAmount() != null) {
                return dto.getAmount();
            }
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && json.has("amount")) {
                return json.get("amount").getAsBigDecimal();
            }
        } catch (Exception ignored) {}

        return null;
    }
}