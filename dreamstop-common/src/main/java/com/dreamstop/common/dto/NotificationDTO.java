package com.dreamstop.common.dto;

import com.dreamstop.common.model.NotificationType;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an in-app persistent notification stored in
 * the database.
 */
public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int recipientUserId;
    private NotificationType type;
    private String title;
    private String message;
    private Integer relatedItemId;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationDTO() {
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public NotificationDTO(int id, int recipientUserId, NotificationType type, String title, String message,
            Integer relatedItemId) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedItemId = relatedItemId;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(int recipientUserId) {
        this.recipientUserId = recipientUserId;
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

    public Integer getRelatedItemId() {
        return relatedItemId;
    }

    public void setRelatedItemId(Integer relatedItemId) {
        this.relatedItemId = relatedItemId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NotificationDTO that = (NotificationDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "NotificationDTO{" +
                "id=" + id +
                ", recipientUserId=" + recipientUserId +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", read=" + read +
                '}';
    }
}
