package com.dreamstop.server.handler;

import com.dreamstop.common.dto.ContributeRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;
import com.dreamstop.server.dao.ContributionDAO;
import com.dreamstop.server.dao.ContributionResult;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.FriendshipDAO;
import com.dreamstop.server.dao.WishlistDAO;
import com.dreamstop.server.network.ClientHandler;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles incoming contribution requests and notifies recipients in real-time.
 */
public class ContributionRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(ContributionRequestHandler.class.getName());

    private final ContributionDAO contributionDAO;
    private final WishlistDAO wishlistDAO;
    private final FriendshipDAO friendshipDAO;

    public ContributionRequestHandler() {
        this(DAOFactory.getInstance().getContributionDAO(), DAOFactory.getInstance().getWishlistDAO(), DAOFactory.getInstance().getFriendshipDAO());
    }

    public ContributionRequestHandler(ContributionDAO contributionDAO, WishlistDAO wishlistDAO) {
        this(contributionDAO, wishlistDAO, DAOFactory.getInstance().getFriendshipDAO());
    }

    public ContributionRequestHandler(ContributionDAO contributionDAO, WishlistDAO wishlistDAO, FriendshipDAO friendshipDAO) {
        this.contributionDAO = contributionDAO;
        this.wishlistDAO = wishlistDAO;
        this.friendshipDAO = friendshipDAO;
    }

    public Response handleContribution(Request request, ClientHandler client) {
        Integer contributorId = client.resolveUserId(request);
        if (contributorId == null) {
            return Response.unauthorized("You must be logged in to contribute");
        }

        ContributeRequestDTO contributeReq = extractContributeRequest(request);
        if (contributeReq == null || contributeReq.getWishlistItemId() <= 0 || contributeReq.getAmount() == null || contributeReq.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return Response.badRequest("Valid wishlist item ID and positive amount are required");
        }

        int wishlistItemId = contributeReq.getWishlistItemId();
        BigDecimal amount = contributeReq.getAmount();

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
            if (client == null || client.getSessionManager() == null) {
                return;
            }

            client.getSessionManager().push(
                    ownerId,
                    new ServerNotification(
                            NotificationType.CONTRIBUTION_RECEIVED,
                            "Contribution Received",
                            "You received a contribution of " + result.getAcceptedAmount() + " EGP!",
                            wishlistItemId
                    )
            );

            // Broadcast WISHLIST_UPDATED to all online friends of owner so viewing clients see progress update live
            try {
                List<UserDTO> friends = friendshipDAO.getFriends(ownerId);
                for (UserDTO friend : friends) {
                    ServerNotification friendNotif = new ServerNotification(
                            NotificationType.WISHLIST_UPDATED,
                            "Contribution Update",
                            "A wishlist item received a contribution.",
                            wishlistItemId
                    );
                    friendNotif.setExtraDataJson(String.valueOf(ownerId));
                    client.getSessionManager().push(friend.getId(), friendNotif);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to broadcast contribution update to friends", e);
            }

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

    private ContributeRequestDTO extractContributeRequest(Request request) {
        if (request == null) {
            return null;
        }

        try {
            ContributeRequestDTO dto = request.getPayloadAs(ContributeRequestDTO.class);
            if (dto != null && dto.getWishlistItemId() > 0 && dto.getAmount() != null) {
                return dto;
            }
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && json.has("wishlistItemId") && json.has("amount")) {
                int itemId = json.get("wishlistItemId").getAsInt();
                BigDecimal amount = json.get("amount").getAsBigDecimal();
                return new ContributeRequestDTO(itemId, amount);
            }
        } catch (Exception ignored) {}

        return null;
    }
}