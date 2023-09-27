package com.fuzzysound.walrus.task.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;

@Getter
@Builder
public class TransactionBlockConfirmationStatus {
    private final String transactionHash;
    private final BigInteger lastConfirmedBlockNumber;
    private final int currentBlockConfirmationCount;
    private final boolean isSettled;
}
