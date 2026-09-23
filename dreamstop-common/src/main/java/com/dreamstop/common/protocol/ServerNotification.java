package com.dreamstop.common.protocol;

import com.dreamstop.common.model.NotificationType;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a real-time push notification pushed from server to connected
 * clients.
 * For example: friend requests, new contributions, or completed wishlist items.
 */
public class ServerNotification implements Serializable {
    private static final long serialVersionUID = 1L;

    private NotificationType type;
    private String title;
    private String message;
    private Integer relatedWishlistItemId;
    private String extraDataJson;
    private LocalDateTime timestamp;

    public ServerNotification() {
        this.timestamp = LocalDateTime.now();
    }

    public ServerNotification(NotificationType type, String title, String message) {
        this(type, title, message, null);
    }

    public ServerNotification(NotificationType type, String title, String message, Integer relatedWishlistItemId) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedWishlistItemId = relatedWishlistItemId;
        this.timestamp = LocalDateTime.now();
    }

    public static ServerNotification itemCompletedForBuyer(String itemTitle, Integer wishlistItemId) {
        return new ServerNotification(
                NotificationType.ITEM_COMPLETED_BUYER,
                "Gift Fully Funded! 🎁",
                "The item \"" + itemTitle + "\" you contributed to has been fully funded!",
                wishlistItemId);
    }

    public static ServerNotification itemCompletedForReceiver(String itemTitle, String friendNames,
            Integer wishlistItemId) {
        return new ServerNotification(
                NotificationType.ITEM_COMPLETED_RECEIVER,
                "Your Wish Came True! ✨",
                "Your wishlist item \"" + itemTitle + "\" has been fully funded by: " + friendNames,
                wishlistItemId);
    }

    public static ServerNotification contributionReceived(String friendName, String itemTitle, Integer wishlistItemId) {
        return new ServerNotification(
                NotificationType.CONTRIBUTION_RECEIVED,
                "New Contribution! 💰",
                friendName + " contributed towards your item: " + itemTitle,
                wishlistItemId);
    }

    public static ServerNotification wishlistUpdated(String title, String message, Integer wishlistItemId) {
        return new ServerNotification(
                NotificationType.WISHLIST_UPDATED,
                title,
                message,
                wishlistItemId);
    }

    public static ServerNotification profileUpdated(String title, String message) {
        return new ServerNotification(
                NotificationType.PROFILE_UPDATED,
                title,
                message);
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRelatedWishlistItemId() {
        return relatedWishlistItemId;
    }

    public void setRelatedWishlistItemId(Integer relatedWishlistItemId) {
        this.relatedWishlistItemId = relatedWishlistItemId;
    }

    public String getExtraDataJson() {
        return extraDataJson;
    }

    public void setExtraDataJson(String extraDataJson) {
        this.extraDataJson = extraDataJson;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ServerNotification{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", relatedWishlistItemId=" + relatedWishlistItemId +
                ", timestamp=" + timestamp +
                '}';
    }
}
