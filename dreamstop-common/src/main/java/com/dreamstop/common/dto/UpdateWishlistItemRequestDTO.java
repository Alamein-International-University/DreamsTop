package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Data Transfer Object containing wishlist item fields to update (target amount
 * or priority).
 */
public class UpdateWishlistItemRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int wishlistItemId;
    private BigDecimal targetAmount;
    private String notes;
    private String priority;

    public UpdateWishlistItemRequestDTO() {
    }

    public UpdateWishlistItemRequestDTO(int wishlistItemId, BigDecimal targetAmount, String notes, String priority) {
        this.wishlistItemId = wishlistItemId;
        this.targetAmount = targetAmount;
        this.notes = notes;
        this.priority = priority;
    }

    public int getWishlistItemId() {
        return wishlistItemId;
    }

    public void setWishlistItemId(int wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
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

    @Override
    public String toString() {
        return "UpdateWishlistItemRequestDTO{" +
                "wishlistItemId=" + wishlistItemId +
                ", targetAmount=" + targetAmount +
                ", priority='" + priority + '\'' +
                '}';
    }
}
