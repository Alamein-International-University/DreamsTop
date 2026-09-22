package com.dreamstop.service;

import com.dreamstop.common.dto.ContributeRequestDTO;
import com.dreamstop.common.dto.ItemDTO;
import com.dreamstop.common.dto.WishlistItemDTO;
import com.dreamstop.common.model.NotificationType;
import com.dreamstop.common.model.RequestType;
import com.dreamstop.common.protocol.JsonUtils;
import com.dreamstop.common.protocol.Request;
import com.dreamstop.model.Item;
import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;
import com.dreamstop.network.NetworkClient;
import com.dreamstop.util.ModelMapper;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class WishlistService {

    private static WishlistService instance;

    private final ObservableList<WishlistItem> myWishlist = FXCollections.observableArrayList();
    private final ObservableList<Item> catalog = FXCollections.observableArrayList();

    private WishlistService() {
        catalog.setAll(MockDataFactory.getCatalogItems());
        loadCatalogFromNetwork();
        refreshMyWishlist();

        NetworkClient.getInstance().addNotificationListener(notification -> {
            if (notification == null) return;
            NotificationType type = notification.getType();
            if (type == NotificationType.CONTRIBUTION_RECEIVED
                    || type == NotificationType.ITEM_COMPLETED_RECEIVER
                    || type == NotificationType.ITEM_COMPLETED_BUYER) {
                refreshMyWishlist();
            }
        });
    }

    public static synchronized WishlistService getInstance() {
        if (instance == null) {
            instance = new WishlistService();
        }
        return instance;
    }

    public void loadCatalogFromNetwork() {
        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected()) {
            Request req = new Request(RequestType.GET_CATALOG_ITEMS);
            network.sendRequestAsync(req).thenAccept(response -> {
                if (response.isSuccess() && response.getDataJson() != null) {
                    Type listType = new TypeToken<List<ItemDTO>>() {}.getType();
                    List<ItemDTO> dtos = JsonUtils.fromJson(response.getDataJson(), listType);
                    if (dtos != null && !dtos.isEmpty()) {
                        List<Item> items = dtos.stream().map(ModelMapper::toItem).collect(Collectors.toList());
                        Platform.runLater(() -> catalog.setAll(items));
                    }
                }
            });
        }
    }

    public void refreshMyWishlist() {
        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            Request req = Request.of(RequestType.GET_MY_WISHLIST, network.getSessionToken(), null);
            network.sendRequestAsync(req).thenAccept(response -> {
                if (response.isSuccess() && response.getDataJson() != null) {
                    Type listType = new TypeToken<List<WishlistItemDTO>>() {}.getType();
                    List<WishlistItemDTO> dtos = JsonUtils.fromJson(response.getDataJson(), listType);
                    if (dtos != null) {
                        List<WishlistItem> items = dtos.stream().map(ModelMapper::toWishlistItem).collect(Collectors.toList());
                        Platform.runLater(() -> myWishlist.setAll(items));
                    }
                }
            });
        } else {
            User me = MockDataFactory.getCurrentUser();
            List<WishlistItem> list = MockDataFactory.getWishlistsByUser().getOrDefault(me.getId(), new ArrayList<>());
            myWishlist.setAll(list);
        }
    }

    public ObservableList<WishlistItem> getMyWishlist() {
        return myWishlist;
    }

    public ObservableList<Item> getCatalog() {
        return catalog;
    }

    public WishlistItem addItemToMyWishlist(Item item, String notes, double targetPrice, String priority) {
        User me = MockDataFactory.getCurrentUser();
        double price = targetPrice > 0 ? targetPrice : item.getPrice();

        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int itemId = ModelMapper.parseNumericId(item.getId());
                JsonObject payload = new JsonObject();
                payload.addProperty("itemId", itemId);
                payload.addProperty("targetAmount", price);
                payload.addProperty("notes", notes != null ? notes : "");
                payload.addProperty("priority", priority != null ? priority : "MEDIUM");

                Request req = Request.of(RequestType.ADD_TO_WISHLIST, network.getSessionToken(), payload);
                network.sendRequestAsync(req).thenAccept(res -> {
                    if (res.isSuccess()) {
                        Platform.runLater(this::refreshMyWishlist);
                    }
                });
            } catch (Exception ignored) {}
        }

        WishlistItem newItem = new WishlistItem(
                "wl-" + UUID.randomUUID().toString().substring(0, 8),
                me.getId(),
                item,
                notes,
                price,
                0.0,
                priority
        );

        List<WishlistItem> userList = MockDataFactory.getWishlistsByUser().computeIfAbsent(me.getId(), k -> new ArrayList<>());
        userList.add(newItem);
        myWishlist.add(newItem);
        return newItem;
    }

    public WishlistItem addCustomItemToMyWishlist(String name, String description, String category, double targetAmount, String iconEmoji, String priority, String notes) {
        String itemId = "itm-" + UUID.randomUUID().toString().substring(0, 8);
        Item customItem = new Item(
                itemId,
                name,
                description != null && !description.isBlank() ? description : name,
                category != null && !category.isBlank() ? category : "Custom",
                targetAmount > 0 ? targetAmount : 100.0,
                iconEmoji != null && !iconEmoji.isBlank() ? iconEmoji : "🎁"
        );
        catalog.add(customItem);
        return addItemToMyWishlist(customItem, notes, targetAmount, priority);
    }

    public boolean updateWishlistItem(WishlistItem item, String notes, double targetPrice, String priority) {
        item.setNotes(notes);
        if (targetPrice > 0) {
            item.setTargetAmount(targetPrice);
        }
        if (priority != null) {
            item.setPriority(priority);
        }

        int idx = myWishlist.indexOf(item);
        if (idx >= 0) {
            myWishlist.set(idx, item);
            return true;
        }
        return false;
    }

    public boolean deleteWishlistItem(WishlistItem item) {
        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int wishlistItemId = ModelMapper.parseNumericId(item.getId());
                JsonObject payload = new JsonObject();
                payload.addProperty("wishlistItemId", wishlistItemId);
                Request req = Request.of(RequestType.REMOVE_FROM_WISHLIST, network.getSessionToken(), payload);
                network.sendRequestAsync(req);
            } catch (Exception ignored) {}
        }

        User me = MockDataFactory.getCurrentUser();
        List<WishlistItem> userList = MockDataFactory.getWishlistsByUser().get(me.getId());
        if (userList != null) {
            userList.remove(item);
        }
        return myWishlist.remove(item);
    }

    public ObservableList<WishlistItem> getFriendWishlist(User friend) {
        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int friendId = ModelMapper.parseNumericId(friend.getId());
                JsonObject payload = new JsonObject();
                payload.addProperty("friendId", friendId);
                Request req = Request.of(RequestType.GET_FRIEND_WISHLIST, network.getSessionToken(), payload);
                var res = network.sendRequest(req, 3);
                if (res != null && res.isSuccess() && res.getDataJson() != null) {
                    Type listType = new TypeToken<List<WishlistItemDTO>>() {}.getType();
                    List<WishlistItemDTO> dtos = JsonUtils.fromJson(res.getDataJson(), listType);
                    if (dtos != null) {
                        List<WishlistItem> items = dtos.stream().map(ModelMapper::toWishlistItem).collect(Collectors.toList());
                        return FXCollections.observableArrayList(items);
                    }
                }
            } catch (Exception ignored) {}
        }

        List<WishlistItem> list = MockDataFactory.getWishlistsByUser().getOrDefault(friend.getId(), Collections.emptyList());
        return FXCollections.observableArrayList(list);
    }

    public ContributionResult contributeToFriendItem(WishlistItem item, double amount) {
        if (amount <= 0 || item.isCompleted()) return null;

        NetworkClient network = NetworkClient.getInstance();
        if (network.isConnected() && network.getSessionToken() != null) {
            try {
                int wishlistItemId = ModelMapper.parseNumericId(item.getId());
                ContributeRequestDTO payload = new ContributeRequestDTO(wishlistItemId, BigDecimal.valueOf(amount));
                Request req = Request.of(RequestType.CONTRIBUTE, network.getSessionToken(), payload);
                network.sendRequestAsync(req);
            } catch (Exception ignored) {}
        }

        double remaining = item.getRemainingAmount();
        double accepted  = Math.min(amount, remaining);
        double refunded  = amount - accepted;

        item.setCurrentAmount(item.getCurrentAmount() + accepted);
        return new ContributionResult(accepted, refunded);
    }

    public double getTotalWishlistValue() {
        return myWishlist.stream().mapToDouble(WishlistItem::getTargetAmount).sum();
    }

    public double getTotalFundedValue() {
        return myWishlist.stream().mapToDouble(WishlistItem::getCurrentAmount).sum();
    }

    public long getCompletedItemsCount() {
        return myWishlist.stream().filter(WishlistItem::isCompleted).count();
    }
}
