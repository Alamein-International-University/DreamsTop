package com.dreamstop.service;

import com.dreamstop.model.Item;
import com.dreamstop.model.User;
import com.dreamstop.model.WishlistItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class WishlistService {

    private static WishlistService instance;

    private final ObservableList<WishlistItem> myWishlist = FXCollections.observableArrayList();
    private final ObservableList<Item> catalog = FXCollections.observableArrayList();

    private WishlistService() {
        catalog.setAll(MockDataFactory.getCatalogItems());
        refreshMyWishlist();
    }

    public static synchronized WishlistService getInstance() {
        if (instance == null) {
            instance = new WishlistService();
        }
        return instance;
    }

    public void refreshMyWishlist() {
        User me = MockDataFactory.getCurrentUser();
        List<WishlistItem> list = MockDataFactory.getWishlistsByUser().getOrDefault(me.getId(), new ArrayList<>());
        myWishlist.setAll(list);
    }

    public ObservableList<WishlistItem> getMyWishlist() {
        return myWishlist;
    }

    public ObservableList<Item> getCatalog() {
        return catalog;
    }

    public WishlistItem addItemToMyWishlist(Item item, String notes, double targetPrice, String priority) {
        User me = MockDataFactory.getCurrentUser();
        WishlistItem newItem = new WishlistItem(
                "wl-" + UUID.randomUUID().toString().substring(0, 8),
                me.getId(),
                item,
                notes,
                targetPrice > 0 ? targetPrice : item.getPrice(),
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

        // Trigger observable change
        int idx = myWishlist.indexOf(item);
        if (idx >= 0) {
            myWishlist.set(idx, item);
            return true;
        }
        return false;
    }

    public boolean deleteWishlistItem(WishlistItem item) {
        User me = MockDataFactory.getCurrentUser();
        List<WishlistItem> userList = MockDataFactory.getWishlistsByUser().get(me.getId());
        if (userList != null) {
            userList.remove(item);
        }
        return myWishlist.remove(item);
    }

    public ObservableList<WishlistItem> getFriendWishlist(User friend) {
        List<WishlistItem> list = MockDataFactory.getWishlistsByUser().getOrDefault(friend.getId(), Collections.emptyList());
        return FXCollections.observableArrayList(list);
    }

    /**
     * Contributes {@code amount} towards a friend's wishlist item.
     * The contribution is capped at the remaining needed amount so the goal
     * is never exceeded. Any excess is returned as a refund in the result.
     *
     * @param item   the wishlist item to contribute to
     * @param amount the requested contribution (EGP)
     * @return a {@link ContributionResult} with the accepted and refunded amounts,
     *         or {@code null} if the item is already completed or amount <= 0
     */
    public ContributionResult contributeToFriendItem(WishlistItem item, double amount) {
        if (amount <= 0 || item.isCompleted()) return null;

        double remaining = item.getRemainingAmount();
        double accepted  = Math.min(amount, remaining);   // cap at what is still needed
        double refunded  = amount - accepted;              // excess that goes back

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
