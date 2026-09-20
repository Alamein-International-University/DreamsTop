package com.dreamstop.server.handler;

import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.common.dto.WishlistItemDTO;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.server.dao.DAOFactory;
import com.dreamstop.server.dao.FriendshipDAO;
import com.dreamstop.server.dao.ItemDAO;
import com.dreamstop.server.dao.WishlistDAO;
import com.dreamstop.server.network.ClientHandler;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WishlistRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(WishlistRequestHandler.class.getName());

    private final WishlistDAO wishlistDAO;
    private final ItemDAO itemDAO;
    private final FriendshipDAO friendshipDAO;

    public WishlistRequestHandler() {
        this(
                DAOFactory.getInstance().getWishlistDAO(),
                DAOFactory.getInstance().getItemDAO(),
                DAOFactory.getInstance().getFriendshipDAO()
        );
    }

    public WishlistRequestHandler(WishlistDAO wishlistDAO, ItemDAO itemDAO, FriendshipDAO friendshipDAO) {
        this.wishlistDAO = wishlistDAO;
        this.itemDAO = itemDAO;
        this.friendshipDAO = friendshipDAO;
    }

    public Response handleGetCatalogItems(Request request, ClientHandler client) {
        try {
            List<ItemDTO> items = itemDAO.getAllItems();
            return Response.success(items, "Catalog items retrieved successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve catalog items", e);
            return Response.error("Database error while loading catalog");
        }
    }

    public Response handleGetMyWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        try {
            List<WishlistItemDTO> wishlist = wishlistDAO.getWishlistByUserId(userId);
            return Response.success(wishlist, "Wishlist retrieved successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve wishlist for user " + userId, e);
            return Response.error("Database error while loading wishlist");
        }
    }

    public Response handleAddToWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int itemId = 0;
        BigDecimal targetAmount = null;
        String notes = "";
        String priority = "MEDIUM";

        try {
            WishlistItemDTO dto = request.getPayloadAs(WishlistItemDTO.class);
            if (dto != null && dto.getItem() != null) {
                itemId = dto.getItem().getId();
                targetAmount = dto.getItem().getPrice();
            }
        } catch (Exception ignored) {}

        if (itemId == 0) {
            try {
                JsonObject json = request.getPayloadAs(JsonObject.class);
                if (json != null) {
                    if (json.has("itemId")) itemId = json.get("itemId").getAsInt();
                    if (json.has("targetAmount")) targetAmount = json.get("targetAmount").getAsBigDecimal();
                    if (json.has("targetPrice")) targetAmount = json.get("targetPrice").getAsBigDecimal();
                    if (json.has("notes")) notes = json.get("notes").getAsString();
                    if (json.has("priority")) priority = json.get("priority").getAsString();
                }
            } catch (Exception ignored) {}
        }

        if (itemId <= 0) {
            return Response.badRequest("Valid item ID is required");
        }

        try {
            if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                Optional<ItemDTO> itemOpt = itemDAO.findById(itemId);
                if (itemOpt.isEmpty()) {
                    return Response.badRequest("Catalog item does not exist");
                }
                targetAmount = itemOpt.get().getPrice();
            }

            WishlistItemDTO addedItem = wishlistDAO.addItem(userId, itemId, targetAmount, notes, priority);
            return Response.success(addedItem, "Item added to wishlist successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add item to wishlist for user " + userId, e);
            return Response.error("Database error adding item to wishlist");
        }
    }

    public Response handleRemoveFromWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int wishlistItemId = extractWishlistItemId(request);
        if (wishlistItemId <= 0) {
            return Response.badRequest("Valid wishlist item ID is required");
        }

        try {
            int ownerId = wishlistDAO.getOwnerUserId(wishlistItemId);
            if (ownerId != userId) {
                return Response.unauthorized("Cannot remove item from another user's wishlist");
            }

            boolean removed = wishlistDAO.removeItem(wishlistItemId);
            if (removed) {
                return Response.success("Item removed from wishlist");
            } else {
                return Response.error("Wishlist item not found");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove wishlist item " + wishlistItemId, e);
            return Response.error("Database error removing wishlist item");
        }
    }

    public Response handleGetFriendWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int friendId = extractFriendId(request);
        if (friendId <= 0) {
            return Response.badRequest("Valid friend ID is required");
        }

        try {
            boolean areFriends = friendshipDAO.areFriends(userId, friendId);
            if (!areFriends) {
                return Response.error("You are not friends with this user");
            }

            List<WishlistItemDTO> friendWishlist = wishlistDAO.getWishlistByUserId(friendId);
            return Response.success(friendWishlist, "Friend wishlist retrieved successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve friend wishlist for friend " + friendId, e);
            return Response.error("Database error retrieving friend wishlist");
        }
    }

    private int extractWishlistItemId(Request request) {
        try {
            Integer id = request.getPayloadAs(Integer.class);
            if (id != null) return id;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && json.has("wishlistItemId")) {
                return json.get("wishlistItemId").getAsInt();
            }
            if (json != null && json.has("id")) {
                return json.get("id").getAsInt();
            }
        } catch (Exception ignored) {}

        return 0;
    }

    private int extractFriendId(Request request) {
        try {
            Integer id = request.getPayloadAs(Integer.class);
            if (id != null) return id;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && json.has("friendId")) {
                return json.get("friendId").getAsInt();
            }
            if (json != null && json.has("userId")) {
                return json.get("userId").getAsInt();
            }
        } catch (Exception ignored) {}

        return 0;
    }
}