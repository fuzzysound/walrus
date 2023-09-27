package com.fuzzysound.walrus.task.service;

import com.fuzzysound.walrus.task.infrastructure.converter.TransactionBlockConfirmationStatusEntityConverter;
import com.fuzzysound.walrus.task.infrastructure.entity.LastConfirmedBlockNumberEntity;
import com.fuzzysound.walrus.task.infrastructure.entity.TransactionBlockConfirmationStatusEntity;
import com.fuzzysound.walrus.task.infrastructure.repository.LastConfirmedBlockNumberRepository;
import com.fuzzysound.walrus.task.infrastructure.repository.TransactionBlockConfirmationStatusRepository;
import com.fuzzysound.walrus.task.model.TransactionBlockConfirmationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static com.fuzzysound.walrus.common.Web3Constants.*;

@Service
@RequiredArgsConstructor
public class DefaultTaskProgressService implements TaskProgressService {
    private final TransactionBlockConfirmationStatusEntityConverter transactionBlockConfirmationStatusEntityConverter;
    private final LastConfirmedBlockNumberRepository lastConfirmedBlockNumberRepository;
    private final TransactionBlockConfirmationStatusRepository transactionBlockConfirmationStatusRepository;

    @Override
    public BigInteger getLastConfirmedBlockNumber(String key) {
        Optional<LastConfirmedBlockNumberEntity> lastConfirmedBlockNumberEntityOptional =
                lastConfirmedBlockNumberRepository.findByDistKey(key);
        return lastConfirmedBlockNumberEntityOptional
                .map(lastConfirmedBlockNumberEntity -> new BigInteger(lastConfirmedBlockNumberEntity.getBlockNumber()))
                .orElse(null);
    }

    @Override
    @Transactional
    public void updateLastConfirmedBlockNumber(String key, BigInteger blockNumber) {
        Optional<LastConfirmedBlockNumberEntity> lastConfirmedBlockNumberEntityOptional =
                lastConfirmedBlockNumberRepository.findByDistKey(key);
        if (lastConfirmedBlockNumberEntityOptional.isPresent()) {
            LastConfirmedBlockNumberEntity lastConfirmedBlockNumberEntity =
                    lastConfirmedBlockNumberEntityOptional.get();
            BigInteger prevBlockNumber = new BigInteger(lastConfirmedBlockNumberEntity.getBlockNumber());
            if (blockNumber.compareTo(prevBlockNumber) > 0) {
                lastConfirmedBlockNumberEntity.setBlockNumber(blockNumber.toString());
            }
        } else {
            LastConfirmedBlockNumberEntity lastConfirmedBlockNumberEntity = new LastConfirmedBlockNumberEntity();
            lastConfirmedBlockNumberEntity.setDistKey(key);
            lastConfirmedBlockNumberEntity.setBlockNumber(blockNumber.toString());
            lastConfirmedBlockNumberRepository.save(lastConfirmedBlockNumberEntity);
        }
    }

    @Override
    public List<TransactionBlockConfirmationStatus> getUncompletedTransactionBlockConfirmationStatusList() {
        return transactionBlockConfirmationStatusRepository.findAllByCurrentBlockConfirmationCountLessThan(
                MAX_BLOCK_CONFIRMATION_COUNT
        ).stream()
                .map(transactionBlockConfirmationStatusEntityConverter::fromEntity)
                .toList();
    }

    @Override
    public TransactionBlockConfirmationStatus getTransactionBlockConfirmationStatus(String transactionHash) {
        Optional<TransactionBlockConfirmationStatusEntity> entityOptional
                = transactionBlockConfirmationStatusRepository.findByTransactionHash(transactionHash);
        if (entityOptional.isPresent()) {
            return transactionBlockConfirmationStatusEntityConverter.fromEntity(entityOptional.get());
        } else {
            throw new IllegalArgumentException(
                    "TransactionBlockConfirmStatus does not exist for transaction hash " + transactionHash
            );
        }
    }

    @Override
    @Transactional
    public void upsertTransactionBlockConfirmationStatus(
            TransactionBlockConfirmationStatus transactionBlockConfirmationStatus
    ) {
        Optional<TransactionBlockConfirmationStatusEntity> entityOptional
                = transactionBlockConfirmationStatusRepository.findByTransactionHash(
                transactionBlockConfirmationStatus.getTransactionHash()
        );
        if (entityOptional.isPresent()) {
            TransactionBlockConfirmationStatusEntity entity = entityOptional.get();;
            if (transactionBlockConfirmationStatus.getCurrentBlockConfirmationCount()
                    > entity.getCurrentBlockConfirmationCount()) {
                entity.setLastConfirmedBlockNumber(transactionBlockConfirmationStatus.getLastConfirmedBlockNumber()
                        .toString());
                entity.setCurrentBlockConfirmationCount(transactionBlockConfirmationStatus
                        .getCurrentBlockConfirmationCount());
                entity.setIsSettled(transactionBlockConfirmationStatus.isSettled());
            }
        } else {
            TransactionBlockConfirmationStatusEntity entity
                    = transactionBlockConfirmationStatusEntityConverter.toEntity(transactionBlockConfirmationStatus);
            transactionBlockConfirmationStatusRepository.save(entity);
        }
    }
}
