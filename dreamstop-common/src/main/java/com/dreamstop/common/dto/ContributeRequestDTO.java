package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class ContributeRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int wishlistItemId;
    private BigDecimal amount;

    public ContributeRequestDTO() {
    }

    public ContributeRequestDTO(int wishlistItemId, BigDecimal amount) {
        this.wishlistItemId = wishlistItemId;
        this.amount = amount;
    }

    public int getWishlistItemId() {
        return wishlistItemId;
    }

    public void setWishlistItemId(int wishlistItemId) {
        this.wishlistItemId = wishlistItemId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
