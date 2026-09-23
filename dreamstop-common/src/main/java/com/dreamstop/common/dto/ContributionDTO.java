package com.dreamstop.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a financial contribution made by a user
 * towards a friend's wishlist item.
 */
public class ContributionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int contributorId;
    private String contributorUsername;
    private int wishlistItemId;
    private BigDecimal amount;
    private LocalDateTime contributionDate;

    public ContributionDTO() {
    }

    public ContributionDTO(int id, int contributorId, String contributorUsername, int wishlistItemId, BigDecimal amount,
            LocalDateTime contributionDate) {
        this.id = id;
        this.contributorId = contributorId;
        this.contributorUsername = contributorUsername;
        this.wishlistItemId = wishlistItemId;
        this.amount = amount;
        this.contributionDate = contributionDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContributorId() {
        return contributorId;
    }

    public void setContributorId(int contributorId) {
        this.contributorId = contributorId;
    }

    public String getContributorUsername() {
        return contributorUsername;
    }

    public void setContributorUsername(String contributorUsername) {
        this.contributorUsername = contributorUsername;
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

    public LocalDateTime getContributionDate() {
        return contributionDate;
    }

    public void setContributionDate(LocalDateTime contributionDate) {
        this.contributionDate = contributionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ContributionDTO that = (ContributionDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "ContributionDTO{" +
                "id=" + id +
                ", contributorUsername='" + contributorUsername + '\'' +
                ", wishlistItemId=" + wishlistItemId +
                ", amount=" + amount +
                ", contributionDate=" + contributionDate +
                '}';
    }
}
