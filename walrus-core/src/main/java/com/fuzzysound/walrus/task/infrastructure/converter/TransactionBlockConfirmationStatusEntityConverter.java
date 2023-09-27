package com.fuzzysound.walrus.task.infrastructure.converter;

import com.fuzzysound.walrus.task.infrastructure.entity.TransactionBlockConfirmationStatusEntity;
import com.fuzzysound.walrus.task.model.TransactionBlockConfirmationStatus;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class TransactionBlockConfirmationStatusEntityConverter {
    public TransactionBlockConfirmationStatusEntity toEntity(
            TransactionBlockConfirmationStatus model
    ) {
        TransactionBlockConfirmationStatusEntity entity = new TransactionBlockConfirmationStatusEntity();
        entity.setTransactionHash(model.getTransactionHash());
        entity.setLastConfirmedBlockNumber(model.getLastConfirmedBlockNumber().toString());
        entity.setCurrentBlockConfirmationCount(model.getCurrentBlockConfirmationCount());
        entity.setIsSettled(model.isSettled());
        return entity;
    }
    public TransactionBlockConfirmationStatus fromEntity(
            TransactionBlockConfirmationStatusEntity entity
    ) {
        return TransactionBlockConfirmationStatus.builder()
                .transactionHash(entity.getTransactionHash())
                .lastConfirmedBlockNumber(new BigInteger(entity.getLastConfirmedBlockNumber()))
                .currentBlockConfirmationCount(entity.getCurrentBlockConfirmationCount())
                .isSettled(entity.getIsSettled())
                .build();
    }
}
