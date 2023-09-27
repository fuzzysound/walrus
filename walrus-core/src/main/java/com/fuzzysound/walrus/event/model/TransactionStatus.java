package com.fuzzysound.walrus.event.model;

import lombok.Getter;

public enum TransactionStatus {
    PENDING("Pending"),
    MINED("Mined"),
    CONFIRMED("Confirmed");

    private final String desc;

    TransactionStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
