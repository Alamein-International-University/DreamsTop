package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object representing an item in a user's wishlist.
 */
public class WishlistItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private ItemDTO item;
    private BigDecimal targetAmount;
    private BigDecimal currentPaidAmount;
    private String notes;
    private String priority;
    private boolean completed;

    public WishlistItemDTO() {
    }

    public WishlistItemDTO(int id, int userId, ItemDTO item, BigDecimal currentPaidAmount, boolean completed) {
        this(id, userId, item, null, currentPaidAmount, "", "MEDIUM", completed);
    }

    public WishlistItemDTO(int id, int userId, ItemDTO item, BigDecimal targetAmount, BigDecimal currentPaidAmount,
            String notes, String priority, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.item = item;
        this.targetAmount = targetAmount;
        this.currentPaidAmount = currentPaidAmount;
        this.notes = notes;
        this.priority = priority;
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public ItemDTO getItem() {
        return item;
    }

    public void setItem(ItemDTO item) {
        this.item = item;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public BigDecimal getCurrentPaidAmount() {
        return currentPaidAmount;
    }

    public void setCurrentPaidAmount(BigDecimal currentPaidAmount) {
        this.currentPaidAmount = currentPaidAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WishlistItemDTO that = (WishlistItemDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "WishlistItemDTO{" +
                "id=" + id +
                ", userId=" + userId +
                ", item=" + (item != null ? item.getName() : "null") +
                ", targetAmount=" + targetAmount +
                ", currentPaidAmount=" + currentPaidAmount +
                ", completed=" + completed +
                '}';
    }
}
