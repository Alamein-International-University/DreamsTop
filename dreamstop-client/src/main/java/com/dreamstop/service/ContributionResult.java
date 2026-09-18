package com.dreamstop.service;

/**
 * Result of a contribution attempt.
 * acceptedAmount — what was actually applied to the item (capped at remaining).
 * refundedAmount — the excess returned to the contributor (0 if no overflow).
 */
public class ContributionResult {
    private final double acceptedAmount;
    private final double refundedAmount;

    public ContributionResult(double acceptedAmount, double refundedAmount) {
        this.acceptedAmount = acceptedAmount;
        this.refundedAmount = refundedAmount;
    }

    public double acceptedAmount() {
        return acceptedAmount;
    }

    public double refundedAmount() {
        return refundedAmount;
    }

    public boolean wasRefunded() {
        return refundedAmount > 0;
    }
}