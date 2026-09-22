package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
        this.targetAmount = BigDecimal.ZERO;
        this.currentPaidAmount = BigDecimal.ZERO;
        this.notes = "";
        this.priority = "MEDIUM";
        this.completed = false;
    }

    public WishlistItemDTO(int id, int userId, ItemDTO item, BigDecimal currentPaidAmount, boolean completed) {
        this(id, userId, item, item != null ? item.getPrice() : BigDecimal.ZERO, currentPaidAmount, "", "MEDIUM", completed);
    }

    public WishlistItemDTO(int id, int userId, ItemDTO item, BigDecimal targetAmount, BigDecimal currentPaidAmount, String notes, String priority, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.item = item;
        this.targetAmount = targetAmount != null ? targetAmount : (item != null ? item.getPrice() : BigDecimal.ZERO);
        this.currentPaidAmount = currentPaidAmount != null ? currentPaidAmount : BigDecimal.ZERO;
        this.notes = notes != null ? notes : "";
        this.priority = priority != null ? priority : "MEDIUM";
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

    public BigDecimal getEffectiveTarget() {
        if (targetAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            return targetAmount;
        }
        if (item != null && item.getPrice() != null) {
            return item.getPrice();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getRemainingAmount() {
        BigDecimal target = getEffectiveTarget();
        if (target.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = target.subtract(currentPaidAmount != null ? currentPaidAmount : BigDecimal.ZERO);
        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining;
    }

    public double getFundingProgressPercentage() {
        BigDecimal target = getEffectiveTarget();
        if (target.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }
        BigDecimal paid = currentPaidAmount != null ? currentPaidAmount : BigDecimal.ZERO;
        BigDecimal progress = paid
                .divide(target, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return Math.min(progress.doubleValue(), 100.0);
    }
}
