package com.fuzzysound.walrus.event.infrastructure.converter;

import com.fuzzysound.walrus.event.infrastructure.entity.EventHistoryEntity;
import com.fuzzysound.walrus.event.model.EventHistory;
import com.fuzzysound.walrus.web3.model.WalrusTransaction;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
public class EventHistoryEntityConverter {
    public EventHistoryEntity toEntity(EventHistory model) {
        EventHistoryEntity entity = new EventHistoryEntity();
        WalrusTransaction transaction = model.getTransaction();
        entity.setTransactionHash(transaction.getTransactionHash());
        entity.setFromAddress(transaction.getFrom());
        entity.setToAddress(transaction.getTo());
        entity.setTransferredValue(transaction.getValue().toString());
        entity.setAddress(model.getAddress());
        entity.setTransactionStatus(model.getTransactionStatus());
        entity.setBlockConfirmationCount(model.getBlockConfirmationCount());
        entity.setTimestamp(model.getTimestamp());
        return entity;
    }

    public EventHistory fromEntity(EventHistoryEntity entity) {
        WalrusTransaction transaction = WalrusTransaction.builder()
                .transactionHash(entity.getTransactionHash())
                .from(entity.getFromAddress())
                .to(entity.getToAddress())
                .value(new BigInteger(entity.getTransferredValue()))
                .build();
        return EventHistory.builder()
                .transaction(transaction)
                .address(entity.getAddress())
                .transactionStatus(entity.getTransactionStatus())
                .blockConfirmationCount(entity.getBlockConfirmationCount())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
