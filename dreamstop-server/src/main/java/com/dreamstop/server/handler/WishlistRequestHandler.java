package com.dreamstop.server.handler;

import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.common.dto.UpdateWishlistItemRequestDTO;
import com.dreamstop.common.dto.UserDTO;
import com.dreamstop.common.dto.WishlistItemDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.common.protocol.Response;
import com.dreamstop.common.protocol.ServerNotification;
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

/**
 * Handles store catalog browsing and user wishlist operations.
 */
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

        AddItemParams params = extractAddItemParams(request);
        if (params == null || (params.itemId <= 0 && (params.name == null || params.name.isBlank()))) {
            return Response.badRequest("Valid item ID or item name is required");
        }

        try {
            BigDecimal targetAmount = params.targetAmount;
            if (params.itemId <= 0 && params.name != null && !params.name.isBlank()) {
                BigDecimal initialPrice = (targetAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0)
                        ? targetAmount : new BigDecimal("100.00");
                ItemDTO created = itemDAO.create(new ItemDTO(
                        0,
                        params.name.trim(),
                        params.description != null && !params.description.isBlank() ? params.description.trim() : params.name.trim(),
                        initialPrice,
                        null,
                        params.category != null && !params.category.isBlank() ? params.category.trim() : "Custom"
                ));
                params.itemId = created.getId();
            } else if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
                Optional<ItemDTO> itemOpt = itemDAO.findById(params.itemId);
                if (itemOpt.isEmpty()) {
                    return Response.badRequest("Catalog item does not exist");
                }
                targetAmount = itemOpt.get().getPrice();
            }

            WishlistItemDTO addedItem = wishlistDAO.addItem(userId, params.itemId, targetAmount, params.notes, params.priority);

            // Broadcast to online friends so viewing friends see new items live
            try {
                List<UserDTO> friends = friendshipDAO.getFriends(userId);
                for (UserDTO friend : friends) {
                    ServerNotification notif = new ServerNotification(
                            NotificationType.WISHLIST_UPDATED,
                            "Wishlist Updated",
                            "A friend added an item to their wishlist.",
                            addedItem != null ? addedItem.getId() : null
                    );
                    notif.setExtraDataJson(String.valueOf(userId));
                    client.getSessionManager().push(friend.getId(), notif);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to broadcast wishlist addition to friends", e);
            }

            return Response.success(addedItem, "Item added to wishlist successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add item to wishlist for user " + userId, e);
            return Response.error("Database error adding item to wishlist");
        }
    }

    public Response handleUpdateWishlistItem(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        UpdateItemParams params = extractUpdateItemParams(request);
        if (params == null || params.wishlistItemId <= 0) {
            return Response.badRequest("Valid wishlist item ID is required");
        }

        try {
            int ownerId = wishlistDAO.getOwnerUserId(params.wishlistItemId);
            if (ownerId != userId) {
                return Response.unauthorized("Cannot edit another user's wishlist item");
            }

            boolean updated = wishlistDAO.updateItem(
                    params.wishlistItemId,
                    params.targetAmount,
                    params.notes,
                    params.priority
            );

            if (!updated) {
                return Response.error("Wishlist item not found");
            }

            Optional<WishlistItemDTO> updatedItemOpt = wishlistDAO.getWishlistItemById(params.wishlistItemId);
            WishlistItemDTO updatedItem = updatedItemOpt.orElse(null);

            // Broadcast to online friends so viewing friends see updated items live
            try {
                List<UserDTO> friends = friendshipDAO.getFriends(userId);
                for (UserDTO friend : friends) {
                    ServerNotification notif = new ServerNotification(
                            NotificationType.WISHLIST_UPDATED,
                            "Wishlist Updated",
                            "A friend updated an item in their wishlist.",
                            params.wishlistItemId
                    );
                    notif.setExtraDataJson(String.valueOf(userId));
                    client.getSessionManager().push(friend.getId(), notif);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to broadcast wishlist update to friends", e);
            }

            return Response.success(updatedItem, "Wishlist item updated successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update wishlist item " + params.wishlistItemId, e);
            return Response.error("Database error updating wishlist item");
        }
    }

    public Response handleRemoveFromWishlist(Request request, ClientHandler client) {
        Integer userId = client.resolveUserId(request);
        if (userId == null) {
            return Response.unauthorized("Unauthorized request");
        }

        int wishlistItemId = extractId(request, "wishlistItemId", "id");
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
                // Broadcast to online friends so viewing friends see removal live
                try {
                    List<UserDTO> friends = friendshipDAO.getFriends(userId);
                    for (UserDTO friend : friends) {
                        ServerNotification notif = new ServerNotification(
                                NotificationType.WISHLIST_UPDATED,
                                "Wishlist Updated",
                                "A friend removed an item from their wishlist."
                        );
                        notif.setExtraDataJson(String.valueOf(userId));
                        client.getSessionManager().push(friend.getId(), notif);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to broadcast wishlist removal to friends", e);
                }

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

        int friendId = extractId(request, "friendId", "userId", "id");
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

    private int extractId(Request request, String... propertyNames) {
        if (request == null) return 0;
        try {
            Integer id = request.getPayloadAs(Integer.class);
            if (id != null) return id;
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null && propertyNames != null) {
                for (String prop : propertyNames) {
                    if (json.has(prop)) {
                        return json.get(prop).getAsInt();
                    }
                }
            }
        } catch (Exception ignored) {}

        return 0;
    }

    private AddItemParams extractAddItemParams(Request request) {
        if (request == null) return null;
        AddItemParams p = new AddItemParams();

        try {
            WishlistItemDTO dto = request.getPayloadAs(WishlistItemDTO.class);
            if (dto != null && dto.getItem() != null) {
                p.itemId = dto.getItem().getId();
                p.targetAmount = dto.getTargetAmount() != null ? dto.getTargetAmount() : dto.getItem().getPrice();
                p.notes = dto.getNotes() != null ? dto.getNotes() : "";
                p.priority = dto.getPriority() != null ? dto.getPriority() : "MEDIUM";
                return p;
            }
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null) {
                if (json.has("itemId")) p.itemId = json.get("itemId").getAsInt();
                if (json.has("targetAmount")) p.targetAmount = json.get("targetAmount").getAsBigDecimal();
                else if (json.has("targetPrice")) p.targetAmount = json.get("targetPrice").getAsBigDecimal();
                if (json.has("notes")) p.notes = json.get("notes").getAsString();
                if (json.has("priority")) p.priority = json.get("priority").getAsString();
                if (json.has("name")) p.name = json.get("name").getAsString();
                else if (json.has("customName")) p.name = json.get("customName").getAsString();
                if (json.has("description")) p.description = json.get("description").getAsString();
                if (json.has("category")) p.category = json.get("category").getAsString();
                return p;
            }
        } catch (Exception ignored) {}

        return null;
    }

    private UpdateItemParams extractUpdateItemParams(Request request) {
        if (request == null) return null;
        UpdateItemParams p = new UpdateItemParams();

        try {
            UpdateWishlistItemRequestDTO dto = request.getPayloadAs(UpdateWishlistItemRequestDTO.class);
            if (dto != null && dto.getWishlistItemId() > 0) {
                p.wishlistItemId = dto.getWishlistItemId();
                p.targetAmount = dto.getTargetAmount();
                p.notes = dto.getNotes() != null ? dto.getNotes() : "";
                p.priority = dto.getPriority() != null ? dto.getPriority() : "MEDIUM";
                return p;
            }
        } catch (Exception ignored) {}

        try {
            JsonObject json = request.getPayloadAs(JsonObject.class);
            if (json != null) {
                if (json.has("wishlistItemId")) p.wishlistItemId = json.get("wishlistItemId").getAsInt();
                else if (json.has("id")) p.wishlistItemId = json.get("id").getAsInt();

                if (json.has("targetAmount")) p.targetAmount = json.get("targetAmount").getAsBigDecimal();
                else if (json.has("targetPrice")) p.targetAmount = json.get("targetPrice").getAsBigDecimal();

                if (json.has("notes")) p.notes = json.get("notes").getAsString();
                if (json.has("priority")) p.priority = json.get("priority").getAsString();
                return p;
            }
        } catch (Exception ignored) {}

        return null;
    }

    private static class AddItemParams {
        int itemId;
        String name;
        String description;
        String category;
        BigDecimal targetAmount;
        String notes = "";
        String priority = "MEDIUM";
    }

    private static class UpdateItemParams {
        int wishlistItemId;
        BigDecimal targetAmount;
        String notes = "";
        String priority = "MEDIUM";
    }
}