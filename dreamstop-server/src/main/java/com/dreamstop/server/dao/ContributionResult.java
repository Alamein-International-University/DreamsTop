package com.dreamstop.server.dao;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Encapsulates the outcome of a financial contribution toward a wishlist item,
 * including excess payment refund details and goal completion status.
 *
 * @author Omar ElSharkawy (@omarehab544)
 */
public class ContributionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final int contributionId;
    private final BigDecimal acceptedAmount;
    private final BigDecimal refundedAmount;
    private final boolean itemCompleted;

    public ContributionResult(boolean success, String message, int contributionId,
                              BigDecimal acceptedAmount, BigDecimal refundedAmount, boolean itemCompleted) {
        this.success = success;
        this.message = message;
        this.contributionId = contributionId;
        this.acceptedAmount = acceptedAmount != null ? acceptedAmount : BigDecimal.ZERO;
        this.refundedAmount = refundedAmount != null ? refundedAmount : BigDecimal.ZERO;
        this.itemCompleted = itemCompleted;
    }

    public static ContributionResult failure(String message) {
        return new ContributionResult(false, message, 0, BigDecimal.ZERO, BigDecimal.ZERO, false);
    }

    public static ContributionResult success(int contributionId, BigDecimal accepted, BigDecimal refunded, boolean completed) {
        return new ContributionResult(true, "Contribution processed successfully", contributionId, accepted, refunded, completed);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getContributionId() {
        return contributionId;
    }

    public BigDecimal getAcceptedAmount() {
        return acceptedAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public boolean wasRefunded() {
        return refundedAmount != null && refundedAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isItemCompleted() {
        return itemCompleted;
    }
}
