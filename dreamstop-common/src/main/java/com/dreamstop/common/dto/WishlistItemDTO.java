package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WishlistItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private ItemDTO item;
    private BigDecimal currentPaidAmount;
    private boolean completed;

    public WishlistItemDTO() {
        this.currentPaidAmount = BigDecimal.ZERO;
        this.completed = false;
    }

    public WishlistItemDTO(int id, int userId, ItemDTO item, BigDecimal currentPaidAmount, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.item = item;
        this.currentPaidAmount = currentPaidAmount != null ? currentPaidAmount : BigDecimal.ZERO;
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

    public BigDecimal getCurrentPaidAmount() {
        return currentPaidAmount;
    }

    public void setCurrentPaidAmount(BigDecimal currentPaidAmount) {
        this.currentPaidAmount = currentPaidAmount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public BigDecimal getRemainingAmount() {
        if (item == null || item.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = item.getPrice().subtract(currentPaidAmount);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    public double getFundingProgressPercentage() {
        if (item == null || item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal progress = currentPaidAmount
                .divide(item.getPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return Math.min(progress.doubleValue(), 100.0);
    }
}
