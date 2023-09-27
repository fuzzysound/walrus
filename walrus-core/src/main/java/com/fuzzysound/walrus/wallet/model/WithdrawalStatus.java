package com.fuzzysound.walrus.wallet.model;

public enum WithdrawalStatus {
    REQUESTED,
    PENDING,
    CONFIRMED,
    CANCELLED;

    public boolean isOnProcess() {
        return this.equals(REQUESTED) || this.equals(PENDING);
    }
}
