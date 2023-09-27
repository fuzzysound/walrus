package com.fuzzysound.walrus.event.model;

import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventHistory {
    private final WalrusTransaction transaction;
    private final String address;
    private final TransactionStatus transactionStatus;
    private final int blockConfirmationCount;
    private final long timestamp;
}
