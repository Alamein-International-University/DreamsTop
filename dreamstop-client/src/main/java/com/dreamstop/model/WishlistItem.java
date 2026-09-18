package com.dreamstop.model;

import java.util.Objects;

public class WishlistItem {
    private String id;
    private String userId;
    private Item item;
    private String notes;
    private double targetAmount;
    private double currentAmount;
    private boolean completed;
    private String priority; // HIGH, MEDIUM, LOW

    public WishlistItem() {}

    public WishlistItem(String id, String userId, Item item, String notes, double targetAmount, double currentAmount, String priority) {
        this.id = id;
        this.userId = userId;
        this.item = item;
        this.notes = notes;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.completed = currentAmount >= targetAmount;
        this.priority = priority != null ? priority : "MEDIUM";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
        this.completed = this.currentAmount >= targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
        this.completed = this.currentAmount >= this.targetAmount;
    }

    public boolean isCompleted() {
        return completed || (targetAmount > 0 && currentAmount >= targetAmount);
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getPriority() {
        return priority != null ? priority : "MEDIUM";
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public double getProgressRatio() {
        if (targetAmount <= 0) return 1.0;
        return Math.min(1.0, Math.max(0.0, currentAmount / targetAmount));
    }

    public double getProgressPercentage() {
        return getProgressRatio() * 100.0;
    }

    public double getRemainingAmount() {
        return Math.max(0.0, targetAmount - currentAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WishlistItem that = (WishlistItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
