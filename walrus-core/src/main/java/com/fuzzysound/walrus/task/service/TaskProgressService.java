package com.fuzzysound.walrus.task.service;

import com.fuzzysound.walrus.task.model.TransactionBlockConfirmationStatus;

import java.math.BigInteger;
import java.util.List;

public interface TaskProgressService {
    BigInteger getLastConfirmedBlockNumber(String key);

    void updateLastConfirmedBlockNumber(String key, BigInteger blockNumber);

    List<TransactionBlockConfirmationStatus> getUncompletedTransactionBlockConfirmationStatusList();

    TransactionBlockConfirmationStatus getTransactionBlockConfirmationStatus(String transactionHash);

    void upsertTransactionBlockConfirmationStatus(TransactionBlockConfirmationStatus transactionBlockConfirmationStatus);
}
